package com.example.workmanagement.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.workmanagement.navigation.ConnectionSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    signIn: suspend (String, String) -> ConnectionSession,
    onSignedIn: (ConnectionSession) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signingIn by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sign in", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                email, { email = it }, Modifier.fillMaxWidth(),
                enabled = !signingIn, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                password, { password = it }, Modifier.fillMaxWidth(),
                enabled = !signingIn, label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Button(
                enabled = !signingIn && email.isNotBlank() && password.isNotEmpty(),
                onClick = {
                    signingIn = true
                    scope.launch {
                        try {
                            val session = signIn(email.trim(), password)
                            password = ""
                            onSignedIn(session)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            snackbar.showSnackbar(error.message ?: "Could not sign in.")
                        } finally {
                            signingIn = false
                        }
                    }
                }
            ) {
                Text(if (signingIn) "Signing in…" else "Sign in")
            }
        }
    }
}
