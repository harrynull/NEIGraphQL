package tech.harrynull

import com.apurebase.kgraphql.GraphQL
import com.apurebase.kgraphql.schema.execution.Executor
import nidomiro.kdataloader.ExecutionResult
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import tech.harrynull.orm.*

val database = Database.connect("jdbc:postgresql://127.0.0.1:5432/", user = "postgres", password = "root")

data class ItemStack(val item: Item, val stackSize: Int)

data class InputItem(val key: Int, val itemStack: ItemStack)

data class OutputItem(val key: Int, val probability: Double, val itemStack: ItemStack)

data class Recipe(val id: String)

fun GraphQL.Configuration.buildSchema() {
    schema {
        configure {
            executor = Executor.DataLoaderPrepared
        }
        query("items") {
            resolver { limit: Int, nameQuery: String?, itemId: String? ->
                database.sequenceOf(Items)
                    .let { items ->
                        if (itemId != null) {
                            items.filter { it.id eq itemId }
                        } else items
                    }
                    .let { items ->
                        if (nameQuery != null) {
                            items.filter { it.localizedName.toLowerCase() like nameQuery.lowercase() }
                        } else items
                    }
                    .sortedBy { it.itemId }
                    .take(limit)
                    .toList()
            }
        }
        type<Item> {
            description = "Minecraft Item"
            dataProperty("recipes") {
                prepare { item: Item -> item.id }
                loader { itemIds -> // List<String> itemIds -> recipeIds
                    val results = database.from(RecipeItemOutputsItems)
                        .select(RecipeItemOutputsItems.itemOutputsValueItemId, RecipeItemOutputsItems.recipeId)
                        .where { RecipeItemOutputsItems.itemOutputsValueItemId inList itemIds }
                        .map { it[RecipeItemOutputsItems.itemOutputsValueItemId] to Recipe(it[RecipeItemOutputsItems.recipeId]!!) }
                        .groupBy { it.first }
                        .mapValues { mapItem -> mapItem.value.map { it.second } }
                        .toMap()
                    itemIds.map { id -> ExecutionResult.Success(results[id] ?: emptyList())  }
                }
            }
            dataProperty("usages") {
                prepare { item: Item -> item.id }
                loader { itemIds -> // List<String> itemIds -> recipeIds
                    val results = database.from(RecipeItemInputsItems)
                        .select(RecipeItemInputsItems.itemInputsItemsId, RecipeItemInputsItems.recipeId)
                        .where { RecipeItemInputsItems.itemInputsItemsId inList itemIds }
                        .map { it[RecipeItemInputsItems.itemInputsItemsId] to Recipe(it[RecipeItemInputsItems.recipeId]!!) }
                        .groupBy { it.first }
                        .mapValues { mapItem -> mapItem.value.map { it.second } }
                        .toMap()
                    itemIds.map { id -> ExecutionResult.Success(results[id] ?: emptyList())  }
                }
            }
            Item::entityClass.ignore()
            Item::properties.ignore()
        }
        type<RecipeType> {
            property("icon") {
                resolver { recipeType: RecipeType ->
                    database.sequenceOf(Items).find { it.id eq recipeType.iconId }!!
                }
            }
            RecipeType::entityClass.ignore()
            RecipeType::properties.ignore()
        }
        type<GregTechRecipe> {
            GregTechRecipe::entityClass.ignore()
            GregTechRecipe::properties.ignore()
        }
        type<Recipe> {
            dataProperty("inputs") {
                prepare { recipe: Recipe -> recipe.id }
                loader { recipeIds: List<String> ->
                    val results = database.from(RecipeItemGroup)
                        .fullJoin(ItemGroupItemStacks, on = ItemGroupItemStacks.itemGroupId eq RecipeItemGroup.itemInputsId)
                        .fullJoin(Items, on = Items.id eq ItemGroupItemStacks.itemStacksItemId)
                        .select()
                        .where { RecipeItemGroup.recipeId inList recipeIds }
                        .map { row ->
                            row[RecipeItemGroup.recipeId] to InputItem(
                                row[RecipeItemGroup.itemInputsKey]!!,
                                ItemStack(Items.createEntity(row), row[ItemGroupItemStacks.itemStacksStackSize]!!)
                            )
                        }
                        .groupBy { it.first }
                        .mapValues { mapItem -> mapItem.value.map { it.second } }
                        .toMap()
                    recipeIds.map { id -> ExecutionResult.Success(results[id] ?: emptyList())  }
                }
            }
            dataProperty("outputs") {
                prepare { recipe: Recipe -> recipe.id }
                loader { recipeIds: List<String> ->
                    val results = database.from(RecipeItemOutputsItems)
                        .fullJoin(Items, on = Items.id eq RecipeItemOutputsItems.itemOutputsValueItemId)
                        .select()
                        .where { RecipeItemOutputsItems.recipeId inList recipeIds }
                        .map { row ->
                            row[RecipeItemOutputsItems.recipeId] to OutputItem(
                                row[RecipeItemOutputsItems.itemOutputsKey]!!,
                                row[RecipeItemOutputsItems.itemOutputsValueProbability]!!,
                                ItemStack(Items.createEntity(row), row[RecipeItemOutputsItems.itemOutputsValueStackSize]!!)
                            )
                        }
                        .groupBy { it.first }
                        .mapValues { mapItem -> mapItem.value.map { it.second } }
                        .toMap()
                    recipeIds.map { id -> ExecutionResult.Success(results[id] ?: emptyList())  }
                }
            }
            dataProperty("recipeType") {
                prepare { it.id }
                loader { recipeIds: List<String> ->
                    val recipeTypeIds = database.from(RecipeRecipeType)
                        .select(RecipeRecipeType.id, RecipeRecipeType.recipeTypeId)
                        .where {RecipeRecipeType.id inList recipeIds }
                        .map { it[RecipeRecipeType.id]!! to it[RecipeRecipeType.recipeTypeId]!! }
                        .toMap()
                    val recipeTypes = database.sequenceOf(RecipeTypes)
                        .filter { it.id inList recipeTypeIds.values }
                        .map {it.id to it}
                        .toMap()
                    recipeIds.map { id -> ExecutionResult.Success(recipeTypes[recipeTypeIds[id]]!!)  }
                }
            }
            dataProperty("gregTechRecipe") {
                prepare { it.id }
                loader { recipeIds: List<String> ->
                    val results = database.sequenceOf(GregTechRecipes).filter { it.recipeId inList recipeIds }
                        .map { it.recipeId to it }
                        .toMap()
                    recipeIds.map { id -> ExecutionResult.Success(results[id]!!) }
                }
            }
        }

    }
}
