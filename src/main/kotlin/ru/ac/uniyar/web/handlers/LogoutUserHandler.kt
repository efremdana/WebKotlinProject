package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.invalidateCookie

class LogoutUserHandler : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(Status.FOUND).invalidateCookie("auth").header("Location", "/")
    }
}
