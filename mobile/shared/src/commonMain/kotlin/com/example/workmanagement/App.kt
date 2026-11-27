package com.example.workmanagement

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.workmanagement.navigation.AppNavigation
import com.example.workmanagement.navigation.ConnectionSession

@Composable
fun App(signIn: (suspend (String, String) -> ConnectionSession)? = null) {
    MaterialTheme {
        if (signIn == null) {
            Text("User connections are available on Android.")
        } else {
            AppNavigation(signIn)
        }
    }
}
