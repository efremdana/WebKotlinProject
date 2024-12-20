package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Ingredient
import ru.ac.uniyar.domain.IngredientUnit
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class EditIngredientFormVM(
    val form: WebForm,
    val units: List<IngredientUnit>,
    val ingredient: Ingredient,
    val errors: List<String>,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
