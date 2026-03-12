package com.tcontur.central.inspectoria.iniciar.qr

import io.ktor.utils.io.core.String
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object QrParser {

    @OptIn(ExperimentalEncodingApi::class)
    fun parse(raw: String): QrData {
        if (raw.isBlank()) throw QrError.Empty
        return try {
            val parts = String(Base64.Default.decode(raw.trim())).split(",")
            if (parts.size != 3) throw QrError.InvalidFormat
            QrData(
                inspector = parts[0].toInt(),
                pin       = parts[1].toInt(),
                vehicle   = parts[2].toInt()
            )
        } catch (e: QrError) { throw e }
        catch (_: Exception) { throw QrError.InvalidFormat }
    }
}
