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
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.domain.TypeDish
import ru.ac.uniyar.models.EditRecipeFormVM
import ru.ac.uniyar.models.ErrorPageVM
import ru.ac.uniyar.web.session.Permissions
import ru.ac.uniyar.web.session.UserSession
import java.time.LocalDate

private val nameField = FormField.string().required("name")
private val typeField = FormField.string().required("type")
private val cookingTimeField = FormField.int().required("time")
private val descriptionField = FormField.string().required("description")
private val stepsField = FormField.string().required("steps")
private val recipeForm =
    Body.webForm(
        Validator.Feedback,
        nameField,
        typeField,
        cookingTimeField,
        descriptionField,
        stepsField,
    ).toLens()

class ShowEditRecipeForm(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val index = request.path("number")?.toIntOrNull()
        val recipe =
            index?.let { cookbook.getRecipeByNumber(it) }
                ?: return Response(Status.NOT_FOUND).body(renderer(ErrorPageVM(request.uri)))
        val model =
            EditRecipeFormVM(
                recipe,
                WebForm(),
                TypeDish.values().toList(),
                recipe.getStepsByString(),
                listOf(),
                user,
                permissions,
            )
        return Response(Status.OK).body(renderer(model))
    }
}

class EditRecipeHandler(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val permissions = access(request)
        val user = key(request)
        val webForm = recipeForm(request)
        val validateForm = ValidatingForm()
        val index = request.path("number")?.toIntOrNull()
        val recipeOld =
            index?.let { cookbook.getRecipeByNumber(it) }
                ?: return Response(Status.NOT_FOUND).body(renderer(ErrorPageVM(request.uri)))
        if (webForm.errors.isNotEmpty()) {
            validateForm.addAllErrorWebForm(webForm)
            val model =
                EditRecipeFormVM(
                    recipeOld,
                    webForm,
                    TypeDish.values().toList(),
                    recipeOld.getStepsByString(),
                    validateForm.getErrorsList(),
                    user,
                    permissions,
                )
            return Response(Status.BAD_REQUEST).body(renderer(model))
        }
        val name = nameField(webForm)
        val type = formTypeDish(typeField(webForm))
        val cookingTime = cookingTimeField(webForm)
        val description = descriptionField(webForm)
        val steps = stepsField(webForm)
        val date = LocalDate.now().toString()
        val author = recipeOld.author
        val listIngredient = recipeOld.listIngredient
        val recipe =
            validateForm.validateRecipe(name, type, cookingTime, index, description, date, author)
                ?: return Response(Status.BAD_REQUEST).body(
                    renderer(
                        EditRecipeFormVM(
                            recipeOld,
                            webForm,
                            TypeDish.values().toList(),
                            steps,
                            validateForm.getErrorsList(),
                            user,
                            permissions,
                        ),
                    ),
                )
        recipe.addCookingSteps(steps.split("\n"))
        recipe.addAllIngredients(listIngredient)
        cookbook.editRecipe(recipe)
        Recipe.replaceRecipeToDatabase(recipe)
        return Response(Status.FOUND).header("Location", "/cookbook/$index")
    }
}
