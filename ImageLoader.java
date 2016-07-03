/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final Thread mMainThread;
    private final Handler mMainThreadHandler;
    private final ExecutorService mAsyncExecutor;
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
        Looper mainLooper = Looper.getMainLooper();
        mMainThread = mainLooper.getThread();
        mMainThreadHandler = new Handler(mainLooper);
        int poolSize = Runtime.getRuntime().availableProcessors();
        if (poolSize > 1) {
            poolSize--;
        }
        mAsyncExecutor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                Thread thread = new Thread(runnable, "ImageLoader-background-thread");
                if (thread.isDaemon()) {
                    thread.setDaemon(false);
                }
                if (thread.getPriority() != Thread.NORM_PRIORITY) {
                    thread.setPriority(Thread.NORM_PRIORITY);
                }
                return thread;
            }
        });
    }

    protected void runOnMainThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() == mMainThread) {
            runnable.run();
        } else {
            runOnMainThread(runnable, 0);
        }
    }

    protected void runOnMainThread(@NonNull Runnable runnable, long delay) {
        mMainThreadHandler.postDelayed(runnable, delay);
    }

    @NonNull
    protected ExecutorService getAsyncExecutor() {
        return mAsyncExecutor;
    }

    @NonNull
    protected Context getContext() {
        return mContext;
    }

    protected boolean isPauseWork() {
        return mPauseWork;
    }

    protected void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!isPauseWork()) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    /**
     * Load image to imageView from imageSource
     *
     * @param imageSource Image source
     * @param imageView   Image view
     * @param callback    Optional callback
     */
    public void loadImage(@NonNull ImageSource<T> imageSource, @NonNull ImageView imageView,
            @Nullable Callback callback) {
        BitmapDrawable drawable = null;
        MemoryImageCache memoryImageCache = getMemoryImageCache();
        if (memoryImageCache != null) {
            drawable = memoryImageCache.get(imageSource.getKey());
        }
        if (drawable != null) {
            if (callback != null) {
                Bitmap image = drawable.getBitmap();
                if (image != null) {
                    callback.onImageLoaded(image, true, false);
                }
            }
            runOnMainThread(new SimpleSetImageAction(imageView, drawable, callback));
        } else if (cancelPotentialWork(imageSource, imageView)) {
            LoadImageAction<T> loadAction =
                    new LoadImageAction<>(imageSource, imageView, callback, this);
            AsyncBitmapDrawable asyncBitmapDrawable =
                    new AsyncBitmapDrawable(getContext().getResources(), getPlaceholderImage(),
                            loadAction);
            runOnMainThread(new SimpleSetImageAction(imageView, asyncBitmapDrawable, null));
            loadAction.execute(getAsyncExecutor());
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
            ImageSource<?> actionImageSource = loadImageAction.mImageSource;
            if (actionImageSource == null ||
                    !Objects.equals(actionImageSource.getKey(), imageSource.getKey())) {
                loadImageAction.cancel();
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Generate MD5 hash string for specified data
     *
     * @param data Data
     * @return MD5 hash string
     */
    @NonNull
    protected static String generateMD5(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data);
            BigInteger bigInteger = new BigInteger(1, messageDigest.digest());
            return bigInteger.toString(Character.MAX_RADIX);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fraction of maximum number of bytes heap can expand to
     *
     * @param fraction Fraction
     * @return Number of bytes
     */
    protected static int getMaxMemoryFraction(float fraction) {
        if (fraction < 0.1F || fraction > 0.8F) {
            throw new IllegalArgumentException(
                    "Argument \"fraction\" must be between 0.1 and 0.8 (inclusive)");
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
    protected static long getFreeStorageFraction(@NonNull File path, double fraction) {
        if (fraction < 0.01D || fraction > 1.0D) {
            throw new IllegalArgumentException(
                    "Argument \"fraction\" must be between 0.01 and 1.0 (inclusive)");
        }
        StatFs stat = new StatFs(path.getAbsolutePath());
        double bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
        return Math.round(bytesAvailable * fraction);
    }

    /**
     * Fraction of total storage space in specified path
     *
     * @param path     Path
     * @param fraction Fraction
     * @return Number of free bytes
     */
    protected static long getTotalStorageFraction(@NonNull File path, double fraction) {
        if (fraction < 0.01D || fraction > 1.0D) {
            throw new IllegalArgumentException(
                    "Argument \"fraction\" must be between 0.01 and 1.0 (inclusive)");
        }
        StatFs stat = new StatFs(path.getAbsolutePath());
        return Math.round(stat.getTotalBytes() * fraction);
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
    protected static int calculateSampleSize(int sourceWidth, int sourceHeight, int requiredWidth,
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
     * Get input stream from uri
     *
     * @param context Context
     * @param uri     Uri
     * @return Data stream
     * @throws IOException
     */
    @Nullable
    protected static InputStream getDataStreamFromUri(@NonNull Context context,
            @NonNull Uri uri) throws IOException {
        String scheme = uri.getScheme();
        if (Constants.Uri.SCHEME_HTTP.equalsIgnoreCase(scheme) ||
                Constants.Uri.SCHEME_HTTPS.equalsIgnoreCase(scheme) ||
                Constants.Uri.SCHEME_FTP.equalsIgnoreCase(scheme)) {
            return new URL(uri.toString()).openConnection().getInputStream();
        } else {
            return context.getContentResolver().openInputStream(uri);
        }
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
        try (InputStream inputStream = getDataStreamFromUri(context, uri)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        try (InputStream inputStream = getDataStreamFromUri(context, uri)) {
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
        final String key = generateMD5(String.valueOf(data).getBytes());
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
     * Create memory image cache with maximum size equal to specified
     * total available memory fraction
     *
     * @param totalMemoryFraction Fraction
     * @return Memory image cache
     */
    @NonNull
    public static MemoryImageCache newMemoryImageCache(float totalMemoryFraction) {
        return new MemoryImageCacheImplementation(getMaxMemoryFraction(totalMemoryFraction));
    }

    /**
     * Create memory image cache with maximum size 25% of
     * total available memory fraction
     *
     * @return Memory image cache
     */
    @NonNull
    public static MemoryImageCache newMemoryImageCache() {
        return newMemoryImageCache(Constants.MemoryImageCache.DEFAULT_FRACTION);
    }

    /**
     * Create storage image cache in specified directory with maximum size 10%
     * of total storage size
     *
     * @param directory Cache directory
     * @return Storage image cache
     */
    @NonNull
    public static StorageImageCache newStorageImageCache(@NonNull File directory) {
        return new StorageImageCacheImplementation(directory,
                getTotalStorageFraction(directory, Constants.StorageImageCache.DEFAULT_FRACTION),
                Constants.StorageImageCache.DEFAULT_FORMAT,
                Constants.StorageImageCache.DEFAULT_QUALITY);
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
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return newStorageImageCache(
                new File(cacheDir, Constants.StorageImageCache.DEFAULT_DIRECTORY));
    }

    protected static final class Constants {
        private Constants() {
        }

        public static final class Uri {
            public static final String SCHEME_HTTP = "http";
            public static final String SCHEME_HTTPS = "https";
            public static final String SCHEME_FTP = "ftp";

            private Uri() {
            }
        }

        public static final class MemoryImageCache {
            public static final float DEFAULT_FRACTION = 0.25F;

            private MemoryImageCache() {
            }
        }

        public static final class StorageImageCache {
            public static final String DEFAULT_DIRECTORY = "image_loader_cache";
            public static final double DEFAULT_FRACTION = 0.1D;
            public static final Bitmap.CompressFormat DEFAULT_FORMAT = Bitmap.CompressFormat.JPEG;
            public static final int DEFAULT_QUALITY = 80;

            private StorageImageCache() {
            }
        }
    }

    protected static final class LoadImageAction<T> {
        private final ImageSource<T> mImageSource;
        private final WeakReference<ImageView> mImageViewReference;
        private final ImageLoader<T> mImageLoader;
        private final Callback mCallback;
        private final AtomicBoolean mSubmitted = new AtomicBoolean();
        private volatile boolean mFinished;
        private volatile boolean mCancelled;
        private volatile Future<Void> mFuture;

        public LoadImageAction(ImageSource<T> imageSource, ImageView imageView, Callback callback,
                ImageLoader<T> imageLoader) {
            mImageSource = imageSource;
            mImageViewReference = new WeakReference<>(imageView);
            mCallback = callback;
            mImageLoader = imageLoader;
        }

        private void loadImage() {
            Bitmap image = null;
            synchronized (mImageLoader.mPauseWorkLock) {
                while (mImageLoader.isPauseWork() && !isCancelled()) {
                    try {
                        mImageLoader.mPauseWorkLock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            if (!isCancelled() && getAttachedImageView() != null &&
                    !mImageLoader.isExitTasksEarly()) {
                StorageImageCache storageImageCache = mImageLoader.getStorageImageCache();
                if (storageImageCache != null) {
                    image = storageImageCache.get(mImageSource.getKey());
                    if (image != null && mCallback != null) {
                        mCallback.onImageLoaded(image, false, true);
                    }
                }
                if (image == null) {
                    BitmapLoader<T> bitmapLoader = mImageLoader.getBitmapLoader();
                    if (bitmapLoader != null) {
                        image = bitmapLoader
                                .load(mImageLoader.getContext(), mImageSource.getData());
                    }
                    if (image != null) {
                        if (mCallback != null) {
                            mCallback.onImageLoaded(image, false, false);
                        }
                        if (storageImageCache != null) {
                            storageImageCache.put(mImageSource.getKey(), image);
                        }
                    }
                }
            }
            RecyclingBitmapDrawable drawable = null;
            if (image != null) {
                drawable = new RecyclingBitmapDrawable(mImageLoader.getContext().getResources(),
                        image);
                MemoryImageCache memoryImageCache = mImageLoader.getMemoryImageCache();
                if (memoryImageCache != null) {
                    memoryImageCache.put(mImageSource.getKey(), drawable);
                }
            }
            mImageLoader
                    .runOnMainThread(new SetImageAction(drawable, mImageLoader, mCallback, this));
        }

        @Nullable
        public ImageView getAttachedImageView() {
            ImageView imageView = mImageViewReference.get();
            LoadImageAction<?> loadImageAction = getLoadImageAction(imageView);
            if (this == loadImageAction) {
                return imageView;
            }
            return null;
        }

        public boolean execute(@NonNull ExecutorService executor) {
            if (!isCancelled() && !isFinished() && mSubmitted.compareAndSet(false, true)) {
                mFuture = executor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        loadImage();
                        mFinished = true;
                        mSubmitted.set(false);
                        return null;
                    }
                });
                return true;
            } else {
                return false;
            }
        }

        public void cancel() {
            if (mFuture != null) {
                mFuture.cancel(true);
            }
            mCancelled = true;
            synchronized (mImageLoader.mPauseWorkLock) {
                mImageLoader.mPauseWorkLock.notifyAll();
            }
        }

        public boolean reset() {
            if (isFinished() || isCancelled()) {
                mFuture = null;
                mFinished = false;
                mCancelled = false;
                mSubmitted.set(false);
                return true;
            } else {
                return false;
            }
        }

        public boolean isFinished() {
            return mFinished;
        }

        public boolean isCancelled() {
            return mCancelled;
        }
    }

    protected static final class SetImageAction implements Runnable {
        private final BitmapDrawable mBitmapDrawable;
        private final ImageLoader<?> mImageLoader;
        private final LoadImageAction<?> mLoadImageAction;
        private final Callback mCallback;

        public SetImageAction(@Nullable BitmapDrawable bitmapDrawable,
                @NonNull ImageLoader<?> imageLoader, @Nullable Callback callback,
                @NonNull LoadImageAction<?> loadImageAction) {
            mBitmapDrawable = bitmapDrawable;
            mImageLoader = imageLoader;
            mCallback = callback;
            mLoadImageAction = loadImageAction;
        }

        @Override
        public void run() {
            if (!mLoadImageAction.isCancelled() && !mImageLoader.isExitTasksEarly()) {
                final ImageView imageView = mLoadImageAction.getAttachedImageView();
                if (mBitmapDrawable == null || imageView == null) {
                    return;
                }
                if (mImageLoader.isImageFadeIn()) {
                    FadeDrawable fadeDrawable =
                            new FadeDrawable(new ColorDrawable(Color.TRANSPARENT), mBitmapDrawable);
                    imageView.setBackground(
                            new BitmapDrawable(mImageLoader.getContext().getResources(),
                                    mImageLoader.getPlaceholderImage()));
                    imageView.setImageDrawable(fadeDrawable);
                    fadeDrawable.setFadeCallback(new FadeDrawable.FadeCallback() {
                        @Override
                        public void onStart(FadeDrawable drawable) {
                        }

                        @Override
                        public void onEnd(FadeDrawable drawable) {
                            if (mCallback != null) {
                                mCallback.onImageDisplayed(imageView);
                            }
                        }
                    });
                    fadeDrawable.startFade(mImageLoader.getImageFadeInTime());
                } else {
                    imageView.setImageDrawable(mBitmapDrawable);
                    if (mCallback != null) {
                        mCallback.onImageDisplayed(imageView);
                    }
                }
            }
        }
    }

    protected static final class SimpleSetImageAction implements Runnable {
        private final ImageView mImageView;
        private final BitmapDrawable mBitmapDrawable;
        private final Callback mCallback;

        public SimpleSetImageAction(@Nullable ImageView imageView,
                @Nullable BitmapDrawable bitmapDrawable, @Nullable Callback callback) {
            mImageView = imageView;
            mBitmapDrawable = bitmapDrawable;
            mCallback = callback;
        }

        @Override
        public void run() {
            if (mBitmapDrawable == null || mImageView == null) {
                return;
            }
            mImageView.setImageDrawable(mBitmapDrawable);
            if (mCallback != null) {
                mCallback.onImageDisplayed(mImageView);
            }
        }
    }

    protected static final class AsyncBitmapDrawable extends BitmapDrawable {
        private final WeakReference<LoadImageAction<?>> mLoadImageActionReference;

        public AsyncBitmapDrawable(@Nullable Resources res, @Nullable Bitmap bitmap,
                @NonNull LoadImageAction<?> loadImageAction) {
            super(res, bitmap);
            mLoadImageActionReference = new WeakReference<LoadImageAction<?>>(loadImageAction);
        }

        public LoadImageAction<?> getLoadImageAction() {
            return mLoadImageActionReference.get();
        }
    }

    protected static class MemoryImageCacheImplementation implements MemoryImageCache {
        private final LruCache<String, BitmapDrawable> mCache;

        /**
         * Memory image cache
         *
         * @param size Size in bytes
         */
        public MemoryImageCacheImplementation(int size) {
            mCache = new LruCache<String, BitmapDrawable>(size) {
                @Override
                protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue,
                        BitmapDrawable newValue) {
                    if (oldValue instanceof RecyclingBitmapDrawable) {
                        ((RecyclingBitmapDrawable) oldValue).setCached(false);
                    }
                }

                @Override
                protected int sizeOf(String key, BitmapDrawable value) {
                    return value.getBitmap().getAllocationByteCount();
                }
            };
        }

        @Override
        public void put(@NonNull String key, @NonNull BitmapDrawable value) {
            if (value instanceof RecyclingBitmapDrawable) {
                ((RecyclingBitmapDrawable) value).setCached(true);
            }
            mCache.put(key, value);
        }

        @Nullable
        @Override
        public BitmapDrawable get(@NonNull String key) {
            return mCache.get(key);
        }

        @Override
        public void remove(@NonNull String key) {
            mCache.remove(key);
        }

        @Override
        public void clear() {
            mCache.evictAll();
        }
    }

    protected static class StorageImageCacheImplementation implements StorageImageCache {
        private final Object mCacheSizeLock = new Object();
        private final File mDirectory;
        private final long mMaxSize;
        private final Bitmap.CompressFormat mCompressFormat;
        private final int mCompressQuality;

        /**
         * Storage image cache
         *
         * @param directory       Cache directory
         * @param maxSize         Maximum size in bytes
         * @param compressFormat  Bitmap compress format
         * @param compressQuality Bitmap compress quality
         */
        public StorageImageCacheImplementation(@NonNull File directory, long maxSize,
                @NonNull Bitmap.CompressFormat compressFormat, int compressQuality) {
            mDirectory = directory;
            mMaxSize = maxSize;
            mCompressFormat = compressFormat;
            mCompressQuality = compressQuality;
            fitCacheSize();
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void fitCacheSize() {
            synchronized (mCacheSizeLock) {
                File[] files = mDirectory.listFiles();
                if (files.length < 2) {
                    return;
                }
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        return Long.signum(rhs.lastModified() - lhs.lastModified());
                    }
                });
                long size = 0;
                for (File file : files) {
                    size += file.length();
                }
                for (int i = files.length - 1; size > mMaxSize && i >= 0; i--) {
                    File removing = files[i];
                    size -= removing.length();
                    removing.delete();
                }
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void put(@NonNull String key, @NonNull Bitmap value) {
            if (!mDirectory.exists()) {
                mDirectory.mkdirs();
            }
            try (OutputStream outputStream = new FileOutputStream(new File(mDirectory, key))) {
                value.compress(mCompressFormat, mCompressQuality, outputStream);
            } catch (IOException e) {
                clear();
            }
            fitCacheSize();
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
        @Override
        public Bitmap get(@NonNull String key) {
            File file = new File(mDirectory, key);
            if (!file.exists()) {
                return null;
            }
            file.setLastModified(System.currentTimeMillis());
            try (InputStream inputStream = new FileInputStream(file)) {
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                return null;
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void remove(@NonNull String key) {
            File file = new File(mDirectory, key);
            if (file.exists()) {
                file.delete();
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void clear() {
            File[] files = mDirectory.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static class FadeDrawable extends LayerDrawable implements Drawable.Callback {
        private static final int FADE_NONE = 0;
        private static final int FADE_RUNNING = 1;
        private static final int FADE_DONE = 2;
        private static final int START_DRAWABLE = 0;
        private static final int END_DRAWABLE = 1;
        private static final int MAX_ALPHA = 255;
        private int mFadeState = FADE_NONE;
        private long mStartTime;
        private long mDuration;
        private FadeCallback mFadeCallback;

        /**
         * Fade drawable
         *
         * @param startDrawable start drawable
         * @param endDrawable   end drawable
         */
        public FadeDrawable(@Nullable Drawable startDrawable, @Nullable Drawable endDrawable) {
            super(new Drawable[]{startDrawable, endDrawable});
            setId(START_DRAWABLE, START_DRAWABLE);
            setId(END_DRAWABLE, END_DRAWABLE);
        }

        private void draw(Canvas canvas, Drawable drawable, int alpha) {
            int originalAlpha = drawable.getAlpha();
            drawable.setAlpha(alpha);
            drawable.draw(canvas);
            drawable.setAlpha(originalAlpha);
        }

        @Override
        public void draw(Canvas canvas) {
            if (mFadeState == FADE_RUNNING) {
                int alpha = Math.min((int) Math
                        .ceil(MAX_ALPHA * (float) (System.currentTimeMillis() - mStartTime) /
                                mDuration), MAX_ALPHA);
                Drawable startDrawable = getStartDrawable();
                if (startDrawable != null) {
                    draw(canvas, startDrawable, MAX_ALPHA - alpha);
                }
                Drawable endDrawable = getEndDrawable();
                if (endDrawable != null) {
                    draw(canvas, endDrawable, alpha);
                }
                if (alpha == MAX_ALPHA) {
                    mFadeState = FADE_DONE;
                    FadeCallback fadeCallback = getFadeCallback();
                    if (fadeCallback != null) {
                        fadeCallback.onEnd(this);
                    }
                } else {
                    invalidateSelf();
                }
            } else if (mFadeState == FADE_NONE) {
                Drawable startDrawable = getStartDrawable();
                if (startDrawable != null) {
                    draw(canvas, startDrawable, MAX_ALPHA);
                }
            } else if (mFadeState == FADE_DONE) {
                Drawable endDrawable = getEndDrawable();
                if (endDrawable != null) {
                    draw(canvas, endDrawable, MAX_ALPHA);
                }
            }
        }

        /**
         * Start fade
         *
         * @param duration Fade duration
         */
        public void startFade(int duration) {
            mDuration = duration;
            mStartTime = System.currentTimeMillis();
            mFadeState = FADE_RUNNING;
            FadeCallback fadeCallback = mFadeCallback;
            if (fadeCallback != null) {
                fadeCallback.onStart(this);
            }
            invalidateSelf();
        }

        public void resetFade() {
            mFadeState = FADE_NONE;
            invalidateSelf();
        }

        @Nullable
        public Drawable getStartDrawable() {
            return getDrawable(START_DRAWABLE);
        }

        public void setStartDrawable(@Nullable Drawable startDrawable) {
            setDrawableByLayerId(START_DRAWABLE, startDrawable);
        }

        @Nullable
        public Drawable getEndDrawable() {
            return getDrawable(END_DRAWABLE);
        }

        public void setEndDrawable(@Nullable Drawable endDrawable) {
            setDrawableByLayerId(END_DRAWABLE, endDrawable);
        }

        @Nullable
        public FadeCallback getFadeCallback() {
            return mFadeCallback;
        }

        public void setFadeCallback(@Nullable FadeCallback fadeCallback) {
            mFadeCallback = fadeCallback;
        }

        public interface FadeCallback {
            void onStart(FadeDrawable drawable);

            void onEnd(FadeDrawable drawable);
        }
    }

    /**
     * BitmapDrawable that recycles it's bitmap.
     */
    public static class RecyclingBitmapDrawable extends BitmapDrawable {
        private int mDisplayReferencesCount;
        private int mCacheReferencesCount;
        private boolean mHasBeenDisplayed;

        public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        public RecyclingBitmapDrawable(Resources res, String filepath) {
            super(res, filepath);
        }

        public RecyclingBitmapDrawable(Resources res, InputStream is) {
            super(res, is);
        }

        private void checkStateAndRecycleIfNeeded() {
            Bitmap bitmap = getBitmap();
            if (bitmap != null && !bitmap.isRecycled() && mCacheReferencesCount <= 0 &&
                    mDisplayReferencesCount <= 0 && mHasBeenDisplayed) {
                bitmap.recycle();
            }
        }

        public synchronized void setDisplayed(boolean displayed) {
            if (displayed) {
                mDisplayReferencesCount++;
                mHasBeenDisplayed = true;
            } else {
                mCacheReferencesCount--;
            }
            checkStateAndRecycleIfNeeded();
        }

        public synchronized void setCached(boolean cached) {
            if (cached) {
                mCacheReferencesCount++;
            } else {
                mCacheReferencesCount--;
            }
            checkStateAndRecycleIfNeeded();
        }
    }

    /**
     * ImageView that notifies RecyclingBitmapDrawable when image drawable is changed.
     */
    public static class RecyclingImageView extends ImageView {
        private boolean mClearDrawableOnDetach;

        public RecyclingImageView(Context context) {
            super(context);
        }

        public RecyclingImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr,
                int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        private void notifyDrawable(Drawable drawable, boolean displayed) {
            if (drawable instanceof RecyclingBitmapDrawable) {
                ((RecyclingBitmapDrawable) drawable).setDisplayed(displayed);
            } else if (drawable instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) drawable;
                for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                    notifyDrawable(layerDrawable.getDrawable(i), displayed);
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            if (mClearDrawableOnDetach) {
                setImageDrawable(null);
            }
            super.onDetachedFromWindow();
        }

        @Override
        public void setImageDrawable(Drawable drawable) {
            Drawable previousDrawable = getDrawable();
            super.setImageDrawable(drawable);
            notifyDrawable(drawable, true);
            notifyDrawable(previousDrawable, false);
        }

        /**
         * Clear image drawable when detached from window. Set true if RecyclingImageView is used
         * with component which doesn't reuse views.
         *
         * @param clearDrawableOnDetach Clear drawable on detach
         */
        public void setClearDrawableOnDetach(boolean clearDrawableOnDetach) {
            mClearDrawableOnDetach = clearDrawableOnDetach;
        }
    }

    /**
     * Memory image cache
     */
    public interface MemoryImageCache {
        void put(@NonNull String key, @NonNull BitmapDrawable value);

        @Nullable
        BitmapDrawable get(@NonNull String key);

        void remove(@NonNull String key);

        void clear();
    }

    /**
     * Storage image cache
     */
    public interface StorageImageCache {
        void put(@NonNull String key, @NonNull Bitmap value);

        @Nullable
        Bitmap get(@NonNull String key);

        void remove(@NonNull String key);

        void clear();
    }

    /**
     * Source data and key to cache loaded bitmaps
     *
     * @param <T> Type of source data
     */
    public interface ImageSource<T> {
        /**
         * Your source data from which you will load bitmaps
         * in loadBitmap method of ImageLoader class
         *
         * @return Source data
         */
        T getData();

        /**
         * Must be unique for each image source. If you want to use storage caching, ensure that
         * returned value doesn't contain symbols that can't be used in file name
         *
         * @return Unique key
         */
        @NonNull
        String getKey();
    }

    /**
     * Bitmap loader from concrete source data type
     *
     * @param <T> Source data type
     */
    public interface BitmapLoader<T> {
        /**
         * Load image bitmap from source data
         *
         * @param data Source data
         * @return Loaded bitmap
         */
        Bitmap load(@NonNull Context context, T data);
    }

    /**
     * Callback of image loading
     */
    public interface Callback {
        void onImageLoaded(@Nullable Bitmap image, boolean fromMemoryCache,
                boolean fromStorageCache);

        void onImageDisplayed(@NonNull ImageView imageView);
    }
}
