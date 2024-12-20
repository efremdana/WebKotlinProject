package ru.ac.uniyar.domain

import java.time.LocalDate
import kotlin.math.ceil

class Cookbook(private val _recipes: MutableList<Recipe>) {
    val recipes: List<Recipe>
        get() = _recipes.toList()

    fun addRecipe(recipe: Recipe) {
        if (recipe.number != _recipes.size) {
            _recipes.add(recipe)
        } else {
            _recipes.add(recipe.copy(number = _recipes.size))
        }
    }

    fun removeRecipe(number: Int) {
        _recipes.removeIf {
            it.number == number
        }
    }

    fun editRecipe(recipe: Recipe) {
        _recipes.forEachIndexed { i, r ->
            if (r.number == recipe.number) {
                _recipes[i] = recipe
            }
        }
    }

    fun getRecipeByNumber(value: Int): Recipe? {
        return _recipes.find {
            it.number == value
        }
    }

    fun pageAmount(
        numberElements: Int,
        listRecipe: MutableList<Recipe> = _recipes,
    ): Int {
        return ceil(listRecipe.size / numberElements.toDouble())
            .toInt()
    }

    fun recipesByNumberPage(
        page: Int,
        numberElements: Int,
        listRecipeFilter: List<Recipe>,
    ): List<Recipe> {
        val startIndex = (page - 1) * numberElements
        val endIndex = minOf(startIndex + numberElements, listRecipeFilter.size)
        return if (startIndex < listRecipeFilter.size) {
            listRecipeFilter.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    fun recipesByFilter(
        typeDish: TypeDish?,
        minCookingTime: Int?,
        maxCookingTime: Int?,
    ): List<Recipe> {
        val list =
            _recipes.filter { recipe ->
                (typeDish == null || recipe.type == typeDish) &&
                    (minCookingTime == null || recipe.cookingTime >= minCookingTime) &&
                    (maxCookingTime == null || recipe.cookingTime <= maxCookingTime)
            }
        return list.sortedBy { LocalDate.parse(it.date) }
    }
}
