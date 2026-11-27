package com.example.workmanagement

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val localNetwork = "android.permission.ACCESS_LOCAL_NETWORK"
            val pendingGrant = remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }
            val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                pendingGrant.value?.complete(it)
            }
            App(signIn = { email, password ->
                if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 37 &&
                    checkSelfPermission(localNetwork) != PackageManager.PERMISSION_GRANTED
                ) {
                    val grant = CompletableDeferred<Boolean>()
                    pendingGrant.value = grant
                    try {
                        permission.launch(localNetwork)
                        if (!grant.await()) throw IOException("Allow local network access to reach the development server.")
                    } finally {
                        pendingGrant.value = null
                    }
                }
                ConnectionsApi.signIn(email, password)
            })
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(signIn = ConnectionsApi::signIn)
}
