package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.domain.TypeDish
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class EditRecipeFormVM(
    val recipe: Recipe,
    val form: WebForm,
    val types: List<TypeDish>,
    val steps: String,
    val errors: List<String>,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
