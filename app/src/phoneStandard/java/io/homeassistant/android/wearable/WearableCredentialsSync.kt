package io.homeassistant.android.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import io.homeassistant.android.Common
import io.homeassistant.android.Utils


object WearableCredentialsSync {

    private val PREF_DATA_MAP_HASH = "data_map_hash"

    @JvmStatic fun transferCredentials(c: Context) {
        val oldHash = Utils.getPrefs(c).getInt(PREF_DATA_MAP_HASH, -1)
        var newHash = 0
        val putDataMapReq = PutDataMapRequest.create("/credentials").apply {
            dataMap.apply {
                putString(Common.PREF_HASS_URL_KEY, Utils.getUrl(c))
                putString(Common.PREF_HASS_PASSWORD_KEY, Utils.getPassword(c))
                putString(Common.PREF_BASIC_AUTH_KEY, Utils.getBasicAuth(c))
                newHash = hashCode()
            }
        }

        if (newHash != oldHash) {
            val apiClient = GoogleApiClient.Builder(c)
                    .addApi(Wearable.API)
                    .build().apply { connect() }
            val result = Wearable.DataApi.putDataItem(apiClient, putDataMapReq.asPutDataRequest())
            result.setResultCallback {
                dataItemResult ->
                if (dataItemResult.status.isSuccess) {
                    Log.d(javaClass.simpleName, "Successfully synced Credentials!")
                }
            }
            Utils.getPrefs(c).edit().putInt(PREF_DATA_MAP_HASH, newHash).apply()
        }
    }
}