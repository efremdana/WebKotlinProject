package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.lens.BiDiLens
import org.http4k.lens.FormField
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.config.WebConfig
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.models.LoginPageVM
import ru.ac.uniyar.web.session.JwtTools
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

private val userNameField = FormField.string().required("userName")
private val passwordField = FormField.string().required("password")
private val passwordCheckField = FormField.string().required("passwordCheck")
private val loginForm =
    Body.webForm(
        Validator.Feedback,
        userNameField,
        passwordField,
        passwordCheckField,
    ).toLens()
private const val DAYS = 7
private const val HOURS = 24
private const val MINUTES = 60
private const val SECONDS = 60

class LoginFormViewHandler(
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val webForm = WebForm()
        val model = LoginPageVM(webForm, listOf(), user, permissions)
        return Response(Status.OK).body(renderer(model))
    }
}

class AuthUsersHandler(
    val renderer: TemplateRenderer,
    val webConfig: WebConfig,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val webForm = loginForm(request)
        val validateForm = ValidatingForm()
        val jwtTools = JwtTools(webConfig.saltJWT, "Cookb00k")
        val userName = userNameField(webForm)
        val password = passwordField(webForm)
        val passwordCheck = passwordCheckField(webForm)
        val passwordHash = Client.createHashPassword(password, webConfig)
        if (!validateForm.validatePassword(password, passwordCheck) &&
            Client.checkUserForPresence(userName, passwordHash) == null
        ) {
            validateForm.addErrorInvalidUserOrPassword()
            val model = LoginPageVM(webForm, validateForm.getErrorsList(), user, permissions)
            return Response(Status.BAD_REQUEST).body(renderer(model))
        }
        val token = jwtTools.createJWT(userName)
        val lifetime = DAYS * HOURS * MINUTES * SECONDS
        val cookie = Cookie("auth", token, lifetime.toLong())
        return Response(Status.FOUND).cookie(cookie).header("Location", "/")
    }
}
