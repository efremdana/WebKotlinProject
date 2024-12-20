package ru.ac.uniyar.domain

class Ingredient(val name: String, val number: Int, val unit: IngredientUnit) {
    override fun toString(): String {
        return "$name - $number ${unit.type}"
    }
}
