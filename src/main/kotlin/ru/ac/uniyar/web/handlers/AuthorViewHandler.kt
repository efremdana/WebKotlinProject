package ru.ac.uniyar.web.handlers

import com.google.gson.Gson
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.models.AuthorPageVM
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession
import java.io.FileReader

class AuthorViewHandler(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = key(request)
        val permissions = access(request)
        val modelError = ErrorPageVM(request.uri)
        val userName =
            request
                .path("userName")
                ?: return Response(Status.NOT_FOUND).body(renderer(modelError))
        val client = readClientFromFile(userName)
        val model = AuthorPageVM(client, user, permissions)
        return Response(Status.OK).body(renderer(model))
    }

    private fun readClientFromFile(userName: String): Client? {
        val gson = Gson()
        val reader = FileReader("clients.json", Charsets.UTF_8)
        val listClient: List<Client> = gson.fromJson(reader, Array<Client>::class.java).toList()
        return listClient.find {
            it.userName == userName
        }
    }
}
