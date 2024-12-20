package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

class MainPageVM(val user: UserSession?, val permissions: Permissions) : ViewModel
