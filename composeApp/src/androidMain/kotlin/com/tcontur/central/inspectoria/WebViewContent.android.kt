package com.tcontur.central.inspectoria

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
actual fun WebViewContent(
    url: String,
    modifier: Modifier,
    onPageFinished: (url: String) -> Unit,
    onError: (description: String) -> Unit
) {
    val context = LocalContext.current

    // Holds the WebView PermissionRequest while we wait for the OS dialog result.
    val pendingWebPermission = remember { mutableStateOf<PermissionRequest?>(null) }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val anyGranted = results.values.any { it }
        pendingWebPermission.value?.let { req ->
            if (anyGranted) req.grant(req.resources) else req.deny()
            pendingWebPermission.value = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory  = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled                = true
                    domStorageEnabled                = true
                    allowFileAccess                  = true
                    loadWithOverviewMode             = true
                    useWideViewPort                  = true
                    builtInZoomControls              = false
                    displayZoomControls              = false
                    mediaPlaybackRequiresUserGesture = false
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        onPageFinished(url)
                    }
                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        if (request.isForMainFrame) onError(error.description.toString())
                    }
                }

                webChromeClient = object : WebChromeClient() {

                    override fun onPermissionRequest(request: PermissionRequest) {
                        val cameraOk = ContextCompat.checkSelfPermission(
                            ctx, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        val audioOk = ContextCompat.checkSelfPermission(
                            ctx, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        val needCam = request.resources.contains(
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        ) && !cameraOk

                        val needAudio = request.resources.contains(
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        ) && !audioOk

                        if (!needCam && !needAudio) {
                            // OS permissions already granted – allow immediately
                            request.grant(request.resources)
                        } else {
                            // Keep the request alive until the OS dialog returns
                            pendingWebPermission.value = request
                            val toRequest = buildList {
                                if (needCam)   add(Manifest.permission.CAMERA)
                                if (needAudio) add(Manifest.permission.RECORD_AUDIO)
                            }.toTypedArray()
                            cameraPermLauncher.launch(toRequest)
                        }
                    }

                    // Grant geolocation automatically (manifest already declares the permission)
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String,
                        callback: GeolocationPermissions.Callback
                    ) {
                        callback.invoke(origin, true, false)
                    }
                }

                loadUrl(url)
            }
        },
        update = { /* URL changes are not expected after initial load */ }
    )
}
