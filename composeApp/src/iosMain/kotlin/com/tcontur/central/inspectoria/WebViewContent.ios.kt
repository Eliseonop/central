package com.tcontur.central.inspectoria

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
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
    UIKitView<WKWebView>(
        modifier = modifier,
        factory = {
            val config = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
            }
            val webView = WKWebView(frame = CGRectZero, configuration = config)
            NSURL.URLWithString(url)?.let {
                webView.loadRequest(NSMutableURLRequest.requestWithURL(it))
            }
            webView
        },
        update = {}
    )
}