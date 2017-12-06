package io.homeassistant.android;

import com.afollestad.ason.Ason;
import com.afollestad.asonretrofit.AsonConverterFactory;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import io.homeassistant.android.api.rest.DeviceTrackerData;
import io.homeassistant.android.api.rest.HassRestService;
import io.homeassistant.android.api.websocket.results.Entity;
import retrofit2.Retrofit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestApiTest {

    HassRestService getRestApi(String baseUrl)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(new AsonConverterFactory())
                .build();
        return retrofit.create(HassRestService.class);
    }

    @Test
    public void hassApi_send_location() throws IOException {

        HassRestService restApi = getRestApi("http://127.0.0.1:8123/api/");


        DeviceTrackerData body = new DeviceTrackerData();
        body.dev_id = "demo_paulus";
        body.setLocation(40.,70.);
        body.gps_accuracy = 5;
        body.battery = 100;


        List<Entity> answer = restApi.setDeviceLocation(body).execute().body();

        assertTrue(answer.size()!=0);


    }

}