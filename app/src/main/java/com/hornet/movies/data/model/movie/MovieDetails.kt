package com.hornet.movies.data.model.movie

import com.hornet.movies.data.model.meta.ProductionCompany
import com.hornet.movies.data.model.person.Credits
import com.imbaland.movies.domain.model.Person

data class MovieDetails(
    private val production_companies: List<ProductionCompany> = listOf(),
    private val credits: Credits = Credits(),
    val actors: List<Person> = credits.actors,
    val director: Person? = credits.director,
    val production_company: ProductionCompany? = production_companies.firstOrNull(),
)