package io.homeassistant.android

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import com.afollestad.ason.Ason
import io.homeassistant.android.CommunicationHandler.*
import io.homeassistant.android.api.HassUtils
import io.homeassistant.android.api.requests.*
import io.homeassistant.android.api.results.Entity
import io.homeassistant.android.api.results.EventResult
import io.homeassistant.android.api.results.RequestResult
import okhttp3.*
import java.lang.ref.SoftReference
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class HassService : Service() {

    private val binder = HassBinder()
    val entityMap: Map<String, Entity> = HashMap()
    @JvmField val connecting = AtomicBoolean(false)
    @JvmField val connected = AtomicBoolean(false)
    @JvmField val authenticationState = AtomicInteger(AUTH_STATE_NOT_AUTHENTICATED)
    private var hassSocket: WebSocket? = null
    private val socketListener = HassSocketListener()
    private val lastId = AtomicInteger(0)

    private var activityHandler: Handler? = null
    private val requests = SparseArray<SoftReference<RequestResult.OnRequestResultListener>>(3)

    private val actionsQueue = LinkedList<String>()
    private val handlingQueue = AtomicBoolean(false)
    private val stopServiceHandler = Handler()

    override fun onCreate() {
        connect()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val command = intent.getStringExtra(EXTRA_ACTION_COMMAND)
        if (command != null) {
            actionsQueue.add(command)
            if (authenticationState.get() == AUTH_STATE_AUTHENTICATED)
                handleActionsQueue()
            return Service.START_NOT_STICKY
        }
        stopSelf()
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onDestroy() {
        disconnect()
    }

    fun setActivityHandler(handler: Handler) {
        activityHandler = handler
    }

    fun connect() {
        // Don't try to connect if already connecting
        if (!connecting.compareAndSet(false, true))
            return

        // Check if already connected
        if (hassSocket != null) {
            if (connected.get()) {
                connecting.set(false)
                // Still connected, reload states
                if (activityHandler != null) {
                    loadStates()
                }
                return
            } else
                disconnect()
        }
        // Connect to WebSocket
        var url = Utils.getUrl(this)
        // Don't connect if no url or password is set - instances without password have their password set to Common.NO_PASSWORD
        if (!url.isEmpty() && !Utils.getPassword(this).isEmpty()) {
            url += "/api/websocket"
            Log.d("Home Assistant URL", url)
            val client = ApiClient.get(this, ::loginMessage)
            val requestBuilder = Request.Builder().url(HttpUrl.parse(url))
            val basicAuth = Utils.getBasicAuth(this)
            if (!basicAuth.isEmpty()) {
                requestBuilder.header("Authorization", basicAuth)
                Log.d(TAG, basicAuth)
            }
            hassSocket = client.newWebSocket(requestBuilder.build(), socketListener)
        } else
            connecting.set(false)
    }

    private fun authenticate() {
        if (!authenticationState.compareAndSet(AUTH_STATE_NOT_AUTHENTICATED, AUTH_STATE_AUTHENTICATING)) {
            return
        }
        val password = Utils.getPassword(this)
        if (password.isNotEmpty())
            send(AuthRequest(password), null)
    }

    private fun loginMessage(success: Boolean, reason: Int, data: String? = null) {
        authenticationState.set(if (success) AUTH_STATE_AUTHENTICATED else AUTH_STATE_NOT_AUTHENTICATED)
        activityHandler?.obtainMessage(if (success) MESSAGE_LOGIN_SUCCESS else MESSAGE_LOGIN_FAILED, reason, 0, data)?.sendToTarget()
    }

    fun subscribeEvents() {
        val eventSubscribe = SubscribeEventsRequest("state_changed")
        send(eventSubscribe, RequestResult.OnRequestResultListener { _, _ -> Log.i(TAG, "Subscribed to events") })
    }

    fun loadStates() {
        if (authenticationState.get() != AUTH_STATE_AUTHENTICATED) {
            authenticate()
            return
        }
        send(StatesRequest(), RequestResult.OnRequestResultListener { success, result ->
            if (success && HassUtils.extractEntitiesFromStateResult(result, entityMap)) {
                activityHandler?.obtainMessage(MESSAGE_STATES_AVAILABLE)?.sendToTarget()
            }
        })
    }

    fun send(hassRequest: HassRequest, resultListener: RequestResult.OnRequestResultListener?): Boolean {
        val message = hassRequest.toAson()
        if (hassRequest !is AuthRequest) {
            val rId = lastId.incrementAndGet()
            message.put("id", rId)
            resultListener?.let { requests.append(rId, SoftReference(resultListener)) }
        }
        return hassSocket?.send(message.toString()) ?: false
    }

    private fun handleActionsQueue() {
        if (handlingQueue.compareAndSet(false, true)) {
            // Automatically stop the service after 30 seconds, queue should be empty by then and service not needed anymore
            stopServiceHandler.postDelayed({ stopSelf() }, 30_000L)
            runNextAction()
        }
    }

    private fun runNextAction() {
        if (actionsQueue.peek() != null) {
            Log.d(TAG, "Sending action command " + actionsQueue.peek())
            send(StringRequest(actionsQueue.remove()), RequestResult.OnRequestResultListener { _, _ -> runNextAction() })
        } else
            handlingQueue.set(false)
    }

    fun disconnect() {
        hassSocket?.apply { close(1001, "Application closed") } ?: connected.set(false)
        hassSocket = null
        authenticationState.set(AUTH_STATE_NOT_AUTHENTICATED)
    }

    inner class HassBinder : Binder() {
        val service: HassService
            get() = this@HassService
    }

    private inner class HassSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            connecting.set(false)
            connected.set(true)
        }

        override fun onMessage(webSocket: WebSocket, text: String?) {
            try {
                val message = Ason(text)
                val type = message.getString("type", "")
                when (type) {
                    "auth_required" -> {
                        Log.d(TAG, "Authenticating..")
                        authenticate()
                    }
                    "auth_failed", "auth_invalid" -> {
                        Log.w(TAG, "Authentication failed!")
                        loginMessage(false, FAILURE_REASON_WRONG_PASSWORD)
                    }
                    "auth_ok" -> {
                        Log.d(TAG, "Authenticated.")
                        loginMessage(true, 0)
                        // Automatically load current states if bound to Activity
                        if (activityHandler != null) {
                            subscribeEvents()
                            loadStates()
                        } else
                            handleActionsQueue()
                    }
                    "event" -> {
                        val eventRequest = Ason.deserialize(message, EventResult::class.java)
                        val updated = HassUtils.updateEntityFromEventResult(eventRequest.event.data, entityMap)
                        updated?.let {
                            Log.d(TAG, "Updated " + updated.toString())
                            activityHandler!!.post { updated.notifyObservers() }
                        }
                    }
                    "result" -> {
                        val res = Ason.deserialize(message, RequestResult::class.java)
                        Log.d(TAG, String.format(
                                "Request %1\$d %2\$s\nResult: %3\$s\nError : %4\$s", res.id, if (res.success) "successful" else "failed",
                                if (res.result != null && res.result.javaClass.isArray) Arrays.toString(res.result as Array<*>) else Objects.toString(res.result),
                                res.error
                        ))
                        val resultListener = requests.get(res.id, SoftReference<RequestResult.OnRequestResultListener>(null)).get()
                        if (resultListener != null) {
                            resultListener.onRequestResult(res.success, res.result)
                            requests.remove(res.id)
                        }
                    }
                }
            } catch (t: Throwable) { // Catch everything that it doesn't get passed to onFailure
                Log.e(TAG, "Error in onMessage()", t)
            }

        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            connected.set(false)
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            connecting.set(false)
            connected.set(false)
            if (t is SocketException || t is ProtocolException || t is SSLException || t is UnknownHostException) {
                Log.e(TAG, String.format("%1\$s while connecting to Socket, going to try again - Code %2\$d", t.javaClass.simpleName, response?.code()))
                disconnect()
                if (t is SSLHandshakeException) {
                    loginMessage(false, FAILURE_REASON_SSL_INVALID_CERT, t.message)
                } else if (t !is SSLPeerUnverifiedException && response?.code() != HttpURLConnection.HTTP_UNAUTHORIZED)
                    loginMessage(false, FAILURE_REASON_GENERIC)
                return
            }
            Log.e(TAG, "Error from onFailure()", t)
        }
    }

    companion object {
        @JvmField val EXTRA_ACTION_COMMAND = "extra_action_command"

        @JvmField val AUTH_STATE_NOT_AUTHENTICATED = 0
        @JvmField val AUTH_STATE_AUTHENTICATING = 1
        @JvmField val AUTH_STATE_AUTHENTICATED = 2

        val TAG = HassService::class.java.simpleName
    }
}