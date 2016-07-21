/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * ImageLoader is a universal tool for loading bitmaps efficiently in Android which
 * provides automatic memory and storage caching. ImageLoader is usable without caches,
 * with one of them, or both (without caches, caching is not available). Also, ImageLoader
 * is usable without bitmapLoader (loading new bitmaps is not available in this case).
 *
 * @param <T> Source data type
 */
public class ImageLoader<T> {
    private final Object mPauseWorkLock = new Object();
    private final Context mContext;
    private volatile boolean mImageFadeIn = true;
    private volatile boolean mExitTasksEarly;
    private volatile boolean mPauseWork;
    private volatile int mImageFadeInTime = 200;
    private volatile BitmapLoader<T> mBitmapLoader;
    private volatile MemoryImageCache mMemoryImageCache;
    private volatile StorageImageCache mStorageImageCache;
    private volatile Bitmap mPlaceholderBitmap;

    /**
     * ImageLoader without any cache or bitmap loader
     *
     * @param context Context
     */
    public ImageLoader(@NonNull Context context) {
        this(context, null, null, null);
    }

    /**
     * ImageLoader with default memory and storage caches. ImageLoader
     * is usable without bitmapLoader (loading new bitmaps is not available in this case).
     *
     * @param context      Context
     * @param bitmapLoader Bitmap loader
     */
    public ImageLoader(@NonNull Context context, @Nullable BitmapLoader<T> bitmapLoader) {
        this(context, bitmapLoader, newMemoryImageCache(), newStorageImageCache(context));
    }

    /**
     * ImageLoader with specified bitmap loader, memory image cache and storage image cache
     *
     * @param context           Context
     * @param bitmapLoader      Bitmap loader
     * @param memoryImageCache  Memory image cache
     * @param storageImageCache Storage image cache
     */
    public ImageLoader(@NonNull Context context, @Nullable BitmapLoader<T> bitmapLoader,
            @Nullable MemoryImageCache memoryImageCache,
            @Nullable StorageImageCache storageImageCache) {
        mContext = Objects.requireNonNull(context);
        mBitmapLoader = bitmapLoader;
        mMemoryImageCache = memoryImageCache;
        mStorageImageCache = storageImageCache;
    }

    @NonNull
    Object getPauseWorkLock() {
        return mPauseWorkLock;
    }

    @NonNull
    protected Context getContext() {
        return mContext;
    }

    protected boolean isPauseWork() {
        return mPauseWork;
    }

    protected void setPauseWork(boolean pauseWork) {
        synchronized (getPauseWorkLock()) {
            mPauseWork = pauseWork;
            if (!isPauseWork()) {
                getPauseWorkLock().notifyAll();
            }
        }
    }

    /**
     * Load image to imageView from imageSource
     *
     * @param imageSource       Image source
     * @param imageView         Image view
     * @param imageLoadCallback Optional callback
     */
    public void loadImage(@NonNull ImageSource<T> imageSource, @NonNull ImageView imageView,
            @Nullable ImageLoadCallback imageLoadCallback) {
        BitmapDrawable drawable = null;
        MemoryImageCache memoryImageCache = getMemoryImageCache();
        if (memoryImageCache != null) {
            drawable = memoryImageCache.get(imageSource.getKey());
        }
        if (drawable != null) {
            if (imageLoadCallback != null) {
                Bitmap image = drawable.getBitmap();
                if (image != null) {
                    imageLoadCallback.onImageLoaded(image, true, false);
                }
            }
            ThreadUtils.runOnMainThread(
                    new SimpleSetImageAction(imageView, drawable, imageLoadCallback));
        } else if (cancelPotentialWork(imageSource, imageView)) {
            LoadImageAction<T> loadAction =
                    new LoadImageAction<>(imageSource, imageView, imageLoadCallback, this);
            AsyncBitmapDrawable asyncBitmapDrawable =
                    new AsyncBitmapDrawable(getContext().getResources(), getPlaceholderImage(),
                            loadAction);
            ThreadUtils.runOnMainThread(
                    new SimpleSetImageAction(imageView, asyncBitmapDrawable, null));
            loadAction.execute(ExecutorUtils.getImageLoaderExecutor());
        }
    }

