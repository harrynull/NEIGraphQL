package tech.harrynull

import com.apurebase.kgraphql.GraphQL
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import tech.harrynull.orm.*

val database = Database.connect("jdbc:postgresql://127.0.0.1:5432/", user = "postgres", password = "root")

data class ItemStack(val item: Item, val stackSize: Int)

data class InputItem(val key: Int, val itemStack: ItemStack)

data class OutputItem(val key: Int, val probability: Double, val itemStack: ItemStack)

data class Recipe(
    val id: String,
    val inputs: List<InputItem>,
    val outputs: List<OutputItem>,
    val recipeType: RecipeType,
    val gregTechRecipe: GregTechRecipe? = null,
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
                        InputItem(
                            row[RecipeItemGroup.itemInputsKey]!!,
                            ItemStack(Items.createEntity(row), row[ItemGroupItemStacks.itemStacksStackSize]!!)
                        )
                    },
                outputs = database.from(RecipeItemOutputsItems)
                    .fullJoin(Items, on = Items.id eq RecipeItemOutputsItems.itemOutputsValueItemId)
                    .select()
                    .where { RecipeItemOutputsItems.recipeId eq recipeId }
                    .map { row ->
                        OutputItem(
                            row[RecipeItemOutputsItems.itemOutputsKey]!!,
                            row[RecipeItemOutputsItems.itemOutputsValueProbability]!!,
                            ItemStack(Items.createEntity(row), row[RecipeItemOutputsItems.itemOutputsValueStackSize]!!)
                        )
                    },
                recipeType = database.sequenceOf(RecipeTypes).find { it.id eq recipeType }!!,
                gregTechRecipe = database.sequenceOf(GregTechRecipes).find { it.recipeId eq recipeId },
            )
        }
    }
}

fun GraphQL.Configuration.buildSchema() {
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
            property("usages") {
                resolver { item: Item ->
                    database.from(RecipeItemInputsItems)
                        .select(RecipeItemInputsItems.recipeId)
                        .where { RecipeItemInputsItems.itemInputsItemsId eq item.id }
                        .map { Recipe.fromRecipeId(it[RecipeItemInputsItems.recipeId]!!) }
                }
            }
            Item::entityClass.ignore()
            Item::properties.ignore()
        }
        type<RecipeType> {
            RecipeType::entityClass.ignore()
            RecipeType::properties.ignore()
        }
        type<GregTechRecipe> {
            GregTechRecipe::entityClass.ignore()
            GregTechRecipe::properties.ignore()
        }
    }
}
