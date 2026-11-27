package com.example.workmanagement.navigation

import androidx.compose.runtime.*
import com.example.workmanagement.ui.auth.LoginScreen
import com.example.workmanagement.ui.home.HomeScreen

interface ConnectionSession {
    val userId: Long
    suspend fun search(query: String): List<Person>
    suspend fun connections(): List<Person>
    suspend fun connect(receiverId: Long)
}

data class Person(val id: Long, val name: String, val connected: Boolean = false)

@Composable
fun AppNavigation(signIn: suspend (String, String) -> ConnectionSession) {
    var session by remember { mutableStateOf<ConnectionSession?>(null) }
    val currentSession = session
    if (currentSession == null) {
        LoginScreen(signIn, onSignedIn = { session = it })
    } else {
        HomeScreen(currentSession, onSignOut = { session = null })
    }
}
