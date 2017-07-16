package io.homeassistant.android.wear

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.wear.widget.WearableLinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import io.homeassistant.android.BaseActivity
import io.homeassistant.android.CommunicationHandler
import io.homeassistant.android.R
import io.homeassistant.android.Utils

class WearActivity : BaseActivity() {

    private val viewAdapter = WearViewAdapter()

    private lateinit var viewRecycler: WearableRecyclerView
    private lateinit var wearableProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hass)

        if (Utils.getUrl(this).isEmpty() || Utils.getPassword(this).isEmpty()) {
            // TODO handle gathering URL and password from phone
        }

        wearableProgress = findViewById(R.id.wearable_progress)

        viewRecycler = findViewById(R.id.view_recycler)
        viewRecycler.isCircularScrollingGestureEnabled = true
        viewRecycler.isEdgeItemsCenteringEnabled = true
        viewRecycler.layoutManager = WearableLinearLayoutManager(this)
        viewRecycler.adapter = viewAdapter
    }

    override fun onStart() {
        super.onStart()
        // Make sure that service is connected, if not it'll re-attempt
        service?.connect()
    }

    override fun loginSuccess() {
    }

    override fun loginFailed(reason: Int) {
        finish()
        @StringRes val message: Int
        when (reason) {
            CommunicationHandler.FAILURE_REASON_WRONG_PASSWORD -> message = R.string.login_error_wrong_password
            else -> message = R.string.login_error_generic
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun updateStates() {
        viewAdapter.updateEntities(service.entityMap)
        wearableProgress.visibility = View.GONE
    }
}