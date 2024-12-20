package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.models.EditRoleUserPageVM
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

class EditRoleFormViewHandler(
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = key(request)
        val permissions = access(request)
        val modelError = ErrorPageVM(request.uri)
        val userSession = request.path("user")
        val userWithRole =
            userSession?.split("&")
                ?: return Response(Status.NOT_FOUND).body(renderer(modelError))
        val userName = userWithRole[0]
        val role = userWithRole[1]
        val client =
            Client.findUser(userName)
                ?: return Response(Status.NOT_FOUND).body(renderer(modelError))
        if (!checkRoleUser(role)) {
            return Response(Status.NOT_FOUND).body(renderer(modelError))
        }
        val model = EditRoleUserPageVM(client, role, user, permissions)
        return Response(Status.OK).body(renderer(model))
    }

    private fun checkRoleUser(role: String) =
        when (role.lowercase()) {
            "user" -> true
            "moderator" -> true
            else -> false
        }
}

class EditRoleUserForm(val renderer: TemplateRenderer) : HttpHandler {
    override fun invoke(request: Request): Response {
        val modelError = ErrorPageVM(request.uri)
        val form = request.form()
        val roleEdited =
            form.findSingle("role")
                ?: return Response(Status.NOT_FOUND).body(renderer(modelError))
        val client =
            request.path("user")
                ?: return Response(Status.NOT_FOUND).body(renderer(modelError))
        val userName = client.split("&")[0]
        Permissions.editRoleUser(userName, roleEdited)
        return Response(Status.FOUND).header("Location", "/users")
    }
}
