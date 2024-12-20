package ru.ac.uniyar.web.handlers

import com.google.gson.Gson
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.models.ListUserPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserAndRole
import ru.ac.uniyar.web.session.UserSession
import java.io.File
import java.io.FileReader

class ListUserViewHandler(
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val listUserWithRole = createListUserWithRole()
        val model = ListUserPageVM(listUserWithRole, permissions, user)
        return Response(Status.OK).body(renderer(model))
    }

    private fun createListUserWithRole(): List<Pair<Client, String?>> {
        val gson = Gson()
        val usersFile = File("clients.json")
        val usersRoleFile = File("roles-user.json")
        val readerUsers = FileReader(usersFile, Charsets.UTF_8)
        val readerUsersRole = FileReader(usersRoleFile, Charsets.UTF_8)
        val listUser: List<Client> = gson.fromJson(readerUsers, Array<Client>::class.java).toList()
        val listUsersRole: List<UserAndRole> = gson.fromJson(readerUsersRole, Array<UserAndRole>::class.java).toList()
        val listUserWithRole = mutableListOf<Pair<Client, String?>>()
        listUser.forEach { client ->
            if (client.userName != "admin") {
                val userAndRole =
                    listUsersRole.find { userAndRole ->
                        userAndRole.userName == client.userName
                    }
                listUserWithRole.add(Pair(client, userAndRole?.role))
            }
        }
        return listUserWithRole.toList()
    }
}
