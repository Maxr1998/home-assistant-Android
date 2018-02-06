package io.homeassistant.android

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
import java.net.HttpURLConnection
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.auth.x500.X500Principal
import kotlin.experimental.and

object ApiClient {
    @JvmStatic
    inline fun get(context: Context, crossinline callback: ((Boolean, Int, String?) -> Unit)): OkHttpClient {
        val trustManager = CustomTrustManager(Utils.getPrefs(context))
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .hostnameVerifier { hostname, session ->
                    if (OkHostnameVerifier.INSTANCE.verify(hostname, session) || Utils.getAllowedHostMismatches(context).contains(hostname)) {
                        return@hostnameVerifier true
                    }
                    callback(false, CommunicationHandler.FAILURE_REASON_SSL_MISMATCH, null)
                    false
                }
                .authenticator { _, response ->
                    Log.d(HassService.TAG, "Authenticator running..")
                    if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        callback(false, CommunicationHandler.FAILURE_REASON_BASIC_AUTH, null)
                    }
                    null
                }
                .sslSocketFactory(trustManager.getSocketFactory(), trustManager)
                .build()
    }

    class CustomTrustManager(private val prefs: SharedPreferences) : X509TrustManager {

        private val defaultTrustManager = try {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
            }
            trustManagers[0] as X509TrustManager
        } catch (e: GeneralSecurityException) {
            throw AssertionError() // The system has no TLS. Just give up.
        }

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            defaultTrustManager.checkClientTrusted(chain, authType)
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String?) {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType)
            } catch (e: CertificateException) {
                val hash = chain[0].encoded.toSHA256String()
                if (prefs.getAllowedSSLCerts().contains(hash))
                    return
                val certNameString = chain[0].subjectX500Principal.getName(X500Principal.RFC1779) + "|" +
                        chain[0].issuerX500Principal.getName(X500Principal.RFC1779) + "|" + hash
                throw CertificateException(certNameString, e)
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return defaultTrustManager.acceptedIssuers
        }

        fun getSocketFactory(): SSLSocketFactory = try {
            SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(this@CustomTrustManager), null)
            }.socketFactory
        } catch (e: GeneralSecurityException) {
            throw AssertionError() // The system has no TLS. Just give up.
        }

        private fun ByteArray.toSHA256String(): String =
                MessageDigest.getInstance("SHA-256").digest(this).bytesToHex()

        private fun ByteArray.bytesToHex(): String {
            val result = StringBuffer()
            for (b: Byte in this) result.append(Integer.toString((b.and(0xff.toByte())) + 0x100, 16).substring(1))
            return result.toString()
        }
    }
}