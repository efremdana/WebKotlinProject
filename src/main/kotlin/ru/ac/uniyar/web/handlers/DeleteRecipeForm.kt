package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.models.DeleteRecipeFormVM
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

class ShowDeleteRecipeForm(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val number = request.path("number")?.toIntOrNull()
        val recipe =
            number?.let { cookbook.getRecipeByNumber(it) } ?: return Response(Status.NOT_FOUND).body(
                renderer(ErrorPageVM(request.uri)),
            )
        val model = DeleteRecipeFormVM(null, recipe, user, permissions)
        return Response(Status.OK).body(renderer(model))
    }
}

class RemoveRecipeHandler(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val checkRemove = request.form("checkRemove")
        val number = request.path("number")?.toIntOrNull()
        val recipe =
            number?.let { cookbook.getRecipeByNumber(it) } ?: return Response(Status.NOT_FOUND).body(
                renderer(ErrorPageVM(request.uri)),
            )
        return if (checkRemove.isNullOrEmpty()) {
            val model = DeleteRecipeFormVM("off", recipe, user, permissions)
            Response(Status.BAD_REQUEST).body(renderer(model))
        } else {
            cookbook.removeRecipe(number)
            Recipe.removeRecipeToDatabase(recipe)
            Response(Status.FOUND).header("Location", "/cookbook")
        }
    }
}
