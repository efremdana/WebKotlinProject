package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class AuthorPageVM(
    val author: Client?,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
