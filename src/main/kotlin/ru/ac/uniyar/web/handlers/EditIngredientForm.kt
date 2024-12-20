package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.lens.FormField
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.domain.IngredientUnit
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.models.EditIngredientFormVM
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession

private val nameField = FormField.string().required("name")
private val unitField = FormField.string().required("unit")
private val numberField = FormField.int().required("number")
private val ingredientForm = Body.webForm(Validator.Feedback, nameField, unitField, numberField).toLens()

class ShowEditIngredientForm(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val numberRecipe = request.path("number")?.toIntOrNull()
        val recipe = numberRecipe?.let { cookbook.getRecipeByNumber(it) }
        val indexIngredient = request.path("indexIngredient")?.toIntOrNull()
        val ingredient =
            recipe?.getIngredientByIndex(indexIngredient)
                ?: return Response(Status.NOT_FOUND).body(renderer(ErrorPageVM(request.uri)))
        val model =
            EditIngredientFormVM(
                WebForm(),
                IngredientUnit.values().toList(),
                ingredient,
                listOf(),
                user,
                permissions,
            )
        return Response(Status.OK).body(renderer(model))
    }
}

class EditIngredientHandler(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val webForm = ingredientForm(request)
        val validateForm = ValidatingForm()
        val recipeNumber = request.path("number")?.toIntOrNull()
        val recipe = recipeNumber?.let { cookbook.getRecipeByNumber(it) }
        val indexIngredient = request.path("indexIngredient")?.toIntOrNull()
        if (recipe == null || indexIngredient == null) {
            return Response(Status.NOT_FOUND).body(renderer(ErrorPageVM(request.uri)))
        }
        val name = nameField(webForm)
        val type = formUnitIngredient(unitField(webForm))
        val number = numberField(webForm)
        val ingredient = validateForm.validateIngredient(name, type, number)
        if (webForm.errors.isNotEmpty() || ingredient == null) {
            validateForm.addAllErrorWebForm(webForm)
            val model =
                EditIngredientFormVM(
                    webForm,
                    IngredientUnit.values().toList(),
                    recipe.listIngredient[indexIngredient],
                    validateForm.getErrorsList(),
                    user,
                    permissions,
                )
            return Response(Status.BAD_REQUEST).body(renderer(model))
        }
        recipe.editIngredient(ingredient, indexIngredient)
        Recipe.replaceRecipeToDatabase(recipe)
        return Response(Status.FOUND).header("Location", "/cookbook/$recipeNumber")
    }
}
