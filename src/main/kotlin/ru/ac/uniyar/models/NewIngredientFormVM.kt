package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.IngredientUnit
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class NewIngredientFormVM(
    val form: WebForm,
    val units: List<IngredientUnit>,
    val errors: List<String>,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
