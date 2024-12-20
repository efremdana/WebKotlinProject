package ru.ac.uniyar

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.NoOp
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.http4k.template.PebbleTemplates
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.config.WebConfig
import ru.ac.uniyar.config.readConfiguration
import ru.ac.uniyar.domain.Client
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.web.handlers.AdditionNewUser
import ru.ac.uniyar.web.handlers.AuthUsersHandler
import ru.ac.uniyar.web.handlers.AuthorViewHandler
import ru.ac.uniyar.web.handlers.CookbookViewHandler
import ru.ac.uniyar.web.handlers.CreateNewIngredient
import ru.ac.uniyar.web.handlers.CreateNewRecipe
import ru.ac.uniyar.web.handlers.DeleteIngredientHandler
import ru.ac.uniyar.web.handlers.EditIngredientHandler
import ru.ac.uniyar.web.handlers.EditRecipeHandler
import ru.ac.uniyar.web.handlers.EditRoleFormViewHandler
import ru.ac.uniyar.web.handlers.EditRoleUserForm
import ru.ac.uniyar.web.handlers.ListUserViewHandler
import ru.ac.uniyar.web.handlers.LoginFormViewHandler
import ru.ac.uniyar.web.handlers.LogoutUserHandler
import ru.ac.uniyar.web.handlers.MainPageHandler
import ru.ac.uniyar.web.handlers.NewUserFormViewHandler
import ru.ac.uniyar.web.handlers.RecipeViewHandler
import ru.ac.uniyar.web.handlers.RegistrationNewUser
import ru.ac.uniyar.web.handlers.RegistrationViewHandler
import ru.ac.uniyar.web.handlers.RemoveRecipeHandler
import ru.ac.uniyar.web.handlers.ShowDeleteIngredientForm
import ru.ac.uniyar.web.handlers.ShowDeleteRecipeForm
import ru.ac.uniyar.web.handlers.ShowEditIngredientForm
import ru.ac.uniyar.web.handlers.ShowEditRecipeForm
import ru.ac.uniyar.web.handlers.ShowNewIngredientForm
import ru.ac.uniyar.web.handlers.ShowNewRecipeForm
import ru.ac.uniyar.web.session.JwtTools
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

