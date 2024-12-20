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
import org.http4k.template.TemplateRenderer
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.domain.TypeDish
import ru.ac.uniyar.models.NewRecipeFormVM
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

class ShowNewRecipeForm(
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = key(request)
        val permissions = access(request)
        val model = NewRecipeFormVM(WebForm(), TypeDish.values().toList(), listOf(), user, permissions)
        return Response(Status.OK).body(renderer(model))
    }
}

class CreateNewRecipe(
    val cookbook: Cookbook,
    val renderer: TemplateRenderer,
    val key: BiDiLens<Request, UserSession?>,
    val access: RequestContextLens<Permissions>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = key(request)
        val permissions = access(request)
        val webForm = recipeForm(request)
        val validateForm = ValidatingForm()
        val name = nameField(webForm)
        val type = formTypeDish(typeField(webForm))
        val cookingTime = cookingTimeField(webForm)
        val description = descriptionField(webForm)
        val steps = stepsField(webForm)
        val date = LocalDate.now().toString()
        val author = user?.userName ?: ""
        val recipe =
            validateForm.validateRecipe(
                name,
                type,
                cookingTime,
                cookbook.recipes.size + 1,
                description,
                date,
                author,
            )
        if (webForm.errors.isNotEmpty() || recipe == null) {
            validateForm.addAllErrorWebForm(webForm)
            val model =
                NewRecipeFormVM(
                    webForm,
                    TypeDish.values().toList(),
                    validateForm.getErrorsList(),
                    user,
                    permissions,
                )
            return Response(Status.BAD_REQUEST).body(renderer(model))
        }
        recipe.addCookingSteps(steps.split("\n"))
        cookbook.addRecipe(recipe)
        Recipe.addRecipeToDatabase(recipe)
        return Response(Status.FOUND).header("Location", "/cookbook/${cookbook.recipes.size}")
    }
}

fun formTypeDish(type: String?) =
    when (type?.lowercase()) {
        "завтрак" -> TypeDish.BREAKFAST
        "первое" -> TypeDish.FIRST
        "второе" -> TypeDish.SECOND
        "десерт" -> TypeDish.DESSERT
        "салат" -> TypeDish.SALAD
        "напитки" -> TypeDish.DRINKS
        "выпечка" -> TypeDish.BAKERY
        else -> null
    }

fun recipeFormTypeField(type: String) =
    when (type.lowercase()) {
        "name" -> "название"
        "description" -> "описание"
        "type" -> "тип блюда"
        "time" -> "время готовки"
        "steps" -> "шаги приготовления"
        "nameAuthor" -> "ваше имя"
        else -> ""
    }
