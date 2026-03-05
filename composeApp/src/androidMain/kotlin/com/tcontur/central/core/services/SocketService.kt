package com.tcontur.central.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tcontur.Protocol
import com.tcontur.central.core.socket.ProtoSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.koin.android.ext.android.inject
import java.io.File
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

// WebSocket URL is built externally (by SocketLoadingViewModel) from the
// empresa's `compute` IP field:  ws://{ip}:22222?schema=android&gps=true&console=true

class SocketService : Service() {

    private val TAG = "SocketService"
    private val CHANNEL_ID = "SOCKET_SERVICE_CHANNEL"
    private val NOTIFICATION_ID = 1002

    private val protoSocketManager: ProtoSocketManager by inject()

    // Lazy-init Protocol with schema from assets
    private val proto: Protocol by lazy {
        val schemaName = "schema.json"
        val schemaFile = File(filesDir, schemaName)
        val json = assets.open(schemaName).bufferedReader().use { it.readText() }
        schemaFile.writeText(json)
        Protocol(false, schemaFile.absolutePath, null)
    }

    private var lastUrl: String? = null
    private val isConnecting     = AtomicBoolean(false)
    private val isReconnecting   = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)
    private val activeSocketId   = AtomicInteger(0)
    private val shouldReconnect  = AtomicBoolean(true)

    private val connectionMutex = Mutex()
    private var reconnectJob: Job? = null
    private var connectionJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var client: OkHttpClient? = null
    private var socket: WebSocket? = null
    private var messageCount: Long = 0
    private var connectionStartTime: Long = 0

    // ── Service lifecycle ─────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "🚀 SocketService creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        when (intent?.action) {
            ACTION_CONNECT -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_STICKY
                shouldReconnect.set(true)
                connect(url)
            }
            ACTION_DISCONNECT -> {
                shouldReconnect.set(false)
                disconnect()
            }
            ACTION_SEND -> {
                @Suppress("UNCHECKED_CAST")
                val data      = intent.getSerializableExtra(EXTRA_DATA) as? HashMap<String, Any?>
                val formatKey = intent.getStringExtra(EXTRA_FORMAT_KEY)
                if (data != null && formatKey != null) send(data, formatKey)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 SocketService destruido")
        shouldReconnect.set(false)
        serviceScope.cancel()
        cleanupResources()
    }

    // ── Connection ────────────────────────────────────────────────────────────

    private fun connect(url: String) {
        lastUrl = url
        connectionJob?.cancel()
        connectionJob = serviceScope.launch {
            connectionMutex.withLock {
                if (isConnecting.get()) return@withLock
                cleanupResources(forceIdIncrement = true)
                reconnectAttempts.set(0)
                connectionStartTime = System.currentTimeMillis()
                initiateConnection(url)
            }
        }
    }

    private fun initiateConnection(url: String) {
        if (!isConnecting.compareAndSet(false, true)) return
        try {
            val socketId = activeSocketId.get()
            Log.d(TAG, "🔄 Conectando [ID: $socketId] → $url")

            socket?.let { it.close(1000, "New connection"); socket = null }

            client = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()

            socket = client!!.newWebSocket(
                Request.Builder().url(url).build(),
                createListener(socketId)
            )

            // Connection timeout watchdog
            serviceScope.launch {
                delay(15_000)
                if (isConnecting.get() && activeSocketId.get() == socketId) {
                    Log.w(TAG, "⏱️ Timeout [ID: $socketId]")
                    isConnecting.set(false)
                    protoSocketManager.updateConnectionState(false)
                    protoSocketManager.emitError("Timeout conexión")
                    socket?.close(1000, "Timeout"); socket = null
                    if (shouldReconnect.get()) scheduleReconnect()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error conectando: ${e.message}")
            isConnecting.set(false)
            socket = null
            if (shouldReconnect.get()) scheduleReconnect()
        }
    }

    private fun createListener(socketId: Int): WebSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (activeSocketId.get() != socketId) { webSocket.close(1000, "Obsolete"); return }
            val elapsed = System.currentTimeMillis() - connectionStartTime
            Log.d(TAG, "✅ Conectado [ID: $socketId] en ${elapsed}ms")
            protoSocketManager.updateConnectionState(true)
            protoSocketManager.emitConnectionSuccess(true)
            isConnecting.set(false)
            isReconnecting.set(false)
            reconnectAttempts.set(0)
            reconnectJob?.cancel()
            messageCount = 0
            updateNotification()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (activeSocketId.get() != socketId) return
            Log.e(TAG, "❌ onFailure [ID: $socketId]: ${t.message}")
            protoSocketManager.updateConnectionState(false)
            isConnecting.set(false)
            protoSocketManager.emitError(t.message ?: "Error desconocido")
            if (shouldReconnect.get()) scheduleReconnect()
            updateNotification()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            if (activeSocketId.get() != socketId) return
            Log.w(TAG, "🔄 onClosing [ID: $socketId] $code: $reason")
            webSocket.close(code, reason)
            protoSocketManager.updateConnectionState(false)
            protoSocketManager.emitConnectionClosed(code, reason)
            if (shouldReconnect.get()) scheduleReconnect()
            updateNotification()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (activeSocketId.get() != socketId) return
            Log.d(TAG, "🔒 onClosed [ID: $socketId] $code: $reason | msgs: $messageCount")
            isConnecting.set(false)
            protoSocketManager.updateConnectionState(false)
            protoSocketManager.emitConnectionClosed(code, reason)
            if (shouldReconnect.get()) scheduleReconnect()
            updateNotification()
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            if (activeSocketId.get() != socketId) return
            messageCount++
            val raw = bytes.toByteArray()
            try {
                val result = proto.decode(raw, null)
                if (result != null) {
                    Log.d(TAG, "📨 [#$messageCount] header='${result.header}' data=${result.data}")
                    @Suppress("UNCHECKED_CAST")
                    protoSocketManager.emitMessageDecoded(
                        header = result.header,
                        data   = result.data as Map<String, Any>
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error decode: ${e.message}")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (activeSocketId.get() != socketId) return
            messageCount++
            Log.d(TAG, "📩 Texto #$messageCount [ID: $socketId]: $text")
        }
    }

    // ── Reconnect ─────────────────────────────────────────────────────────────

    private fun scheduleReconnect() {
        if (!shouldReconnect.get()) return
        val attempt = reconnectAttempts.incrementAndGet()
        if (!isReconnecting.compareAndSet(false, true)) return
        val delayMs = calculateDelay(attempt)
        Log.d(TAG, "🔄 Reconexión #$attempt en ${delayMs}ms")
        reconnectJob?.cancel()
        reconnectJob = serviceScope.launch {
            try {
                delay(delayMs)
                if (!isActive) { isReconnecting.set(false); return@launch }
                if (!protoSocketManager.isConnected.value && lastUrl != null && shouldReconnect.get()) {
                    isReconnecting.set(false)
                    connectionMutex.withLock {
                        if (shouldReconnect.get()) {
                            cleanupResources(forceIdIncrement = false)
                            initiateConnection(lastUrl!!)
                        }
                    }
                } else {
                    isReconnecting.set(false)
                }
            } catch (e: CancellationException) {
                isReconnecting.set(false)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error reconexión: ${e.message}")
                isReconnecting.set(false)
            }
        }
    }

    private fun calculateDelay(attempts: Int): Long =
        (3_000L + 2_000L * (attempts - 1)).coerceAtMost(60_000L)

    // ── Send ──────────────────────────────────────────────────────────────────

    private fun send(data: Map<String, Any?>, formatKey: String) {
        serviceScope.launch {
            val currentSocket = socket
            if (currentSocket == null || !protoSocketManager.isConnected.value) {
                Log.w(TAG, "[SEND] Socket no disponible")
                return@launch
            }
            try {
                Log.d(TAG, "[SEND] Enviando mensaje con formato '$formatKey': $data")
                val encoded = proto.encode(data, formatKey) ?: run {
                    Log.e(TAG, "[SEND] Encode falló para '$formatKey'")
                    return@launch
                }
                val ok = currentSocket.send(ByteString.of(*encoded))
                if (!ok) {
                    Log.e(TAG, "[SEND] send() retornó false — socket cerrado?")
                    protoSocketManager.updateConnectionState(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[SEND] Excepción: ${e.message}")
            }
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private fun disconnect() {
        serviceScope.launch {
            connectionMutex.withLock {
                shouldReconnect.set(false)
                socket?.close(1000, "Manual disconnect")
                cleanupResources()
            }
        }
    }

    private fun cleanupResources(forceIdIncrement: Boolean = false) {
        reconnectJob?.cancel()
        connectionJob?.cancel()
        reconnectJob = null
        connectionJob = null
        isConnecting.set(false)
        isReconnecting.set(false)
        socket?.let { runCatching { it.cancel() }; socket = null }
        client?.let {
            runCatching {
                it.dispatcher.cancelAll()
                it.dispatcher.executorService.shutdownNow()
                it.connectionPool.evictAll()
            }
            client = null
        }
        protoSocketManager.updateConnectionState(false)
        if (forceIdIncrement) activeSocketId.incrementAndGet()
        messageCount = 0
        updateNotification()
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Socket Service", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Maintains WebSocket connection in background" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val status = if (protoSocketManager.isConnected.value) "🟢 Conectado" else "🔴 Desconectado"
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Socket Central $status")
            .setContentText("Mensajes: $messageCount")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID, createNotification())
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        const val ACTION_CONNECT    = "com.tcontur.central.socket.CONNECT"
        const val ACTION_DISCONNECT = "com.tcontur.central.socket.DISCONNECT"
        const val ACTION_SEND       = "com.tcontur.central.socket.SEND"
        const val EXTRA_URL         = "extra_url"
        const val EXTRA_DATA        = "extra_data"
        const val EXTRA_FORMAT_KEY  = "extra_format_key"

        fun startConnection(context: Context, wsUrl: String) {
            val intent = Intent(context, SocketService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_URL, wsUrl)
            }
            context.startForegroundService(intent)
        }

        fun sendMessage(context: Context, data: HashMap<String, Any?>, formatKey: String) {
            context.startService(Intent(context, SocketService::class.java).apply {
                action = ACTION_SEND
                putExtra(EXTRA_DATA, data)
                putExtra(EXTRA_FORMAT_KEY, formatKey)
            })
        }

        fun disconnect(context: Context) {
            context.startService(Intent(context, SocketService::class.java).apply {
                action = ACTION_DISCONNECT
            })
        }
    }
}
