package com.hornet.movies.data.model.person

data class Credits(private val cast: List<Person> = listOf(), val crew: List<Person> = listOf()) {
    private val ACTOR_COUNT = 3
    val actors: List<Person> = cast.take(ACTOR_COUNT)
    val director: Person? = crew.firstOrNull { it.department == "Directing" }
}