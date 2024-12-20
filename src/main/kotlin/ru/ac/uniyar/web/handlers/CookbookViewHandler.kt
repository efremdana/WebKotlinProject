package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.domain.TypeDish
import ru.ac.uniyar.models.CookbookVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession
import ru.ac.uniyar.web.view.Paginator

class CookbookViewHandler(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val page =
            request
                .uri
                .queries()
                .findSingle("page")
                ?.toIntOrNull()
                ?: 1
        val typeDish =
            request
                .uri
                .queries()
                .findSingle("typeDish")
        val type = formTypeDish(typeDish)
        val minTime =
            request
                .uri
                .queries()
                .findSingle("minTime")
                ?.toIntOrNull()
        val maxTime =
            request
                .uri
                .queries()
                .findSingle("maxTime")
                ?.toIntOrNull()
        val listRecipeFilter = cookbook.recipesByFilter(type, minTime, maxTime)
        val listToPage = cookbook.recipesByNumberPage(page, 10, listRecipeFilter)
        val paginator =
            Paginator(
                page,
                cookbook.pageAmount(
                    10,
                    listRecipeFilter.toMutableList(),
                ),
                request
                    .uri
                    .path,
                type,
                minTime,
                maxTime,
                listToPage,
            )
        val model = CookbookVM(paginator, TypeDish.values().toList(), permissions, user)
        return Response(Status.OK).body(renderer(model))
    }
}
