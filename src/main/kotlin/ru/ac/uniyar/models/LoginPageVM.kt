package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

data class LoginPageVM(
    val form: WebForm,
    val errors: List<String>,
    val user: UserSession?,
    val permissions: Permissions,
) : ViewModel
