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
import java.util.HashMap;
import java.util.Map;

import io.homeassistant.android.ApiClient;
import io.homeassistant.android.Utils;
import io.homeassistant.android.api.Attribute;
import io.homeassistant.android.api.EntityType;
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
    private final OkHttpClient httpClient;
    private final MaterialDesignIconsUtils materialDesignIcons;

    private final Map<String, WeakReference<Drawable>> drawableCache = new HashMap<>(20);
    private final Map<String, String> urlCache = new HashMap<>(3);

    private ImageUtils(Context c) {
        iconDirectory = new File(c.getCacheDir(), "icons");
        //noinspection ResultOfMethodCallIgnored
        iconDirectory.mkdir();
        httpClient = ApiClient.get(c, (success, code, data) -> null);
        materialDesignIcons = new MaterialDesignIconsUtils(iconDirectory, httpClient);
    }

    public static ImageUtils getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new ImageUtils(c);
        }
        return sInstance;
    }

    public void getEntityDrawable(Context context, Entity entity, DrawableLoadListener listener) throws Exception {
        boolean useCache = true;
        String imageName = null;
        String imageUrl = null;

        final String entityIcon = entity.attributes.getString(Attribute.ICON);
        final String entityPicture = entity.attributes.getString(Attribute.ENTITY_PICTURE);
        if (entityIcon != null) {
            imageName = entityIcon.substring(4);
            imageUrl = materialDesignIcons.getUrlFromName(imageName);
        }
        if (entityPicture != null && (entity.type == EntityType.CAMERA || entity.type == EntityType.MEDIA_PLAYER)) {
            imageName = "image_" + entity.getName();
            imageUrl = (entityPicture.startsWith("/local/") || entityPicture.startsWith("/api/") ? Utils.getUrl(context) : "") + entityPicture;
            if (!imageUrl.equals(urlCache.get(entity.id)) || entity.type == EntityType.CAMERA) {
                useCache = false;
            }
        }

        if (imageName != null && imageUrl != null) {
            loadEntityDrawable(entity, imageName, imageUrl, useCache, listener);
        }
    }

    private void loadEntityDrawable(Entity entity, String imageName, String imageUrl, boolean useCache, DrawableLoadListener listener) {
        final File iconFile = new File(iconDirectory, imageName.concat(".png"));
        if (useCache) {
            // Try to read from cache
            final Drawable cached = drawableCache.get(imageName) != null ? drawableCache.get(imageName).get() : null;
            if (cached != null) {
                Log.d(TAG, "Loading " + imageName + " from cache");
                listener.onDrawableLoaded(cached, false);
                return;
            }
            // Check whether icon file is available
            if (iconFile.exists()) {
                final Drawable drawable = Drawable.createFromPath(iconFile.getAbsolutePath());
                if (drawable != null) {
                    Log.d(TAG, "Loading " + imageName + " from file");
                    drawableCache.put(imageName, new WeakReference<>(drawable));
                    listener.onDrawableLoaded(drawable, false);
                    return;
                }
                //noinspection ResultOfMethodCallIgnored
                iconFile.delete();
            }
        }

        // Download from server
        Log.d(TAG, "Loading " + imageName + " from " + imageUrl);
        Request request = new Request.Builder().url(imageUrl).build();
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
                drawableCache.put(imageName, new WeakReference<>(drawable));
                urlCache.put(entity.id, imageUrl);
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