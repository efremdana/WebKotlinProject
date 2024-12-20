package ru.ac.uniyar

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.RequestContextKey
import org.http4k.template.PebbleTemplates
import org.http4k.template.TemplateRenderer
import org.junit.jupiter.api.Test
import ru.ac.uniyar.models.MainPageVM
import ru.ac.uniyar.web.handlers.MainPageHandler
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

class WebAppTest {
    private val renderer: TemplateRenderer = PebbleTemplates().HotReload("src/main/resources")

    @Test
    fun `Display main page`() {
        val permission = Permissions()
        val expectedMainPage = renderer(MainPageVM(null, permission))
        val contexts = RequestContexts()
        val key = RequestContextKey.optional<UserSession>(contexts)
        val access = RequestContextKey.required<Permissions>(contexts)
        val request = Request(GET, "/").with(access of permission)
        val handler = MainPageHandler(renderer, key, access)
        val response = handler(request)

        response shouldHaveStatus Status.OK
        response shouldHaveBody expectedMainPage
    }
}
