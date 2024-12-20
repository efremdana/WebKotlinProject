package ru.ac.uniyar.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import ru.ac.uniyar.config.WebConfig
import ru.ac.uniyar.web.session.UserAndRole
import ru.ac.uniyar.web.session.UserSession
import java.io.File
import java.io.FileReader
import java.security.MessageDigest

class Client(val name: String, val registration: String, val password: String, val userName: String) {
    private val recipes = mutableSetOf<String>()

    fun recipeCreatedByAuthor(recipe: String) = recipes.contains(recipe)

    fun showListRecipes() = recipes.toList()

    fun addRecipe(recipeName: String) {
        recipes.add(recipeName)
    }

    fun addAllRecipes(list: List<Recipe>) {
        list.forEach { recipes.add(it.name) }
    }

    companion object Database {
        private val dataClients = File("clients.json")
        private val rolesUser = File("roles-user.json")
        private val gson = Gson()
        private val mapper = ObjectMapper()

        fun getUsers(): List<Client> {
            val reader = FileReader(dataClients, Charsets.UTF_8)
            val listUser: List<Client> = gson.fromJson(reader, Array<Client>::class.java).toList()
            reader.close()
            return listUser
        }

        fun findUser(userName: String) =
            getUsers().find {
                it.userName == userName
            }

        fun addUser(user: Client) {
            val listClient = getUsers().toMutableList()
            listClient.add(user)
            mapper.writeValue(dataClients, listClient.toList())
            val reader = FileReader(rolesUser, Charsets.UTF_8)
            val listUserWithRole: MutableList<UserAndRole> =
                gson.fromJson(reader, Array<UserAndRole>::class.java).toMutableList()
            listUserWithRole.add(UserAndRole(user.userName, "user"))
            mapper.writeValue(rolesUser, listUserWithRole)
            reader.close()
        }

        fun checkUserForPresence(
            userName: String,
            password: String,
        ) = getUsers().find {
            it.userName == userName &&
                it.password == password
        }

        fun checkUserForPresenceByUserName(userName: String): UserSession? {
            val user =
                getUsers().find {
                    it.userName == userName
                }
            return if (user != null) {
                UserSession(user.name, user.userName)
            } else {
                null
            }
        }

        fun createHashPassword(
            password: String,
            webConfig: WebConfig,
        ): String {
            val messageDigest = MessageDigest.getInstance("SHA-384")
            val data = (webConfig.authSalt + password).encodeToByteArray()
            val digest = messageDigest.digest(data)
            return String(digest, Charsets.UTF_8)
        }
    }

    override fun toString(): String {
        return "$name зарегестриван: $registration\nРецепты:$recipes"
    }
}
