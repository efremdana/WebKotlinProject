package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.models.RecipeVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

class RecipeViewHandler(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val access: RequestContextLens<Permissions>,
    val key: BiDiLens<Request, UserSession?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val num = request.path("number")?.toIntOrNull()
        val recipe =
            num?.let {
                cookbook.getRecipeByNumber(it)
            } ?: return Response(Status.NOT_FOUND).body(renderer(ErrorPageVM(request.uri)))
        val model = RecipeVM(recipe, permissions, user)
        return Response(Status.OK).body(renderer(model))
    }
}
