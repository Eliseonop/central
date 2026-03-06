package com.tcontur.central.inspectoria.iniciar.qr

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSArray
import platform.Foundation.NSObject
import platform.QuartzCore.CALayer
import platform.UIKit.*
import platform.darwin.*

@OptIn(ExperimentalForeignApi::class)
private class QrDelegate(
    private val onScanned: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: NSArray,
        fromConnection: AVCaptureConnection
    ) {
        val obj = (0 until didOutputMetadataObjects.count.toInt())
            .mapNotNull { didOutputMetadataObjects.objectAtIndex(it.toULong()) as? AVMetadataMachineReadableCodeObject }
            .firstOrNull()
        obj?.stringValue?.let { value ->
            dispatch_async(dispatch_get_main_queue()) { onScanned(value) }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosQrScanner(
    private val onQrScanned: (String) -> Unit,
    private val onError:     (String) -> Unit
) {
    val view = UIView()
    private var session:      AVCaptureSession? = null
    private var delegate:     QrDelegate?       = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null

    fun start() {
        val s = AVCaptureSession()
        session = s

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device == null) { onError("No se encontró cámara"); return }

        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                as? AVCaptureDeviceInput
        if (input == null) { onError("No se pudo acceder a la cámara"); return }

        if (s.canAddInput(input)) s.addInput(input)

        val output = AVCaptureMetadataOutput()
        if (s.canAddOutput(output)) {
            s.addOutput(output)
            val del = QrDelegate { raw ->
                session?.stopRunning()
                onQrScanned(raw)
            }
            delegate = del
            output.setMetadataObjectsDelegate(del, dispatch_get_main_queue())
            output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        }

        val layer = AVCaptureVideoPreviewLayer(session = s)
        layer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.frame = view.bounds
        view.layer.insertSublayer(layer, atIndex = 0u)
        previewLayer = layer

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            s.startRunning()
        }
    }

    fun stop() {
        session?.stopRunning()
        previewLayer?.removeFromSuperlayer()
        session  = null
        delegate = null
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerView(
    onQrScanned: (String) -> Unit,
    onError:     (String) -> Unit,
    modifier:    Modifier
) {
    val scanner = remember { IosQrScanner(onQrScanned, onError) }

    DisposableEffect(Unit) {
        scanner.start()
        onDispose { scanner.stop() }
    }

    UIKitView<UIView>(
        factory  = { scanner.view },
        modifier = modifier
    )
}