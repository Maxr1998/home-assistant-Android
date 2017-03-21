package io.homeassistant.android.api.icons;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.afollestad.ason.Ason;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MaterialDesignIconsUtils {

    private static final String MANIFEST_FILE = "manifest.json";
    private static MaterialDesignIconsUtils sInstance;

    private final OkHttpClient client = new OkHttpClient();
    private final File manifestFile;
    private final Map<String, String> manifest = new HashMap<>();
    private final Map<String, WeakReference<Drawable>> drawableCache = new HashMap<>();

    private MaterialDesignIconsUtils(Context c) {
        manifestFile = new File(c.getFilesDir(), MANIFEST_FILE);
        updateMap(null);
    }

    public static MaterialDesignIconsUtils getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new MaterialDesignIconsUtils(c);
        }
        return sInstance;
    }

    private void downloadManifest() {
        Request request = new Request.Builder().url("https://materialdesignicons.com/api/package/38EF63D0-4744-11E4-B3CF-842B2B6CFE1B").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody manifestBody = response.body();
                if (manifestBody != null) {
                    // Parse and update items
                    final List<ManifestItem> items = Ason.deserializeList(new Ason(manifestBody.string()).getJsonArray("icons"), ManifestItem.class);
                    updateMap(items);

                    // Strip extra data
                    String data = Ason.serializeList(items).toString();
                    manifestBody.close();

                    // Write manifest to storage
                    BufferedWriter writer = new BufferedWriter(new FileWriter(manifestFile));
                    writer.write(data);
                    writer.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void updateMap(@Nullable List<ManifestItem> items) {
        if (items == null) {
            try {
                if (!manifestFile.exists() || manifestFile.length() == 0) {
                    downloadManifest();
                    return;
                }
                BufferedReader reader = new BufferedReader(new FileReader(manifestFile));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                if (stringBuilder.length() == 0) {
                    downloadManifest();
                    return;
                }
                items = Ason.deserializeList(stringBuilder.toString(), ManifestItem.class);
            } catch (IOException e) {
                return;
            }
        }
        manifest.clear();
        for (int i = 0; i < items.size(); i++) {
            ManifestItem item = items.get(i);
            manifest.put(item.name, item.id);
        }
    }

    public Drawable getDrawableFromName(Context c, String name) throws Exception {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        name = name.substring(4);

        // Try to read from cache
        final Drawable cached = drawableCache.get(name) != null ? drawableCache.get(name).get() : null;
        if (cached != null) {
            return cached;
        }

        // Check whether icon file is available
        final File icon = new File(c.getFilesDir(), name.concat(".png"));
        if (icon.exists()) {
            final Drawable drawable = Drawable.createFromPath(icon.getAbsolutePath());
            drawableCache.put(name, new WeakReference<>(drawable));
            return drawable;
        } else {
            if (manifest.isEmpty()) {
                updateMap(null);
            }
            // Download from server
            Request request = new Request.Builder().url("https://materialdesignicons.com/api/download/icon/png/" + manifest.get(name) + "/192/000000/0.54").build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    byte[] buffer = new byte[1024];
                    InputStream input = response.body().byteStream();
                    FileOutputStream output = new FileOutputStream(icon);
                    int n;
                    while ((n = input.read(buffer)) != -1) {
                        output.write(buffer, 0, n);
                    }
                    input.close();
                    output.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {

                }
            });
            return null;
        }
    }

    private static class ManifestItem {
        public String id;
        public String name;
    }
}