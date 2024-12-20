package ru.ac.uniyar.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int
import org.http4k.lens.string

data class WebConfig(
    val authSalt: String,
    val webPort: Int,
    val saltJWT: String,
) {
    companion object {
        val authSaltLens = EnvironmentKey.string().required("auth.salt", "Authentication salt")
        val webPortLens = EnvironmentKey.int().required("web.port", "Application web port")
        val saltJWTLens = EnvironmentKey.string().required("secretSalt.JWT", "Secret string for JWT")

        fun formWebConfig(environment: Environment): WebConfig {
            val authSalt = authSaltLens(environment)
            val port = webPortLens(environment)
            val saltJWT = saltJWTLens(environment)
            return WebConfig(authSalt, port, saltJWT)
        }
    }
}
