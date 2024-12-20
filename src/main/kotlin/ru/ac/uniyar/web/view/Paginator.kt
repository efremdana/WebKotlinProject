package ru.ac.uniyar.web.view

import org.http4k.core.Uri
import ru.ac.uniyar.domain.Recipe
import ru.ac.uniyar.domain.TypeDish

class Paginator(
    val currentPage: Int,
    val numberPages: Int,
    val basePath: String,
    val typeDish: TypeDish?,
    val minTime: Int?,
    val maxTime: Int?,
    val listData: List<Recipe>,
) {
    val firstPage =
        Uri.of("$basePath?page=1&typeDish=${typeDish?.type}&minTime=$minTime&maxTime=$maxTime")

    val lastPage =
        Uri.of("$basePath?page=$numberPages&typeDish=${typeDish?.type}&minTime=$minTime&maxTime=$maxTime")

    fun hasPrevious(): String {
        return if (currentPage > 1) "" else "disabled"
    }

    fun hasNext(): String {
        return if (currentPage < numberPages && listData.isNotEmpty()) "" else "disabled"
    }

    fun getPreviousPage(): String {
        return "$basePath?page=${currentPage - 1}&typeDish=${typeDish?.type}&minTime=$minTime&maxTime=$maxTime"
    }

    fun getNextPage() = "$basePath?page=${currentPage + 1}&typeDish=${typeDish?.type}&minTime=$minTime&maxTime=$maxTime"
}
