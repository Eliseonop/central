package com.tcontur.central.inspectoria.iniciar.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
actual fun QrScannerView(
    onQrScanned: (String) -> Unit,
    onError:     (String) -> Unit,
    modifier:    Modifier
) {
    val context = LocalContext.current

    // ── Camera permission ─────────────────────────────────────────────────────
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) onError("Permiso de cámara denegado")
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Se requiere permiso de cámara", fontSize = 14.sp, color = Color.Gray)
                Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Conceder permiso")
                }
            }
        }
        return
    }

    // ── CameraX preview ───────────────────────────────────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView    = remember { PreviewView(context) }
    val executor       = remember { Executors.newSingleThreadExecutor() }
    var scanned        by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)

        future.addListener({
            val provider = future.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { ia ->
                    ia.setAnalyzer(executor) { proxy ->
                        if (!scanned) {
                            val media = proxy.image
                            if (media != null) {
                                val img = InputImage.fromMediaImage(
                                    media,
                                    proxy.imageInfo.rotationDegrees
                                )
                                scanner.process(img)
                                    .addOnSuccessListener { codes ->
                                        codes.firstOrNull()?.rawValue?.let { raw ->
                                            scanned = true
                                            onQrScanned(raw)
                                        }
                                    }
                                    .addOnCompleteListener { proxy.close() }
                            } else {
                                proxy.close()
                            }
                        } else {
                            proxy.close()
                        }
                    }
                }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                onError(e.message ?: "Error al iniciar la cámara")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            executor.shutdown()
            try { future.get().unbindAll() } catch (_: Exception) { }
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
