package com.tcontur.central

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform