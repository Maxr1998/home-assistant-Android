package io.homeassistant.android.select;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.util.Log;

import java.util.List;

import io.homeassistant.android.ApiInjector;
import io.homeassistant.android.HassViewModel;
import io.homeassistant.android.api.rest.HassRestService;
import io.homeassistant.android.api.websocket.results.Entity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nicolas on 2017-12-11.
 */

public class SelectViewModel extends ViewModel {

    final static String TAG = SelectViewModel.class.getSimpleName();
    final private HassRestService api;
    final private MutableLiveData<List<Entity>> entitiesLiveData = new MutableLiveData<>();
    final private MutableLiveData<Boolean> statusLiveData = new MutableLiveData<>();

    public SelectViewModel(Context context)
    {
        api = ApiInjector.getRestApi(context);
    }

    public void load()
    {
        Log.d(TAG,"load");
        api.getStates().enqueue(new Callback<List<Entity>>() {
            @Override
            public void onResponse(Call<List<Entity>> call, Response<List<Entity>> response) {
                Log.d(TAG,"load succeeded");
                entitiesLiveData.postValue(response.body());
                statusLiveData.postValue(true);
            }

            @Override
            public void onFailure(Call<List<Entity>> call, Throwable t) {
                Log.e(TAG,"Failed to get states",t);
                statusLiveData.postValue(false);
            }
        });
    }

    public LiveData<List<Entity>> getEntityList()
    {
        return entitiesLiveData;
    }

    public LiveData<Boolean> getStatus()
    {
        return statusLiveData;
    }




}