    /**
     * Load image to imageView from imageSource
     *
     * @param imageSource Image source
     * @param imageView   Image view
     */
    public void loadImage(@NonNull ImageSource<T> imageSource, @NonNull ImageView imageView) {
        loadImage(imageSource, imageView, null);
    }

    public void invalidate(@NonNull ImageSource<T> imageSource) {
        String key = imageSource.getKey();
        MemoryImageCache memoryImageCache = getMemoryImageCache();
        if (memoryImageCache != null) {
            memoryImageCache.remove(key);
        }
        StorageImageCache storageImageCache = getStorageImageCache();
        if (storageImageCache != null) {
            storageImageCache.remove(key);
        }
    }

    @Nullable
    public Bitmap getPlaceholderImage() {
        return mPlaceholderBitmap;
    }

    public void setPlaceholderImage(@Nullable Bitmap bitmap) {
        mPlaceholderBitmap = bitmap;
    }

    public void setPlaceholderImage(int resourceId) {
        mPlaceholderBitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId);
    }

    @Nullable
    public BitmapLoader<T> getBitmapLoader() {
        return mBitmapLoader;
    }

    public void setBitmapLoader(@Nullable BitmapLoader<T> bitmapLoader) {
        mBitmapLoader = bitmapLoader;
    }

    @Nullable
    public MemoryImageCache getMemoryImageCache() {
        return mMemoryImageCache;
    }

    public void setMemoryImageCache(@Nullable MemoryImageCache memoryImageCache) {
        mMemoryImageCache = memoryImageCache;
    }

    @Nullable
    public StorageImageCache getStorageImageCache() {
        return mStorageImageCache;
    }

    public void setStorageImageCache(@Nullable StorageImageCache storageImageCache) {
        mStorageImageCache = storageImageCache;
    }

    public boolean isImageFadeIn() {
        return mImageFadeIn;
    }

    public void setImageFadeIn(boolean imageFadeIn) {
        mImageFadeIn = imageFadeIn;
    }

    public boolean isExitTasksEarly() {
        return mExitTasksEarly;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    public int getImageFadeInTime() {
        return mImageFadeInTime;
    }

    public void setImageFadeInTime(int imageFadeInTime) {
        mImageFadeInTime = imageFadeInTime;
    }

    public void clearCache() {
        MemoryImageCache memoryImageCache = getMemoryImageCache();
        if (memoryImageCache != null) {
            memoryImageCache.clear();
        }
        StorageImageCache storageImageCache = getStorageImageCache();
        if (storageImageCache != null) {
            storageImageCache.clear();
        }
    }

    @Nullable
    protected static LoadImageAction<?> getLoadImageAction(@Nullable ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncBitmapDrawable) {
                return ((AsyncBitmapDrawable) drawable).getLoadImageAction();
            }
        }
        return null;
    }

    protected static void cancelWork(@Nullable ImageView imageView) {
        LoadImageAction<?> loadImageAction = getLoadImageAction(imageView);
        if (loadImageAction != null) {
            loadImageAction.cancel();
        }
    }

    protected static boolean cancelPotentialWork(@NonNull ImageSource<?> imageSource,
            @Nullable ImageView imageView) {
        LoadImageAction<?> loadImageAction = getLoadImageAction(imageView);
        if (loadImageAction != null) {
            if (!Objects.equals(loadImageAction.getImageSource().getKey(), imageSource.getKey())) {
                loadImageAction.cancel();
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Fraction of maximum number of bytes heap can expand to
     *
     * @param fraction Fraction
     * @return Number of bytes
     */
    public static int getMaxMemoryFraction(float fraction) {
        if (fraction < 0.1F || fraction > 0.8F) {
            throw new IllegalArgumentException(
                    ImageLoaderConstants.MemoryImageCache.FRACTION_RANGE_ERROR_MESSAGE);
        }
        return Math.round(fraction * Runtime.getRuntime().maxMemory());
    }

    /**
     * Fraction of free storage space in specified path
     *
     * @param path     Path
     * @param fraction Fraction
     * @return Number of free bytes
     */
    public static long getFreeStorageFraction(@NonNull File path, double fraction) {
        if (fraction < 0.01D || fraction > 1.0D) {
            throw new IllegalArgumentException(
                    ImageLoaderConstants.StorageImageCache.FRACTION_RANGE_ERROR_MESSAGE);
        }
        return Math.round(CommonUtils.getFreeBytes(path) * fraction);
    }

    /**
     * Fraction of total storage space in specified path
     *
     * @param path     Path
     * @param fraction Fraction
     * @return Number of free bytes
     */
    public static long getTotalStorageFraction(@NonNull File path, double fraction) {
        if (fraction < 0.01D || fraction > 1.0D) {
            throw new IllegalArgumentException(
                    ImageLoaderConstants.StorageImageCache.FRACTION_RANGE_ERROR_MESSAGE);
        }
        return Math.round(CommonUtils.getTotalBytes(path) * fraction);
    }

    /**
     * Calculate sample size for required size from source size
     * Sample size is the number of pixels in either dimension that
     * correspond to a single pixel
     *
     * @param sourceWidth               Source width
     * @param sourceHeight              Source height
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Sample size
     */
    public static int calculateSampleSize(int sourceWidth, int sourceHeight, int requiredWidth,
            int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        int sampleSize = 1;
        if (sourceWidth > requiredWidth || sourceHeight > requiredHeight) {
            int halfWidth = sourceWidth / 2;
            int halfHeight = sourceHeight / 2;
            while ((halfWidth / sampleSize) > requiredWidth &&
                    (halfHeight / sampleSize) > requiredHeight) {
                sampleSize *= 2;
            }
            if (ignoreTotalNumberOfPixels) {
                return sampleSize;
            }
            long totalPixels = (sourceWidth * sourceHeight) / (sampleSize * sampleSize);
            long totalRequiredPixels = requiredWidth * requiredHeight;
            while (totalPixels > totalRequiredPixels) {
                sampleSize *= 2;
                totalPixels /= 4L;
            }
        }
        return sampleSize;
    }

    /**
     * Crop {@link Bitmap} to specified size
     *
     * @param bitmap Source bitmap
     * @param width  Result width
     * @param height Result height
     * @return Cropped bitmap
     */
    @NonNull
    public static Bitmap cropBitmap(@NonNull Bitmap bitmap, int width, int height) {
        int w = width;
        int h = height;
        while (w > 0 && h > 0) {
            if (w > h) {
                w %= h;
            } else {
                h %= w;
            }
        }
        int d = w + h;
        int arW = width / d;
        int arH = height / d;
        final int CROP_MODE_NONE = 1;
        final int CROP_MODE_WIDTH = 2;
        final int CROP_MODE_HEIGHT = 3;
        int cropMode = CROP_MODE_HEIGHT;
        int resultH = bitmap.getHeight();
        int resultW = arW * resultH / arH;
        if (resultW > bitmap.getWidth()) {
            cropMode = CROP_MODE_WIDTH;
            resultW = bitmap.getWidth();
            resultH = arH * resultW / arW;
        }
        if (resultH == bitmap.getHeight() && resultW == bitmap.getWidth()) {
            cropMode = CROP_MODE_NONE;
        }
        Bitmap croppedBitmap = null;
        switch (cropMode) {
            case CROP_MODE_NONE:
                croppedBitmap = bitmap;
                break;
            case CROP_MODE_HEIGHT:
                croppedBitmap =
                        Bitmap.createBitmap(bitmap, (bitmap.getWidth() - resultW) / 2, 0, resultW,
                                resultH);
                break;
            case CROP_MODE_WIDTH:
                croppedBitmap =
                        Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - resultH) / 2, resultW,
                                resultH);
                break;
        }
        return Bitmap.createScaledBitmap(croppedBitmap, width, height, true);
    }

    /**
     * Round {@link Bitmap} corners
     *
     * @param bitmap Bitmap
     * @param radius Corner radius
     * @return Rounded bitmap
     */
    @NonNull
    public static Bitmap roundBitmapCorners(@NonNull Bitmap bitmap, float radius) {
        int maskColor = 0xff424242;
        Paint cornerPaint = new Paint();
        cornerPaint.setAntiAlias(true);
        cornerPaint.setColor(maskColor);
        Rect rcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rcRectF = new RectF(rcRect);
        Bitmap rcBitmap =
                Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas rcCanvas = new Canvas(rcBitmap);
        rcCanvas.drawARGB(0, 0, 0, 0);
        rcCanvas.drawRoundRect(rcRectF, radius, radius, cornerPaint);
        cornerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        rcCanvas.drawBitmap(bitmap, rcRect, rcRect, cornerPaint);
        return rcBitmap;
    }

    /**
     * Rotate {@link Bitmap} by specified amount of degrees
     *
     * @param bitmap  Bitmap
     * @param degrees Amount of degrees
     * @return Rotated bitmap
     */
    @NonNull
    public static Bitmap rotateBitmap(@NonNull Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        return Bitmap
                .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Load sampled bitmap from uri
     *
     * @param context                   Context
     * @param uri                       Uri
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromUri(@NonNull Context context, @NonNull Uri uri,
            int requiredWidth, int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = CommonUtils.getDataStreamFromUri(context, uri)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        try (InputStream inputStream = CommonUtils.getDataStreamFromUri(context, uri)) {
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Loading sampled bitmap from file
     *
     * @param file                      File
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromFile(@NonNull File file, int requiredWidth,
            int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = new FileInputStream(file)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        try (InputStream inputStream = new FileInputStream(file)) {
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Load sampled bitmap from file descriptor
     *
     * @param fileDescriptor            File descriptor
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromFileDescriptor(@NonNull FileDescriptor fileDescriptor,
            int requiredWidth, int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = new FileInputStream(fileDescriptor)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        try (InputStream inputStream = new FileInputStream(fileDescriptor)) {
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Load sampled bitmap from resource
     *
     * @param resources                 Resources
     * @param resourceId                Resource id
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromResource(@NonNull Resources resources, int resourceId,
            int requiredWidth, int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        TypedValue typedValue = new TypedValue();
        options.inJustDecodeBounds = true;
        options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
        try (InputStream inputStream = resources.openRawResource(resourceId, typedValue)) {
            if (typedValue.density == TypedValue.DENSITY_DEFAULT) {
                options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            } else if (typedValue.density != TypedValue.DENSITY_NONE) {
                options.inDensity = typedValue.density;
            }
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        try (InputStream inputStream = resources.openRawResource(resourceId, typedValue)) {
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Load sampled bitmap from byte array
     *
     * @param byteArray                 Byte array
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromByteArray(@NonNull byte[] byteArray,
            int requiredWidth, int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
    }

    /**
     * Create new common bitmap loader for uris
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<Uri> newUriBitmapLoader() {
        return new BitmapLoader<Uri>() {
            @Override
            public Bitmap load(@NonNull Context context, Uri data) {
                return loadSampledBitmapFromUri(context, data, Integer.MAX_VALUE, Integer.MAX_VALUE,
                        true);
            }
        };
    }

    /**
     * Create new common bitmap loader for files
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<File> newFileBitmapLoader() {
        return new BitmapLoader<File>() {
            @Override
            public Bitmap load(@NonNull Context context, File data) {
                return loadSampledBitmapFromFile(data, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
            }
        };
    }

    /**
     * Create new common bitmap loader for file descriptors
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<FileDescriptor> newFileDescriptorBitmapLoader() {
        return new BitmapLoader<FileDescriptor>() {
            @Override
            public Bitmap load(@NonNull Context context, FileDescriptor data) {
                return loadSampledBitmapFromFileDescriptor(data, Integer.MAX_VALUE,
                        Integer.MAX_VALUE, true);
            }
        };
    }

    /**
     * Create new common bitmap loader for resources
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<Integer> newResourceBitmapLoader() {
        return new BitmapLoader<Integer>() {
            @Override
            public Bitmap load(@NonNull Context context, Integer data) {
                return loadSampledBitmapFromResource(context.getResources(), data,
                        Integer.MAX_VALUE, Integer.MAX_VALUE, true);
            }
        };
    }

    /**
     * Create new common bitmap loader for byte arrays
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<byte[]> newByteArrayBitmapLoader() {
        return new BitmapLoader<byte[]>() {
            @Override
            public Bitmap load(@NonNull Context context, byte[] data) {
                return loadSampledBitmapFromByteArray(data, Integer.MAX_VALUE, Integer.MAX_VALUE,
                        true);
            }
        };
    }

    /**
     * Create new common image source that is usable in most cases
     *
     * @param data Source data
     * @param <T>  Source data type
     * @return Image source
     */
    @NonNull
    public static <T> ImageSource<T> newImageSource(@NonNull final T data) {
        final String key = CommonUtils.generateMD5(String.valueOf(data));
        return new ImageSource<T>() {
            @Override
            public T getData() {
                return data;
            }

            @NonNull
            @Override
            public String getKey() {
                return key;
            }
        };
    }

    /**
     * Create memory image cache with specified maximum size
     *
     * @param maxSize Maximum size in bytes
     * @return Memory image cache
     */
    @NonNull
    public static MemoryImageCache newMemoryImageCache(int maxSize) {
        return new MemoryImageCacheImplementation(maxSize);
    }

    /**
     * Create memory image cache with maximum size 25% of
     * total available memory fraction
     *
     * @return Memory image cache
     */
    @NonNull
    public static MemoryImageCache newMemoryImageCache() {
        return newMemoryImageCache(
                getMaxMemoryFraction(ImageLoaderConstants.MemoryImageCache.DEFAULT_FRACTION));
    }

    /**
     * Create storage image cache with specified parameters
     *
     * @param directory       Directory
     * @param maxSize         Maximum size
     * @param compressFormat  Compress format
     * @param compressQuality Compress quality
     * @return Storage image cache
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    public static StorageImageCache newStorageImageCache(@NonNull File directory, long maxSize,
            @NonNull Bitmap.CompressFormat compressFormat, int compressQuality) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new StorageImageCacheImplementation(directory, maxSize, compressFormat,
                compressQuality);
    }

    /**
     * Create storage image cache in specified directory with maximum size 10%
     * of total storage size
     *
     * @param directory Cache directory
     * @return Storage image cache
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    public static StorageImageCache newStorageImageCache(@NonNull File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return newStorageImageCache(directory, getTotalStorageFraction(directory,
                ImageLoaderConstants.StorageImageCache.DEFAULT_FRACTION),
                ImageLoaderConstants.StorageImageCache.DEFAULT_FORMAT,
                ImageLoaderConstants.StorageImageCache.DEFAULT_QUALITY);
    }

    /**
     * Create storage image cache in default application cache directory with maximum size 10%
     * of total storage size
     *
     * @param context Context
     * @return Storage image cache
     */
    @NonNull
    public static StorageImageCache newStorageImageCache(@NonNull Context context) {
        return newStorageImageCache(getDefaultStorageCacheDirectory(context));
    }

    /**
     * Default storage image cache directory
     *
     * @param context Context
     * @return Cache directory
     */
    @NonNull
    public static File getDefaultStorageCacheDirectory(@NonNull Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return new File(cacheDir, ImageLoaderConstants.StorageImageCache.DEFAULT_DIRECTORY);
    }
}