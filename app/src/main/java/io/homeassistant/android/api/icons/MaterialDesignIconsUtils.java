package io.homeassistant.android.api.icons;

import android.support.annotation.Nullable;

import com.afollestad.ason.Ason;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class MaterialDesignIconsUtils {

    private final OkHttpClient httpClient;
    private final File manifestFile;
    private final Map<String, String> manifest = new HashMap<>();

    private final AtomicBoolean downloadingManifest = new AtomicBoolean(false);

    MaterialDesignIconsUtils(File iconDir, OkHttpClient client) {
        manifestFile = new File(iconDir, "manifest.json");
        httpClient = client;
        updateMap(null);
    }

    private void downloadManifest() {
        if (!downloadingManifest.compareAndSet(false, true)) {
            return;
        }
        Request request = new Request.Builder().url("https://materialdesignicons.com/api/package/38EF63D0-4744-11E4-B3CF-842B2B6CFE1B").build();
        httpClient.newCall(request).enqueue(new Callback() {
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
                downloadingManifest.set(false);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                downloadingManifest.set(false);
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

    @Nullable
    String getUrlFromName(String name) {
        if (manifest.isEmpty() || !manifest.containsKey(name)) {
            if (!downloadingManifest.get())
                updateMap(null);
            return null;
        }
        return "https://materialdesignicons.com/api/download/icon/png/" + manifest.get(name) + "/192/000000/0.54";
    }

    private static class ManifestItem {
        public String id;
        public String name;
    }
}