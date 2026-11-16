package com.example.workmanagement

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform