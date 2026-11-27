package com.example.workmanagement

import com.example.workmanagement.navigation.ConnectionSession
import com.example.workmanagement.navigation.Person
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resumeWithException

internal object ConnectionsApi {
    private val client = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    private val baseUrl by lazy { BuildConfig.API_URL.toHttpUrl() }

    suspend fun signIn(email: String, password: String): ConnectionSession {
        val authorization = Credentials.basic(email, password, Charsets.UTF_8)
        val user = JSONObject(execute(request(authorization, "api/users/me")))
        return SignedInSession(user.getLong("id"), authorization)
    }

    private class SignedInSession(
        override val userId: Long,
        private val authorization: String
    ) : ConnectionSession {
        override suspend fun search(query: String): List<Person> =
            execute(request(authorization, "api/users/search", query)).toPeople()

        override suspend fun connections(): List<Person> =
            execute(request(authorization, "api/connections/user/$userId")).toPeople()

        override suspend fun connect(receiverId: Long) {
            val body = JSONObject().put("receiverId", receiverId).toString()
                .toRequestBody("application/json".toMediaType())
            execute(request(authorization, "api/connections/connect").newBuilder().post(body).build())
        }
    }

    private fun request(authorization: String, path: String, query: String? = null): Request {
        val url = baseUrl.newBuilder().addPathSegments(path)
        if (query != null) url.addQueryParameter("query", query)
        return Request.Builder().url(url.build()).header("Authorization", authorization).build()
    }

    private suspend fun execute(request: Request): String = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(IOException("Could not reach the server. Check your connection.", e))
            }

            override fun onResponse(call: Call, response: Response) {
                val result = runCatching {
                    response.use {
                        if (!it.isSuccessful) throw IOException(
                            when (it.code) {
                                400 -> "The server rejected this request."
                                401 -> "Check your email and password."
                                403 -> "You don’t have access to this request."
                                404 -> "This user could not be found."
                                409 -> "This connection already exists."
                                else -> "The request failed. Please try again."
                            }
                        )
                        it.body?.string().orEmpty()
                    }
                }
                continuation.resumeWith(result)
            }
        })
    }

    private fun String.toPeople(): List<Person> {
        val json = JSONArray(this)
        return List(json.length()) { index ->
            val person = json.getJSONObject(index)
            Person(person.getLong("id"), person.getString("name"), person.optBoolean("connected"))
        }
    }
}
