package io.homeassistant.android;

import android.content.Context;

import com.afollestad.asonretrofit.AsonConverterFactory;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.homeassistant.android.api.rest.HassRestService;
import io.homeassistant.android.api.websocket.HassWebSockerApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Created by Nicolas on 2017-11-23.
 */

public class ApiInjector {

    public static LocationUpdateHandler getLocationUpdateHandler(Context context) {

        String url = getUrl(context);
        String deviceName = getDeviceName(context);
        HassRestService api = getRestApi(context,url);

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


    public static OkHttpClient getOkHttpClientWithAuth(String password)
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            Request original = chain.request();

                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("x-ha-access", password); // <-- this is the important line

                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();


        return okHttpClient;
    }

    private static Retrofit getRestApiRetrofit(String baseUrl, String password)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "/api/")
                .addConverterFactory(new AsonConverterFactory())
                .client(getOkHttpClientWithAuth(password))
                .build();
        return retrofit;
    }

    public static HassRestService getRestApi(Context context)
    {
        Retrofit retrofit = getRestApiRetrofit(getUrl(context),getPassword(context));

        return retrofit.create(HassRestService.class);
    }

    public static HassRestService getRestApi(Context context,String baseUrl)
    {
        Retrofit retrofit = getRestApiRetrofit(baseUrl,getPassword(context));
        return retrofit.create(HassRestService.class);
    }

    public static HassRestService getRestApi(String baseUrl, String password)
    {
        Retrofit retrofit = getRestApiRetrofit(baseUrl,password);
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
