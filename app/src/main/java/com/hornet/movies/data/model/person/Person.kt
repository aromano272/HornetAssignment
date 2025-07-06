package com.hornet.movies.data.model.person

import com.hornet.movies.util.pathToUrl

data class Person(
    val id: Int = 0,
    val name: String = "",
    val department: String? = "",
    private val profile_path: String? = "",
    val image: String = profile_path.pathToUrl(),
)