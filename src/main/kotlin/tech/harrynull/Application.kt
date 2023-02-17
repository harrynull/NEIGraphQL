package tech.harrynull

import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import tech.harrynull.orm.*
import tech.harrynull.plugins.*

val database = Database.connect("jdbc:postgresql://127.0.0.1:5432/", user = "postgres", password = "root")

data class ItemWithPosition(val position: Int, val item: Item)

data class OutputItem(val probability: Double, val item: Item, val stackSize: Int)

data class Recipe(
    val id: String,
    val inputs: List<ItemWithPosition>,
    val outputs: List<OutputItem>,
    val recipeType: RecipeType,
) {
    companion object {
        fun fromRecipeId(recipeId: String): Recipe {
            val recipeType = database.from(RecipeRecipeType)
                .select(RecipeRecipeType.recipeTypeId)
                .where {RecipeRecipeType.id eq recipeId }
                .limit(1)
                .iterator().next()[RecipeRecipeType.recipeTypeId]!!
            return Recipe(
                id = recipeId,
                inputs = database.from(RecipeItemGroup)
                    .fullJoin(ItemGroupItemStacks, on = ItemGroupItemStacks.itemGroupId eq RecipeItemGroup.itemInputsId)
                    .fullJoin(Items, on = Items.id eq ItemGroupItemStacks.itemStacksItemId)
                    .select()
                    .where { RecipeItemGroup.recipeId eq recipeId }
                    .map { row ->
                        ItemWithPosition(row[RecipeItemGroup.itemInputsKey]!!, Items.createEntity(row))
                    },
                outputs = database.from(RecipeItemOutputsItems)
                    .fullJoin(Items, on = Items.id eq RecipeItemOutputsItems.itemOutputsValueItemId)
                    .select()
                    .where { RecipeItemOutputsItems.recipeId eq recipeId }
                    .map { row ->
                        OutputItem(
                            row[RecipeItemOutputsItems.itemOutputsValueProbability]!!,
                            Items.createEntity(row),
                            row[RecipeItemOutputsItems.itemOutputsValueStackSize]!!
                        )
                    },
                recipeType = database.sequenceOf(RecipeTypes).find { it.id eq recipeType }!!
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
                resolver { limit: Int, nameQuery: String ->
                    database.sequenceOf(Items)
                        .filter { it.localizedName.toLowerCase() like nameQuery.lowercase() }
                        .take(limit)
                        .toList()
                }
            }
            type<Item> {
                description = "Minecraft Item"
                property("recipes") {
                    resolver { item: Item ->
                        database.from(RecipeItemOutputsItems)
                            .select(RecipeItemOutputsItems.recipeId)
                            .where { RecipeItemOutputsItems.itemOutputsValueItemId eq item.id }
                            .map { Recipe.fromRecipeId(it[RecipeItemOutputsItems.recipeId]!!) }
                    }
                }
                Item::entityClass.ignore()
                Item::properties.ignore()
            }
            type<RecipeType> {
                RecipeType::entityClass.ignore()
                RecipeType::properties.ignore()
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

