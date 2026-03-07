package com.tcontur.central.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    println("[KTOR] $message")
                }
            }
        }

        // ── Global HTTP error guard ───────────────────────────────────────────
        // Intercepts all non-2xx responses BEFORE .body<T>() is called, so
        // Ktor never tries to deserialize an error JSON as the success type
        // (which would produce a confusing JsonConvertException).
        HttpResponseValidator {
            validateResponse { response ->
                val status = response.status.value
                if (status == 401) {
                    println("[TCONTUR][HTTP] ⚠️ 401 Unauthorized — URL: ${response.call.request.url}")
                    SessionEventBus.notifyUnauthorized()
                    throw ResponseException(response, "Token inválido – sesión expirada")
                }
                if (status !in 200..299) {
                    val body = response.bodyAsText()
                    println("[TCONTUR][HTTP] ⚠️ HTTP $status — URL: ${response.call.request.url}")
                    println("[TCONTUR][HTTP] ⚠️ Body: $body")
                    throw ResponseException(response, "Error $status: $body")
                }
            }
        }
    }
}
