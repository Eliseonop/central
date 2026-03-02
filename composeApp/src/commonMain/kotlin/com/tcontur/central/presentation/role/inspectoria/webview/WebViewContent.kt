package com.tcontur.central.presentation.role.inspectoria.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Cross-platform WebView composable.
 *
 * Concrete implementations:
 *   - androidMain: uses Android [android.webkit.WebView] via [AndroidView]
 *   - iosMain:     uses [WKWebView] via [UIKitView]
 */
@Composable
expect fun WebViewContent(
    url: String,
    modifier: Modifier = Modifier,
    onPageFinished: (url: String) -> Unit = {},
    onError: (description: String) -> Unit = {}
)
