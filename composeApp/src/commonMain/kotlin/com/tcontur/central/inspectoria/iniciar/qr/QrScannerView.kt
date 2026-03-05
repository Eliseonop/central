package com.tcontur.central.inspectoria.iniciar.qr

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific QR code camera scanner.
 *
 * - Android: CameraX + ML Kit Barcode Scanning
 * - iOS:     AVFoundation / AVCaptureSession
 *
 * Calls [onQrScanned] with the raw string value of the first QR code detected.
 * Calls [onError] with a human-readable message if the camera cannot start.
 *
 * The scanner fires [onQrScanned] once and then stops; the parent must unmount
 * and remount this composable (e.g. via `key(scanKey)`) to allow re-scanning.
 */
@Composable
expect fun QrScannerView(
    onQrScanned: (String) -> Unit,
    onError:     (String) -> Unit,
    modifier:    Modifier = Modifier
)
