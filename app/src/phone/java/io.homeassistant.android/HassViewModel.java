package io.homeassistant.android;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.homeassistant.android.api.Events;
import io.homeassistant.android.api.websocket.HassWebSockerApi;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.websocket.requests.HassRequest;
import io.homeassistant.android.api.websocket.requests.StatesRequest;
import io.homeassistant.android.api.websocket.requests.SubscribeEventsRequest;
import io.homeassistant.android.api.websocket.results.Entity;
import io.homeassistant.android.api.websocket.results.EventResult;
import io.homeassistant.android.api.websocket.results.RequestResult;

/**
 * Created by Nicolas on 2017-11-23.
 */

public class HassViewModel extends ViewModel {
    final static String TAG = HassViewModel.class.getSimpleName();
    final private HassWebSockerApi hassApi;
    final private Map<String, Entity> entityMap = new HashMap<>();
    final private MutableLiveData<Map<String, Entity>> entityMapData = new MutableLiveData<>();
    final private MutableLiveData<Entity> updatedEntityData = new MutableLiveData<>();

    public HassViewModel(HassWebSockerApi hassApi)
    {
        this.hassApi = hassApi;

        this.hassApi.getEvent().observeForever( new Observer<EventResult>() {
            @Override
            public void onChanged(@Nullable EventResult eventResult) {
                Entity entity = HassUtils.updateEntityFromEventResult(eventResult.event.data, entityMap);
                if(entity!=null)
                {
                    updatedEntityData.postValue(entity);
                    entityMapData.postValue(entityMap);
                }
            }
        });
    }

    public void connect(String url, String password)
    {
        if (!url.isEmpty() && !password.isEmpty()) {
            Log.d(TAG,"connecting");
            hassApi.connect(url, password);
        }
    }

    public void disconnect()
    {
        Log.d(TAG,"disconnecting");
        hassApi.disconnect();
    }

    public void load()
    {
        Log.d(TAG,"Loading states");
        final LiveData<RequestResult> data = hassApi.send(new StatesRequest());

        final Observer<RequestResult> observer = new Observer<RequestResult>() {
            @Override
            public void onChanged(@Nullable RequestResult requestResult) {
                data.removeObserver(this);
                if(requestResult.success) {
                    HassUtils.extractEntitiesFromStateResult(requestResult.result, entityMap);
                    entityMapData.postValue(entityMap);

                    hassApi.send(new SubscribeEventsRequest(Events.STATE_CHANGED));
                }
            }
        };

        data.observeForever(observer);
    }



    public LiveData<Map<String, Entity>> getEntityMap()
    {
        return entityMapData;
    }

    public LiveData<Entity> getUpdatedEntity()
    {
        // TODO
        return updatedEntityData;
    }

    public LiveData<Boolean> getConnected()
    {
        return hassApi.getConnected();
    }

    public LiveData<HassWebSockerApi.AuthStatus> getAuthStatus()
    {
        return hassApi.getAuthStatus();
    }

    public LiveData<RequestResult> send(HassRequest request) {
        return hassApi.send(request);
    }
}
