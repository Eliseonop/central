package com.tcontur.central.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
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

        // ── Global 401 guard ──────────────────────────────────────────────────
        // If any request returns 401 (token inválido), notify SessionEventBus
        // so the navigation layer clears the session and forces Login.
        // Throwing here prevents runCatching{}.body<T>() from crashing trying
        // to deserialize the error JSON as the expected response type.
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value == 401) {
                    println("[TCONTUR][HTTP] ⚠️ 401 Unauthorized detectado — URL: ${response.call.request.url}")
                    println("[TCONTUR][HTTP] 🔔 Notificando SessionEventBus → logout automático")
                    SessionEventBus.notifyUnauthorized()
                    throw ResponseException(response, "Token inválido – sesión expirada")
                }
            }
        }
    }
}
