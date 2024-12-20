package ru.ac.uniyar.config

import org.http4k.cloudnative.env.Environment

private const val PORT_DEFAULT = 9000

val defaultEnv =
    Environment.defaults(
        WebConfig.webPortLens of PORT_DEFAULT,
    )
val appEnv =
    Environment.fromResource("ru/ac/uniyar/config/app.properties") overrides
        Environment.JVM_PROPERTIES overrides
        Environment.ENV overrides
        defaultEnv

fun readConfiguration(): WebConfig {
    return WebConfig.formWebConfig(appEnv)
}
