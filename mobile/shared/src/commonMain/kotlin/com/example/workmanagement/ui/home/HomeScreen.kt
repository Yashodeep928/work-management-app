package com.example.workmanagement.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workmanagement.navigation.ConnectionSession
import com.example.workmanagement.navigation.Person
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(session: ConnectionSession, onSignOut: () -> Unit) {
    var query by remember(session) { mutableStateOf("") }
    var results by remember(session) { mutableStateOf<List<Person>>(emptyList()) }
    var searching by remember(session) { mutableStateOf(false) }
    var searchError by remember(session) { mutableStateOf<String?>(null) }
    var matchesReady by remember(session) { mutableStateOf(false) }
    var selected by remember(session) { mutableStateOf<Person?>(null) }
    var connecting by remember(session) { mutableStateOf(false) }
    var connectError by remember(session) { mutableStateOf<String?>(null) }

    var connections by remember(session) { mutableStateOf<List<Person>>(emptyList()) }
    var loading by remember(session) { mutableStateOf(true) }
    var listError by remember(session) { mutableStateOf<String?>(null) }
    var refresh by remember(session) { mutableStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session, refresh) {
        loading = true
        listError = null
        try {
            connections = session.connections()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            listError = error.message ?: "Could not load connections."
        } finally {
            loading = false
        }
    }

    val prefix = query.trim()
    val searchInput = query
    LaunchedEffect(session, searchInput) {
        if (prefix.isEmpty()) return@LaunchedEffect
        delay(300)
        if (query != searchInput) return@LaunchedEffect
        searching = true
        try {
            val matches = session.search(prefix)
            if (query == searchInput) {
                results = matches
                matchesReady = true
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (query == searchInput) searchError = error.message ?: "Could not search for users."
        } finally {
            if (query == searchInput) searching = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Home", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onSignOut, enabled = !connecting) { Text("Sign out") }
            }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    results = emptyList()
                    searchError = null
                    matchesReady = false
                    searching = false
                },
                label = { Text("Search by name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searching) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                searchError?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
                if (matchesReady && results.isEmpty()) item { Text("No matching users.") }
                items(results, key = { "match-${it.id}" }) { person ->
                    val connected = person.connected || connections.any { it.id == person.id }
                    val showConfirmation = {
                        connectError = null
                        selected = person
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            person.name,
                            Modifier.weight(1f)
                                .clickable(enabled = !connected, onClick = showConfirmation)
                                .padding(vertical = 12.dp)
                        )
                        if (connected) {
                            Text("Connected")
                        } else {
                            TextButton(onClick = showConfirmation) { Text("Connect") }
                        }
                    }
                }

                item { Text("My Connections", style = MaterialTheme.typography.titleLarge) }
                if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                listError?.let { message ->
                    item {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { refresh++ }) { Text("Retry") }
                    }
                }
                if (!loading && listError == null && connections.isEmpty()) {
                    item { Text("You don’t have any connections yet.") }
                }
                items(connections, key = { "connection-${it.id}" }) { person ->
                    ListItem(
                        headlineContent = { Text(person.name) },
                        supportingContent = { Text("User ID ${person.id}") }
                    )
                }
            }
        }
    }

    selected?.let { person ->
        AlertDialog(
            onDismissRequest = { if (!connecting) selected = null },
            title = { Text("Connect with ${person.name}?") },
            text = {
                Column {
                    Text("Do you want to connect with this user?")
                    connectError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }, enabled = !connecting) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(
                    enabled = !connecting,
                    onClick = {
                        if (connecting) return@TextButton
                        connecting = true
                        connectError = null
                        scope.launch {
                            try {
                                session.connect(person.id)
                                selected = null
                                query = ""
                                results = emptyList()
                                searchError = null
                                matchesReady = false
                                searching = false
                                refresh++
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (error: Exception) {
                                connectError = error.message ?: "Could not connect with ${person.name}."
                            } finally {
                                connecting = false
                            }
                            snackbar.showSnackbar(connectError ?: "Connected with ${person.name} successfully!")
                        }
                    }
                ) { Text(if (connecting) "Connecting…" else "Connect") }
            }
        )
    }
}
