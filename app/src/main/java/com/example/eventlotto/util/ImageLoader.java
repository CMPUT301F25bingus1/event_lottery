package com.example.eventlotto.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private ImageLoader() {}

    public static void load(ImageView target, @Nullable String url, int placeholderResId) {
        if (url == null || url.isEmpty()) {
            target.setImageResource(placeholderResId);
            target.setTag(null);
            return;
        }
        target.setImageResource(placeholderResId);
        target.setTag(url);

        EXECUTOR.execute(() -> {
            Bitmap bmp = null;
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(7000);
                conn.setInstanceFollowRedirects(true);
                conn.connect();
                try (InputStream is = conn.getInputStream()) {
                    bmp = BitmapFactory.decodeStream(is);
                }
            } catch (Exception ignored) {
            }

            final Bitmap result = bmp;
            target.post(() -> {
                Object tag = target.getTag();
                if (url.equals(tag)) {
                    if (result != null) {
                        target.setImageBitmap(result);
                    } else {
                        target.setImageResource(placeholderResId);
                    }
                }
            });
        });
    }
}

