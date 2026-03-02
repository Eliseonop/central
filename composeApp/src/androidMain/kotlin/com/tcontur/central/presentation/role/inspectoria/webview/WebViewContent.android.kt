package com.tcontur.central.presentation.role.inspectoria.webview

import android.graphics.Bitmap
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun WebViewContent(
    url: String,
    modifier: Modifier,
    onPageFinished: (url: String) -> Unit,
    onError: (description: String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory  = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled     = true
                    domStorageEnabled     = true
                    allowFileAccess       = true
                    loadWithOverviewMode  = true
                    useWideViewPort       = true
                    builtInZoomControls   = false
                    displayZoomControls   = false
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
                        if (request.isForMainFrame) {
                            onError(error.description.toString())
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    // Grant camera and mic permissions automatically
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources)
                    }

                    // Grant geolocation automatically (manifest already declares permission)
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
