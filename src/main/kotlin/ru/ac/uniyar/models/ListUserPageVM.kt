package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class ListUserPageVM(
    val listUser: List<Pair<Client, String?>>,
    val permissions: Permissions,
    val user: UserSession?,
) : ViewModel
