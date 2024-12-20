package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class RecipeVM(val recipe: Recipe, val permissions: Permissions, val user: UserSession?) : ViewModel
