package io.homeassistant.android.api.icons;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

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
import io.homeassistant.android.api.Attribute;
import io.homeassistant.android.api.results.Entity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

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

    public void loadEntityDrawable(Context context, Entity entity, boolean useCache, DrawableLoadListener listener) throws Exception {
        IconRecord tempIcon = null;

        String iconName = entity.attributes.getString(Attribute.ICON);
        String pictureUrl = entity.attributes.getString(Attribute.ENTITY_PICTURE);
        if (iconName != null) {
            iconName = iconName.substring(4);
            String iconUrl = materialDesignIcons.getUrlFromName(iconName);
            if (iconUrl != null) {
                tempIcon = new IconRecord(iconName, iconUrl);
            }
        } else if (pictureUrl != null) {
            if (pictureUrl.startsWith("/local") || pictureUrl.startsWith("/api/camera_proxy")) {
                pictureUrl = Utils.getUrl(context) + pictureUrl;
            }
            String pictureName = new URL(pictureUrl).getFile();
            int extIndex = pictureName.lastIndexOf(".");
            pictureName = pictureName.substring(pictureName.lastIndexOf("/") + 1, extIndex != -1 ? extIndex : pictureName.length());
            if (!pictureName.isEmpty()) {
                tempIcon = new IconRecord(pictureName, pictureUrl);
            }
        }

        if (tempIcon == null) {
            return;
        }
        final IconRecord icon = tempIcon;

        final File iconFile = new File(iconDirectory, icon.name.concat(".png"));
        if (useCache) {
            // Try to read from cache
            final Drawable cached = drawableCache.get(icon.name) != null ? drawableCache.get(icon.name).get() : null;
            if (cached != null) {
                Log.d(TAG, "Cached " + tempIcon.name);
                listener.onDrawableLoaded(cached, false);
                return;
            }

            // Check whether icon file is available
            if (iconFile.exists()) {
                final Drawable drawable = Drawable.createFromPath(iconFile.getAbsolutePath());
                if (drawable != null) {
                    Log.d(TAG, "Stored " + tempIcon.name);
                    drawableCache.put(icon.name, new WeakReference<>(drawable));
                    listener.onDrawableLoaded(drawable, false);
                    return;
                }
                //noinspection ResultOfMethodCallIgnored
                iconFile.delete();
            }
        }

        // Download from server
        Log.d(TAG, "Loading " + tempIcon.name + " from " + tempIcon.url);
        Request request = new Request.Builder().url(tempIcon.url).build();
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

                // Return drawable
                Drawable drawable = Drawable.createFromPath(iconFile.getAbsolutePath());
                if (useCache)
                    drawableCache.put(icon.name, new WeakReference<>(drawable));

                listener.onDrawableLoaded(drawable, true);
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    public interface DrawableLoadListener {
        void onDrawableLoaded(@Nullable Drawable drawable, boolean async);
    }
}