package ru.ac.uniyar.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import java.io.File
import java.io.FileReader

data class Recipe(
    val name: String,
    val type: TypeDish,
    val cookingTime: Int,
    val number: Int,
    val description: String,
    val date: String,
    val author: String,
) {
    val listIngredient: MutableList<Ingredient> = mutableListOf()

    val steps: MutableList<String> = mutableListOf()

    fun addAllIngredients(list: List<Ingredient>) {
        listIngredient.addAll(list)
    }

    fun addIngredient(ingredient: Ingredient) {
        listIngredient.add(ingredient)
    }

    fun addCookingSteps(list: List<String>) {
        steps.addAll(list)
    }

    fun getIngredientByIndex(index: Int?): Ingredient? {
        return if (index != null) {
            listIngredient[index]
        } else {
            null
        }
    }

    fun editIngredient(
        ingredient: Ingredient,
        index: Int,
    ) {
        listIngredient[index] = ingredient
    }

    fun removeIngredient(index: Int?) {
        if (index != null) {
            listIngredient.removeAt(index)
        }
    }

    fun getStepsByString(): String {
        return steps.joinToString(separator = "\n")
    }

    companion object {
        private val recipesData = File("data-to-cookbook.json")
        private val mapper = ObjectMapper()
        private val gson = Gson()

        fun getRecipes(): List<Recipe> {
            val reader = FileReader(recipesData, Charsets.UTF_8)
            val listRecipe: List<Recipe> = gson.fromJson(reader, Array<Recipe>::class.java).toList()
            reader.close()
            return listRecipe
        }

        fun addRecipeToDatabase(recipe: Recipe) {
            val listRecipe = getRecipes().toMutableList()
            listRecipe.add(recipe)
            mapper.writeValue(recipesData, listRecipe.toList())
        }

        fun replaceRecipeToDatabase(recipe: Recipe) {
            val listRecipe = getRecipes().toMutableList()
            listRecipe.removeIf { it.number == recipe.number }
            listRecipe.add(recipe)
            mapper.writeValue(recipesData, listRecipe.toList())
        }

        fun removeRecipeToDatabase(recipe: Recipe) {
            val listRecipe = getRecipes().toMutableList()
            listRecipe.removeIf { it.number == recipe.number }
            mapper.writeValue(recipesData, listRecipe.toList())
        }
    }
}
