package io.homeassistant.android.api.icons;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.homeassistant.android.Utils;
import io.homeassistant.android.api.results.Entity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class ImageUtils {

    private static ImageUtils sInstance;

    private final File iconDirectory;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final MaterialDesignIconsUtils materialDesignIcons;

    private final Map<String, WeakReference<Drawable>> drawableCache = new HashMap<>();

    private ImageUtils(Context c) {
        iconDirectory = c.getFilesDir();
        materialDesignIcons = new MaterialDesignIconsUtils(iconDirectory, httpClient);
    }

    public static ImageUtils getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new ImageUtils(c);
        }
        return sInstance;
    }

    @Nullable
    public Drawable getEntityDrawable(Context context, Entity entity) throws Exception {
        IconRecord icon;

        String iconName = entity.attributes.icon;
        String pictureUrl = entity.attributes.entity_picture;
        if (iconName != null) {
            iconName = iconName.substring(4);
            String iconUrl = materialDesignIcons.getUrlFromName(iconName);
            if (iconUrl == null) {
                return null;
            }
            icon = new IconRecord(iconName, iconUrl);
        } else if (pictureUrl != null) {
            if (pictureUrl.startsWith("/local")) {
                pictureUrl = Utils.getUrl(context) + pictureUrl;
            }
            String pictureName = new URL(pictureUrl).getFile();
            int extIndex = pictureName.lastIndexOf(".");
            pictureName = pictureName.substring(pictureName.lastIndexOf("/") + 1, extIndex != -1 ? extIndex : pictureName.length());
            if (pictureName.isEmpty()) {
                return null;
            }
            icon = new IconRecord(pictureName, pictureUrl);
        } else icon = null;

        if (icon != null) {
            return loadDrawableFromCacheOrServer(icon);
        }
        return null;
    }


    @Nullable
    private Drawable loadDrawableFromCacheOrServer(@NotNull IconRecord icon) throws Exception {
        // Try to read from cache
        final Drawable cached = drawableCache.get(icon.name) != null ? drawableCache.get(icon.name).get() : null;
        if (cached != null) {
            Log.d(getClass().getSimpleName(), "Cached " + icon.name);
            return cached;
        }

        // Check whether icon file is available
        final File iconFile = new File(iconDirectory, icon.name.concat(".png"));
        if (iconFile.exists()) {
            final Drawable drawable = Drawable.createFromPath(iconFile.getAbsolutePath());
            if (drawable == null) {
                //noinspection ResultOfMethodCallIgnored
                iconFile.delete();
                return null;
            }
            drawableCache.put(icon.name, new WeakReference<>(drawable));
            return drawable;
        } else {
            // Download from server
            Log.d(getClass().getSimpleName(), "Loading " + icon.name + " from " + icon.url);
            Request request = new Request.Builder().url(icon.url).build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    byte[] buffer = new byte[1024];
                    InputStream input = response.body().byteStream();
                    FileOutputStream output = new FileOutputStream(iconFile);
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
}