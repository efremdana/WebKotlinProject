package ru.ac.uniyar.web.session

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import java.io.File
import java.io.FileReader

data class UserSession(val name: String, val userName: String)

data class UserAndRole(val userName: String, val role: String)

data class Permissions(
    val canListUsers: Boolean = false,
    val canAddUser: Boolean = false,
    val canAddRecipe: Boolean = false,
    val canEditAndDeleteRecipe: Boolean = false,
) {
    companion object {
        private val rolesUser = File("roles-user.json")
        private val gson = Gson()

        fun identifyingUserRole(userName: String?): Permissions {
            val reader = FileReader(rolesUser, Charsets.UTF_8)
            val listUserAndRole: List<UserAndRole> = gson.fromJson(reader, Array<UserAndRole>::class.java).toList()
            val user = listUserAndRole.find { it.userName == userName }
            return getRole(user)
        }

        fun findUser(userName: String): UserAndRole? {
            val reader = FileReader(rolesUser, Charsets.UTF_8)
            val listUserAndRole: List<UserAndRole> = gson.fromJson(reader, Array<UserAndRole>::class.java).toList()
            return listUserAndRole.find { it.userName == userName }
        }

        fun editRoleUser(
            userName: String,
            roleEdited: String,
        ) {
            val reader = FileReader(rolesUser, Charsets.UTF_8)
            val listUserAndRole: MutableList<UserAndRole> =
                gson.fromJson(reader, Array<UserAndRole>::class.java).toMutableList()
            listUserAndRole.removeIf { it.userName == userName }
            listUserAndRole.add(UserAndRole(userName, roleEdited))
            val mapper = ObjectMapper()
            mapper.writeValue(rolesUser, listUserAndRole)
            reader.close()
        }

        private fun getRole(user: UserAndRole?) =
            when (user?.role) {
                "admin" ->
                    Permissions(
                        true,
                        true,
                        true,
                        true,
                    )
                "moderator" ->
                    Permissions(
                        canAddRecipe = true,
                        canEditAndDeleteRecipe = true,
                    )
                "user" -> Permissions(canAddRecipe = true)
                else -> Permissions()
            }
    }
}
