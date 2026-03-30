package com.ecotrack.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Image utility — compression and resizing for photo uploads.
 * Keeps uploaded images small for Firebase Storage efficiency.
 */
public final class ImageUtils {

    private static final int DEFAULT_MAX_WIDTH = 1024;
    private static final int JPEG_QUALITY = 80;

    private ImageUtils() { }

    /**
     * Compress an image from a content URI.
     *
     * @param context  Application context for content resolver.
     * @param imageUri URI of the source image (gallery or camera).
     * @param maxWidth Maximum width in pixels; height scales proportionally.
     * @return Compressed JPEG bytes, or null on failure.
     */
    public static byte[] compressImage(Context context, Uri imageUri, int maxWidth) {
        try (InputStream input = context.getContentResolver().openInputStream(imageUri)) {
            if (input == null) return null;

            // Decode bounds only
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opts);

            // Calculate inSampleSize for efficient memory usage
            opts.inSampleSize = calculateInSampleSize(opts, maxWidth);
            opts.inJustDecodeBounds = false;

            // Re-open stream (can't reuse after bounds decode)
            try (InputStream input2 = context.getContentResolver().openInputStream(imageUri)) {
                if (input2 == null) return null;

                Bitmap bitmap = BitmapFactory.decodeStream(input2, null, opts);
                if (bitmap == null) return null;

                // Scale to exact max width if still too large
                if (bitmap.getWidth() > maxWidth) {
                    float ratio = (float) maxWidth / bitmap.getWidth();
                    int newHeight = Math.round(bitmap.getHeight() * ratio);
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
                    if (scaled != bitmap) {
                        bitmap.recycle();
                    }
                    bitmap = scaled;
                }

                // Compress to JPEG
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
                bitmap.recycle();
                return baos.toByteArray();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convenience overload using default max width (1024px).
     */
    public static byte[] compressImage(Context context, Uri imageUri) {
        return compressImage(context, imageUri, DEFAULT_MAX_WIDTH);
    }

    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps
     * both width and height larger than the requested width.
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {
            int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
