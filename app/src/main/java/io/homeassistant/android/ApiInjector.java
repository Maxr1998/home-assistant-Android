package io.homeassistant.android;

import android.content.Context;

import com.afollestad.asonretrofit.AsonConverterFactory;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

import io.homeassistant.android.api.rest.HassRestService;
import io.homeassistant.android.api.websocket.HassWebSockerApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by Nicolas on 2017-11-23.
 */

public class ApiInjector {

    public static LocationUpdateHandler getLocationUpdateHandler(Context context) {

        String url = getUrl(context);
        String deviceName = getDeviceName(context);
        HassRestService api = getRestApi(url + "/api");

        return new LocationUpdateHandler(api,deviceName,context);
    }

    public interface AuthFailedHandler
    {
        void setAuthStatus(boolean success, int reason);
    }

    static private HassWebSockerApi hassApi = null;
    static private OkHttpClient okHttpClient=null;


    public static String getPassword(Context context)
    {
        return Utils.getPassword(context);
    }

    public static String getDeviceName(Context context)
    {
        String deviceName = Utils.getPrefs(context).getString(Common.PREF_LOCATION_DEVICE_NAME, null);
        return deviceName;
    }

    public static String getUrl(Context context)
    {
        return Utils.getUrl(context);
    }

    public static OkHttpClient getOkHttpClient()//(final AuthFailedHandler authFailedHandler)
    {
        if(okHttpClient==null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                /*.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        if (OkHostnameVerifier.INSTANCE.verify(hostname, session) || getAllowedHostMismatchesFor(hostname)) {
                            return true;
                        }
                        else {
                            authFailedHandler.setAuthStatus(false, FAILURE_REASON_SSL_MISMATCH);
                            return false;
                        }
                    }
                })
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            authFailedHandler.setAuthStatus(false, FAILURE_REASON_BASIC_AUTH);
                        }
                        return null;
                    }
                })*/
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        }
        return okHttpClient;
    }


    public static HassRestService getRestApi(String baseUrl)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(new AsonConverterFactory())
                .client(getOkHttpClient())
                .build();
        return retrofit.create(HassRestService.class);
    }

    public static HassWebSockerApi getHassApi(Context context)
    {
        if(hassApi == null)
        {
            hassApi = new HassWebSockerApi();
        }

        return hassApi;
    }



}
