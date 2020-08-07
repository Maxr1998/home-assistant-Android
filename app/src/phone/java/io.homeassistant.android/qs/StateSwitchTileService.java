package io.homeassistant.android.qs;

import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

@RequiresApi(api = Build.VERSION_CODES.N)
public class StateSwitchTileService extends TileService {

    @Override
    public void onTileAdded() {
    }

    @Override
    public void onClick() {
        Toast.makeText(this, "Clicked my tile", Toast.LENGTH_SHORT).show();
    }

    public static final String PREF_QS_QUICK_ACCESS = "qs_quick_access";
}

/*

        <service
            android:name=".qs.StateSwitchTileService"
            android:icon="@mipmap/ic_launcher_foreground"
            android:permission="android.permission.BIND_QUICK_SETTINGS_TILE">
            <intent-filter>
                <action android:name="android.service.quicksettings.action.QS_TILE" />
            </intent-filter>
        </service>

 */