fun router(
    cookbook: Cookbook,
    renderer: TemplateRenderer,
    webConfig: WebConfig,
    key: BiDiLens<Request, UserSession?>,
    access: RequestContextLens<Permissions>,
) = routes(
    "/" bind GET to MainPageHandler(renderer, key, access),
    "/cookbook" bind GET to CookbookViewHandler(cookbook, renderer, key, access),
    "/users" bind GET to
        permissionFilter(access, Permissions::canListUsers)
            .then(ListUserViewHandler(renderer, key, access)),
    "/users/{user}" bind GET to
        permissionFilter(access, Permissions::canListUsers)
            .then(EditRoleFormViewHandler(renderer, key, access)),
    "/users/{user}" bind POST to
        permissionFilter(access, Permissions::canListUsers)
            .then(EditRoleUserForm(renderer)),
    "/new-user" bind GET to
        permissionFilter(access, Permissions::canAddUser)
            .then(NewUserFormViewHandler(renderer, access, key)),
    "/new-user" bind POST to
        permissionFilter(access, Permissions::canAddUser)
            .then(AdditionNewUser(renderer, webConfig, access, key)),
    "/cookbook/new-recipe-form" bind GET to
        permissionFilter(access, Permissions::canAddRecipe)
            .then(ShowNewRecipeForm(renderer, key, access)),
    "/cookbook/new-recipe-form" bind POST to
        permissionFilter(access, Permissions::canAddRecipe)
            .then(CreateNewRecipe(cookbook, renderer, key, access)),
    "/registration" bind GET to RegistrationViewHandler(renderer, access, key),
    "/registration" bind POST to RegistrationNewUser(renderer, webConfig, access, key),
    "/login" bind GET to LoginFormViewHandler(renderer, key, access),
    "/login" bind POST to AuthUsersHandler(renderer, webConfig, key, access),
    "/logout" bind GET to LogoutUserHandler(),
    "/cookbook/{number}" bind GET to RecipeViewHandler(cookbook, renderer, access, key),
    "/cookbook/{number}/new-ingredient-form" bind GET to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(ShowNewIngredientForm(renderer, key, access)),
    "/cookbook/{number}/new-ingredient-form" bind POST to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(CreateNewIngredient(cookbook, renderer, key, access)),
    "/cookbook/{number}/edit" bind GET to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(ShowEditRecipeForm(cookbook, renderer, key, access)),
    "/cookbook/{number}/edit" bind POST to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(EditRecipeHandler(cookbook, renderer, key, access)),
    "/cookbook/{number}/{indexIngredient}/edit-ingredient-form" bind GET to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(ShowEditIngredientForm(cookbook, renderer, key, access)),
    "/cookbook/{number}/{indexIngredient}/edit-ingredient-form" bind POST to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(EditIngredientHandler(cookbook, renderer, key, access)),
    "/cookbook/{number}/{indexIngredient}/delete-ingredient-form" bind GET to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(ShowDeleteIngredientForm(cookbook, renderer, key, access)),
    "/cookbook/{number}/{indexIngredient}/delete-ingredient-form" bind POST to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(DeleteIngredientHandler(cookbook, renderer, key, access)),
    "/cookbook/{number}/delete" bind GET to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(ShowDeleteRecipeForm(cookbook, renderer, key, access)),
    "/cookbook/{number}/delete" bind POST to
        permissionFilter(access, Permissions::canEditAndDeleteRecipe)
            .then(RemoveRecipeHandler(cookbook, renderer, key, access)),
    "/author/{userName}" bind GET to AuthorViewHandler(cookbook, renderer, key, access),
    "/{error}" bind GET to { request ->
        Response(Status.NOT_FOUND).body(renderer(ErrorPageVM(request.uri)))
    },
    static(ResourceLoader.Classpath("ru/ac/uniyar")),
)

fun authenticationUserFilter(
    key: BiDiLens<Request, UserSession?>,
    webConfig: WebConfig,
) = Filter { next ->
    { request ->
        val jwtTools = JwtTools(webConfig.saltJWT, "Cookb00k")
        val token = request.cookie("auth")?.value
        val userName = token?.let { jwtTools.verificationJWT(token) }
        val userSession = userName?.let { Client.checkUserForPresenceByUserName(it) }
        if (userSession == null) {
            next(request)
        } else {
            next(request.with(key of userSession))
        }
    }
}

fun identifyingUserRoleFilter(
    key: BiDiLens<Request, UserSession?>,
    access: RequestContextLens<Permissions>,
) = Filter { next ->
    { request ->
        val user = key(request)
        val permission = Permissions.identifyingUserRole(user?.userName)
        next(request.with(access of permission))
    }
}

fun permissionFilter(
    permissionLens: RequestContextLens<Permissions>,
    canUse: (Permissions) -> Boolean,
) = Filter { next ->
    { request ->
        val permissions = permissionLens(request)
        if (!canUse(permissions)) {
            Response(Status.FORBIDDEN)
        } else {
            next(request)
        }
    }
}

fun main() {
    val webConfig = readConfiguration()
    val cookBook = readCookbook()
    val renderer: TemplateRenderer = PebbleTemplates().HotReload("src/main/resources")
    val contexts = RequestContexts()
    val key = RequestContextKey.optional<UserSession>(contexts)
    val access = RequestContextKey.required<Permissions>(contexts)
    val app =
        ServerFilters.InitialiseRequestContext(contexts)
            .then(authenticationUserFilter(key, webConfig))
            .then(identifyingUserRoleFilter(key, access))
            .then(router(cookBook, renderer, webConfig, key, access))
    val printingApp: HttpHandler = Filter.NoOp.then(app)
    val server = printingApp.asServer(Netty(webConfig.webPort)).start()
    println("Server started on http://localhost:" + server.port())
}
