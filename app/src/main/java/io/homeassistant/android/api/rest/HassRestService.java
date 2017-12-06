package io.homeassistant.android.api.rest;

import java.util.List;

import io.homeassistant.android.api.websocket.results.Entity;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Nicolas on 2017-12-04.
 */

public interface HassRestService {

    @GET("states")
    Call<List<Entity>> getStates();

    @POST("services/{domain}/{service}")
    Call<List<Entity>> callService(@Path("domain") String domain, @Path("service") String service, @Body RequestBody body);

    @POST("services/device_tracker/see")
    Call<List<Entity>>  setDeviceLocation(@Body DeviceTrackerData body);

}
