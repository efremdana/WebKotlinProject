package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
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
import ru.ac.uniyar.models.NewUserFormVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession
import java.time.LocalDate

private val nameField = FormField.string().required("name")
private val userNameField = FormField.string().required("userName")
private val passwordField = FormField.string().required("password")
private val passwordCheckField = FormField.string().required("passwordCheck")
private val addUserForm =
    Body.webForm(
        Validator.Feedback,
        nameField,
        userNameField,
        passwordField,
        passwordCheckField,
    ).toLens()

class NewUserFormViewHandler(
    val renderer: TemplateRenderer,
    val access: RequestContextLens<Permissions>,
    val key: BiDiLens<Request, UserSession?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val model = NewUserFormVM(WebForm(), listOf(), user, permissions)
        return Response(Status.OK).body(renderer(model))
    }
}

class AdditionNewUser(
    val renderer: TemplateRenderer,
    val webConfig: WebConfig,
    val access: RequestContextLens<Permissions>,
    val key: BiDiLens<Request, UserSession?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val webForm = addUserForm(request)
        val validateForm = ValidatingForm()
        val name = nameField(webForm)
        val userName = userNameField(webForm)
        val password = passwordField(webForm)
        val passwordCheck = passwordCheckField(webForm)
        val passwordHash = Client.createHashPassword(password, webConfig)
        if (!validateForm.validatePassword(password, passwordCheck) &&
            Client.checkUserForPresence(userName, passwordHash) != null
        ) {
            validateForm.addErrorInvalidUserOrPassword()
            val model = NewUserFormVM(webForm, validateForm.getErrorsList(), user, permissions)
            return Response(Status.BAD_REQUEST).body(renderer(model))
        }
        val client = Client(name, LocalDate.now().toString(), passwordHash, userName)
        Client.addUser(client)
        return Response(Status.FOUND).header("Location", "/users")
    }
}
