package com.tcontur.central.core.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private const val TAG = "TCONTUR_PERMS"

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun android.content.Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun android.content.Context.areStartupPermissionsGranted(): Boolean {
    val locationOk =
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
        hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    val notifOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        true
    }

    return locationOk && notifOk
}

private fun android.content.Context.missingStartupPermissions(): Array<String> =
    buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }.toTypedArray()

/** Logs the status of every startup permission. */
private fun android.content.Context.logPermissionStatus() {
    val fine    = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse  = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    val notif   = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                      hasPermission(Manifest.permission.POST_NOTIFICATIONS)
                  else true

    Log.d(TAG, "┌── Estado de permisos ──────────────────────")
    Log.d(TAG, "│  ACCESS_FINE_LOCATION    : ${if (fine)   "✅ CONCEDIDO" else "❌ DENEGADO"}")
    Log.d(TAG, "│  ACCESS_COARSE_LOCATION  : ${if (coarse) "✅ CONCEDIDO" else "❌ DENEGADO"}")
    Log.d(TAG, "│  POST_NOTIFICATIONS      : ${if (notif)  "✅ CONCEDIDO" else "❌ DENEGADO (API 33+)"}")
    Log.d(TAG, "└────────────────────────────────────────────")
}

// ── Actual implementation ──────────────────────────────────────────────────────

@Composable
actual fun rememberStartupPermissions(): StartupPermissionsState {
    val context = LocalContext.current

    var allGranted by remember {
        val granted = context.areStartupPermissionsGranted()
        Log.d(TAG, "rememberStartupPermissions() — verificación inicial")
        context.logPermissionStatus()
        if (granted) Log.d(TAG, "✅ Todos los permisos ya concedidos — se saltará la pantalla")
        else         Log.d(TAG, "⚠️ Faltan permisos — se mostrará la pantalla de permisos")
        mutableStateOf(granted)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        Log.d(TAG, "Resultado del diálogo de permisos:")
        results.forEach { (perm, granted) ->
            Log.d(TAG, "  ${perm.substringAfterLast('.')} → ${if (granted) "✅ CONCEDIDO" else "❌ DENEGADO"}")
        }
        allGranted = context.areStartupPermissionsGranted()
        Log.d(TAG, if (allGranted) "✅ Todos los permisos concedidos — continuando"
                   else            "❌ Aún faltan permisos")
    }

    return StartupPermissionsState(
        allGranted = allGranted,
        request = {
            val missing = context.missingStartupPermissions()
            Log.d(TAG, "Solicitando ${missing.size} permiso(s): ${missing.map { it.substringAfterLast('.') }}")
            if (missing.isEmpty()) {
                Log.d(TAG, "No hay permisos faltantes — marcando como concedidos")
                allGranted = true
            } else {
                launcher.launch(missing)
            }
        }
    )
}
