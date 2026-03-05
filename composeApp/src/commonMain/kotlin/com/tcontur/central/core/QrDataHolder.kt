package com.tcontur.central.core

import com.tcontur.central.inspectoria.iniciar.qr.QrCorte

/**
 * Koin singleton that bridges QR data from [IniciarInspeccionViewModel]
 * to [InspeccionViewModel].
 *
 * The iniciar screen stores QR cortes here before navigating to the active
 * inspection screen, which reads them once and then clears the holder.
 */
class QrDataHolder {
    /** Cortes from the last successful QR scan. Null if no QR was used. */
    var qrCortes: List<QrCorte>? = null

    fun clear() { qrCortes = null }
}
