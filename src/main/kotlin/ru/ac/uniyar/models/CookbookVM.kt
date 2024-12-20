package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.TypeDish
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession
import ru.ac.uniyar.web.view.Paginator

data class CookbookVM(
    val paginator: Paginator,
    val types: List<TypeDish>,
    val permissions: Permissions,
    val user: UserSession?,
) : ViewModel
