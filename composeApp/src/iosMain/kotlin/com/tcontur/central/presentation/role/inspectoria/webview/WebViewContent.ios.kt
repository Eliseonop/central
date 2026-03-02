package com.tcontur.central.presentation.role.inspectoria.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebViewContent(
    url: String,
    modifier: Modifier,
    onPageFinished: (url: String) -> Unit,
    onError: (description: String) -> Unit
) {
    UIKitView(
        modifier = modifier,
        factory  = {
            val config = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
            }
            val webView = WKWebView(frame = kotlinx.cinterop.CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config)

            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl != null) {
                val request = NSMutableURLRequest.requestWithURL(nsUrl)
                webView.loadRequest(request)
            }

            webView
        },
        update = { /* no dynamic updates needed */ }
    )
}
