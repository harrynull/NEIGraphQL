package tech.harrynull.orm

import org.ktorm.entity.Entity
import org.ktorm.schema.*

object RecipeRecipeType : Table<Nothing>("recipe") {
    val id = varchar("id").primaryKey()
    val recipeTypeId = varchar("recipe_type_id")
}

object RecipeItemGroup : Table<Nothing>("recipe_item_group") {
    val recipeId = varchar("recipe_id")
    val itemInputsId = varchar("item_inputs_id")
    val itemInputsKey = int("item_inputs_key")
}

object RecipeItemInputsItems : Table<Nothing>("recipe_item_inputs_items") {
    val recipeId = varchar("recipe_id")
    val itemInputsItemsId = varchar("item_inputs_items_id")
}

object RecipeItemOutputsItems : Table<Nothing>("recipe_item_outputs") {
    val recipeId = varchar("recipe_id")
    val itemOutputsValueItemId = varchar("item_outputs_value_item_id")
    val itemOutputsValueProbability = double("item_outputs_value_probability")
    val itemOutputsValueStackSize = int("item_outputs_value_stack_size")
    val itemOutputsKey = int("item_outputs_key")
}

object ItemGroupItemStacks : Table<Nothing>("item_group_item_stacks") {
    val itemGroupId = varchar("item_group_id")
    val itemStacksItemId = varchar("item_stacks_item_id")
    val itemStacksStackSize = int("item_stacks_stack_size")
}

object Items : Table<Item>("item") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val imageFilePath = varchar("image_file_path").bindTo { it.imageFilePath }
    val internalName = varchar("internal_name").bindTo { it.internalName }
    val item_damage = int("item_damage").bindTo { it.itemDamage }
    val itemId = int("item_id").bindTo { it.itemId }
    val localizedName = varchar("localized_name").bindTo { it.localizedName }
    val maxDamage = int("max_damage").bindTo { it.maxDamage }
    val maxStackSize = int("max_stack_size").bindTo { it.maxStackSize }
    val modId = varchar("mod_id").bindTo { it.modId }
    val nbt = varchar("nbt").bindTo { it.nbt }
    val tooltip = varchar("tooltip").bindTo { it.tooltip }
    val unlocalizedName = varchar("unlocalized_name").bindTo { it.unlocalizedName }
}

object RecipeTypes : Table<RecipeType>("recipe_type") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val category = varchar("category").bindTo { it.category }
    val fluidInputDimensionHeight = int("fluid_input_dimension_height").bindTo { it.fluidInputDimensionHeight }
    val fluidInputDimensionWidth = int("fluid_input_dimension_width").bindTo { it.fluidInputDimensionWidth }
    val fluidOutputDimensionHeight = int("fluid_output_dimension_height").bindTo { it.fluidOutputDimensionHeight }
    val fluidOutputDimensionWidth = int("fluid_output_dimension_width").bindTo { it.fluidOutputDimensionWidth }
    val iconInfo = varchar("icon_info").bindTo { it.iconInfo }
    val itemInputDimensionHeight = int("item_input_dimension_height").bindTo { it.itemInputDimensionHeight }
    val itemInputDimensionWidth = int("item_input_dimension_width").bindTo { it.itemInputDimensionWidth }
    val itemOutputDimensionHeight = int("item_output_dimension_height").bindTo { it.itemOutputDimensionHeight }
    val itemOutputDimensionWidth = int("item_output_dimension_width").bindTo { it.itemOutputDimensionWidth }
    val shapeless = boolean("shapeless").bindTo { it.shapeless }
    val type = varchar("type").bindTo { it.type }
    val iconId = varchar("icon_id").bindTo { it.iconId }
}

interface Item : Entity<Item> {
    companion object : Entity.Factory<Item>()
    val id: String
    val imageFilePath: String
    val internalName: String
    val itemDamage: Int
    val itemId: Int
    val localizedName: String
    val maxDamage: Int
    val maxStackSize: Int
    val modId: String
    val nbt: String
    val tooltip: String
    val unlocalizedName: String
}

interface RecipeType : Entity<RecipeType> {
    companion object : Entity.Factory<RecipeType>()
    val id: String
    val category: String
    val fluidInputDimensionHeight: Int
    val fluidInputDimensionWidth: Int
    val fluidOutputDimensionHeight: Int
    val fluidOutputDimensionWidth: Int
    val iconInfo: String
    val itemInputDimensionHeight: Int
    val itemInputDimensionWidth: Int
    val itemOutputDimensionHeight: Int
    val itemOutputDimensionWidth: Int
    val shapeless: Boolean
    val type: String
    val iconId: String
}

