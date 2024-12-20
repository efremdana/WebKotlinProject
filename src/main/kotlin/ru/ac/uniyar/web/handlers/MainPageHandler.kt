package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.models.MainPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

class MainPageHandler(
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val userSession = key(request)
        val permissions = access(request)
        val model = MainPageVM(userSession, permissions)
        return Response(Status.OK).body(renderer(model))
    }
}
