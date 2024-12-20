package ru.ac.uniyar.web.handlers

import org.http4k.lens.Failure
import org.http4k.lens.WebForm
import ru.ac.uniyar.domain.Ingredient
import ru.ac.uniyar.domain.IngredientUnit
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.domain.TypeDish

class ValidatingForm {
    private val errors = mutableListOf<String>()

    fun addErrorRequiredField(nameField: String) {
        errors.add("Поле \"$nameField\" обязательно")
    }

    fun addErrorIncorrectFormatFields(nameField: String) {
        errors.add("Неверный формат поля \"$nameField\"")
    }

    fun addErrorPasswordInequality() {
        errors.add("Пароли не равны")
    }

    fun addErrorInvalidUserOrPassword() {
        errors.add("Неверный логин или пароль")
    }

    fun getErrorsList(): List<String> {
        return errors.toList()
    }

    fun validateString(str: String): Boolean {
        val result = str.toIntOrNull()
        return result == null
    }

    fun addAllErrorWebForm(webForm: WebForm) {
        for (error in webForm.errors) {
            if (error.type == Failure.Type.Missing) {
                addErrorRequiredField(ingredientFormField(error.meta.name))
            }
            if (error.type == Failure.Type.Invalid) {
                addErrorIncorrectFormatFields(ingredientFormField(error.meta.name))
            }
        }
    }

    fun validateIngredient(
        name: String,
        type: IngredientUnit?,
        number: Int,
    ): Ingredient? {
        if (!validateString(name)) {
            addErrorIncorrectFormatFields(ingredientFormField("name"))
            return null
        }
        if (type == null) {
            addErrorIncorrectFormatFields(ingredientFormField("unit"))
            return null
        }
        return Ingredient(name, number, type)
    }

    fun validateRecipe(
        name: String,
        type: TypeDish?,
        cookingTime: Int,
        index: Int,
        description: String,
        date: String,
        author: String,
    ): Recipe? {
        if (!validateString(name)) {
            addErrorIncorrectFormatFields(recipeFormTypeField("name"))
            return null
        }
        if (type == null) {
            addErrorIncorrectFormatFields(recipeFormTypeField("type"))
            return null
        }
        return Recipe(name, type, cookingTime, index, description, date, author)
    }

    fun validatePassword(
        password: String,
        passwordCheck: String,
    ): Boolean {
        if (password != passwordCheck) {
            addErrorPasswordInequality()
            return false
        }
        val digitRegex = Regex("\\d")
        val upperCaseRegex = Regex("[A-Z]")
        val lowerCaseRegex = Regex("[a-z]")
        val hasMinimumLength = password.length >= 8
        val hasDigits = digitRegex.containsMatchIn(password)
        val hasUpperCase = upperCaseRegex.containsMatchIn(password)
        val hasLowerCase = lowerCaseRegex.containsMatchIn(password)
        return hasMinimumLength && hasDigits && hasUpperCase && hasLowerCase
    }
}
