package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Ingredient
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class DeleteIngredientFormVM(
    val checkRemove: String?,
    val ingredient: Ingredient,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
