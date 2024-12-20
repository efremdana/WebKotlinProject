package ru.ac.uniyar

import com.google.gson.Gson
import ru.ac.uniyar.domain.Cookbook
import ru.ac.uniyar.domain.Recipe
import java.io.File
import java.io.FileReader

private val cookbookData = File("data-to-cookbook.json")

fun readCookbook(): Cookbook {
    val gson = Gson()
    val reader = FileReader(cookbookData, Charsets.UTF_8)
    val listRecipe: List<Recipe> = gson.fromJson(reader, Array<Recipe>::class.java).toList()
    reader.close()
    return Cookbook(listRecipe.toMutableList())
}
