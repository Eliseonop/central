package com.tcontur.central.inspectoria.iniciar.qr

/**
 * Representa un corte de boleto proveniente de ProtoCheckQr.
 * Se almacena en [QrDataHolder] para que [InspeccionViewModel] lo consuma.
 *
 *   fare        → boletoId  (from ProtoCheckQrTicket.fare)
 *   correlative → fin       (from ProtoCheckQrTicket.correlative)
 *   inicio      → 0         (no disponible en check_qr)
 */
data class QrCorte(
    val boletoId: Int,
    val inicio:   Int,
    val fin:      Int
)
