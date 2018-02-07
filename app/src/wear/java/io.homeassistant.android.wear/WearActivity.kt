package io.homeassistant.android.wear

import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.wear.widget.WearableLinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import io.homeassistant.android.*


class WearActivity : BaseActivity() {

    private var retryCount = 0
    private val viewAdapter = WearViewAdapter()

    private lateinit var viewRecycler: WearableRecyclerView
    private lateinit var wearableProgress: ProgressBar

    private val googleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build()
                .apply { connect() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hass)

        if (Utils.getUrl(this).isEmpty() || Utils.getPassword(this).isEmpty()) {
            retrieveCredentials()
        }

        wearableProgress = findViewById(R.id.wearable_progress)

        viewRecycler = findViewById(R.id.view_recycler)
        viewRecycler.isCircularScrollingGestureEnabled = true
        viewRecycler.isEdgeItemsCenteringEnabled = true
        viewRecycler.layoutManager = WearableLinearLayoutManager(this)
        viewRecycler.adapter = viewAdapter
    }

    fun retrieveCredentials() {
        Wearable.DataApi.getDataItems(googleApiClient, Uri.parse("wear://*/credentials"))
                .setResultCallback { dataItemBuffer ->
                    dataItemBuffer.apply {
                        firstOrNull()?.apply {
                            let { DataMapItem.fromDataItem(it) }.dataMap.apply map@ {
                                Utils.getPrefs(this@WearActivity).edit().apply prefs@ {
                                    Common.PREF_HASS_URL_KEY.let {
                                        this@prefs.putString(it, this@map.getString(it))
                                    }
                                    Common.PREF_HASS_PASSWORD_KEY.let {
                                        this@prefs.putString(it, this@map.getString(it))
                                    }
                                    Common.PREF_BASIC_AUTH_KEY.let {
                                        this@prefs.putString(it, this@map.getString(it))
                                    }
                                }.apply()
                            }
                        }
                        release()
                    }
                    attemptLogin()
                }
    }

    override fun onStart() {
        super.onStart()
        // Make sure that service is connected, if not it'll re-attempt
        service?.connect()
    }

    override fun onStop() {
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
        super.onStop()
    }

    override fun loginSuccess() {
    }

    override fun loginFailed(reason: Int, data: String) {
        if (retryCount++ == 0) {
            retrieveCredentials()
        } else {
            finish()
            @StringRes val message: Int
            when (reason) {
                CommunicationHandler.FAILURE_REASON_WRONG_PASSWORD -> message = R.string.login_error_wrong_password
                else -> message = R.string.login_error_generic
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun updateStates() {
        viewAdapter.updateEntities(service.entityMap)
        wearableProgress.visibility = View.GONE
    }
}