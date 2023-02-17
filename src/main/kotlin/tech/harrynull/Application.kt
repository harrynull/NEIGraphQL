package tech.harrynull

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.apurebase.kgraphql.GraphQL
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import nei.ITEM
import tech.harrynull.plugins.*

val database = NEIDatabase(getSqlDriver())

data class ItemWithPosition(val position: Int, val item: ITEM)

data class OutputItem(val probability: Double, val item: ITEM, val stackSize: Int)

data class Recipe(val id: String, val inputs: List<ItemWithPosition>, val outputs: List<OutputItem>) {
    companion object {
        fun fromRecipeId(recipeId: String): Recipe {
            return Recipe(
                id = recipeId,
                inputs = database.recipeQueries.findRecipeInputsByRecipeId(recipeId = recipeId, mapper = {
                    ITEM_INPUTS_KEY, ID, IMAGE_FILE_PATH, INTERNAL_NAME,
                    ITEM_DAMAGE, ITEM_ID, LOCALIZED_NAME, MAX_DAMAGE, MAX_STACK_SIZE, MOD_ID, NBT, TOOLTIP,
                    UNLOCALIZED_NAME ->
                    ItemWithPosition(ITEM_INPUTS_KEY, ITEM(
                        ID = ID,
                        IMAGE_FILE_PATH = IMAGE_FILE_PATH,
                        INTERNAL_NAME = INTERNAL_NAME,
                        ITEM_DAMAGE = ITEM_DAMAGE,
                        ITEM_ID = ITEM_ID,
                        LOCALIZED_NAME = LOCALIZED_NAME,
                        MAX_DAMAGE = MAX_DAMAGE,
                        MAX_STACK_SIZE = MAX_STACK_SIZE,
                        MOD_ID = MOD_ID,
                        NBT = NBT,
                        TOOLTIP = TOOLTIP,
                        UNLOCALIZED_NAME = UNLOCALIZED_NAME
                    ))
                }).executeAsList(),
                outputs = database.recipeQueries.findRecipeOutputsByRecipeId(recipeId = recipeId, mapper = {
                    ITEM_OUTPUTS_VALUE_PROBABILITY, ITEM_OUTPUTS_VALUE_STACK_SIZE, ID, IMAGE_FILE_PATH, INTERNAL_NAME,
                    ITEM_DAMAGE, ITEM_ID, LOCALIZED_NAME, MAX_DAMAGE, MAX_STACK_SIZE, MOD_ID, NBT, TOOLTIP,
                    UNLOCALIZED_NAME ->
                    OutputItem(
                        probability = ITEM_OUTPUTS_VALUE_PROBABILITY,
                        item = ITEM(
                            ID = ID,
                            IMAGE_FILE_PATH = IMAGE_FILE_PATH,
                            INTERNAL_NAME = INTERNAL_NAME,
                            ITEM_DAMAGE = ITEM_DAMAGE,
                            ITEM_ID = ITEM_ID,
                            LOCALIZED_NAME = LOCALIZED_NAME,
                            MAX_DAMAGE = MAX_DAMAGE,
                            MAX_STACK_SIZE = MAX_STACK_SIZE,
                            MOD_ID = MOD_ID,
                            NBT = NBT,
                            TOOLTIP = TOOLTIP,
                            UNLOCALIZED_NAME = UNLOCALIZED_NAME
                        ),
                        stackSize = ITEM_OUTPUTS_VALUE_STACK_SIZE
                    )
                }).executeAsList()
            )
        }
    }
}

fun Application.module() {
    install(GraphQL) {
        configureRouting()
        playground = true
        schema {
            query("items") {
                resolver { limit: Long, nameQuery: String ->
                    database.itemQueries.findItems(query = nameQuery, limit = limit).executeAsList()
                }
            }
            type<ITEM> {
                description = "Minecraft Item"
                property("asIngredient") {
                    resolver { item: ITEM ->
                        val recipeIds = database.recipeQueries.findRecipeIdsByOutputItemId(outputItemId = item.ID).executeAsList()
                        recipeIds.map { Recipe.fromRecipeId(it) }
                    }
                }
            }
        }
    }
}
private fun getSqlDriver(): SqlDriver {
    val ds = HikariDataSource()
    ds.jdbcUrl = "jdbc:postgres://127.0.0.1:5432/public"
    ds.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
    ds.username = "postgres"
    ds.password = "root"
    return ds.asJdbcDriver()
}
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

