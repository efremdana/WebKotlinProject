package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class EditRoleUserPageVM(
    val client: Client,
    val role: String,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
