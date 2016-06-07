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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ImageLoader is a universal tool for loading bitmaps efficiently in Android which
 * provides automatic memory and storage caching. ImageLoader is usable without caches,
 * with one of them, or both (without caches, caching is not available). Also, ImageLoader
 * is usable without bitmapLoader (loading new bitmaps is not available in this case).
 *
 * @param <T> Source data type
 */
public class ImageLoader<T> {
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(1);
    private static final String URI_SCHEME_HTTP = "http";
    private static final String URI_SCHEME_HTTPS = "https";
    private static final String URI_SCHEME_FTP = "ftp";
    private final Object mPauseWorkLock = new Object();
    private final Context mContext;
    private final Thread mMainThread;
    private final Handler mMainThreadHandler;
    private final ExecutorService mAsyncExecutor;
    private volatile boolean mExitTasksEarly = false;
    private volatile boolean mImageFadeIn = true;
    private volatile boolean mPauseWork = false;
    private volatile int mImageFadeInTime = 200;
    private volatile BitmapLoader<T> mBitmapLoader;
    private volatile MemoryImageCache mMemoryImageCache;
    private volatile StorageImageCache mStorageImageCache;
    private volatile Bitmap mPlaceholderBitmap = null;

    /**
     * ImageLoader without any cache or bitmap loader
     *
     * @param context Context
     */
    public ImageLoader(@NonNull Context context) {
        this(context, null, null, null);
    }

    /**
     * ImageLoader with default memory cache and without storage cache. ImageLoader
     * is usable without bitmapLoader (loading new bitmaps is not available in this case).
     *
     * @param context      Context
     * @param bitmapLoader Bitmap loader
     */
    public ImageLoader(@NonNull Context context, @Nullable BitmapLoader<T> bitmapLoader) {
        this(context, bitmapLoader, new MemoryImageCache(), null);
    }

    /**
     * ImageLoader with default memory and storage caches. ImageLoader is usable without
     * bitmapLoader (loading new bitmaps is not available in this case).
     *
     * @param context               Context
     * @param bitmapLoader          Bitmap loader
     * @param storageCacheDirectory Storage cache directory
     */
    public ImageLoader(@NonNull Context context, @Nullable BitmapLoader<T> bitmapLoader,
            @NonNull File storageCacheDirectory) {
        this(context, bitmapLoader, new MemoryImageCache(),
                new StorageImageCache(storageCacheDirectory));
    }

    /**
     * ImageLoader with defined bitmap loader, memory image cache and storage image cache
     *
     * @param context           Context
     * @param bitmapLoader      Bitmap loader
     * @param memoryImageCache  Memory image cache
     * @param storageImageCache Storage image cache
     */
    public ImageLoader(@NonNull Context context, @Nullable BitmapLoader<T> bitmapLoader,
            @Nullable MemoryImageCache memoryImageCache,
            @Nullable StorageImageCache storageImageCache) {
        mContext = context;
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
        INSTANCE_COUNTER.compareAndSet(Integer.MAX_VALUE, 1);
        mAsyncExecutor = Executors.newFixedThreadPool(poolSize,
                new ImageLoaderThreadFactory(INSTANCE_COUNTER.getAndIncrement()));
    }

    protected void cancelWork(@Nullable ImageView imageView) {
        LoadImageAction<?> loadImageAction = getLoadImageAction(imageView);
        if (loadImageAction != null) {
            loadImageAction.cancel();
        }
    }

    protected boolean cancelPotentialWork(@NonNull ImageSource<?> imageSource,
            @Nullable ImageView imageView) {
        LoadImageAction<?> loadImageAction = getLoadImageAction(imageView);
        if (loadImageAction != null) {
            ImageSource<?> actionImageSource = loadImageAction.mImageSource;
            if (actionImageSource == null || !actionImageSource.equals(imageSource)) {
                loadImageAction.cancel();
            } else {
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected LoadImageAction<?> getLoadImageAction(@Nullable ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncBitmapDrawable) {
                AsyncBitmapDrawable asyncBitmapDrawable = (AsyncBitmapDrawable) drawable;
                return asyncBitmapDrawable.getLoadImageAction();
            }
        }
        return null;
    }

    protected void runOnMainThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() == mMainThread) {
            runnable.run();
        } else {
            runOnMainThread(runnable, 0);
        }
    }

    protected void runOnMainThread(@NonNull Runnable runnable, int delay) {
        mMainThreadHandler.postDelayed(runnable, delay);
    }

    protected ExecutorService getAsyncExecutor() {
        return mAsyncExecutor;
    }

    protected Context getContext() {
        return mContext;
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

    public Bitmap getPlaceholderImage() {
        return mPlaceholderBitmap;
    }

    public void setPlaceholderImage(@Nullable Bitmap bitmap) {
        mPlaceholderBitmap = bitmap;
    }

    public void setPlaceholderImage(int resId) {
        mPlaceholderBitmap = BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    public BitmapLoader<T> getBitmapLoader() {
        return mBitmapLoader;
    }

    public void setBitmapLoader(@Nullable BitmapLoader<T> bitmapLoader) {
        mBitmapLoader = bitmapLoader;
    }

    public MemoryImageCache getMemoryImageCache() {
        return mMemoryImageCache;
    }

    public void setMemoryImageCache(@Nullable MemoryImageCache memoryImageCache) {
        mMemoryImageCache = memoryImageCache;
    }

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

    public boolean isPauseWork() {
        return mPauseWork;
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!isPauseWork()) {
                mPauseWorkLock.notifyAll();
            }
        }
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

    /**
     * Calculates sample size for required size from source size
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
            for (; (halfWidth / sampleSize) > requiredWidth &&
                    (halfHeight / sampleSize) > requiredHeight; ) {
                sampleSize *= 2;
            }
            if (ignoreTotalNumberOfPixels) {
                return sampleSize;
            }
            long totalPixels = (sourceWidth * sourceHeight) / (sampleSize * sampleSize);
            long totalRequiredPixels = requiredWidth * requiredHeight;
            for (; totalPixels > totalRequiredPixels; ) {
                sampleSize *= 2;
                totalPixels /= 4L;
            }
        }
        return sampleSize;
    }

    /**
     * Helper method for getting input stream from uris
     *
     * @param context Context
     * @param uri     Uri
     * @return stream
     * @throws IOException
     */
    public static InputStream getStreamFromUri(Context context, Uri uri) throws IOException {
        String scheme = uri.getScheme();
        if (Objects.equals(scheme, URI_SCHEME_HTTP) || Objects.equals(scheme, URI_SCHEME_HTTPS) ||
                Objects.equals(scheme, URI_SCHEME_FTP)) {
            return new URL(uri.toString()).openConnection().getInputStream();
        } else {
            return context.getContentResolver().openInputStream(uri);
        }
    }

    /**
     * Helper method for loading sampled bitmaps from URIs
     *
     * @param context                   Context
     * @param uri                       URI
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromUri(Context context, Uri uri, int requiredWidth,
            int requiredHeight, boolean ignoreTotalNumberOfPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = getStreamFromUri(context, uri)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, requiredWidth,
                        requiredHeight, ignoreTotalNumberOfPixels);
        try (InputStream inputStream = getStreamFromUri(context, uri)) {
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Helper method for loading sampled bitmaps from files
     *
     * @param file                      File
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromFile(File file, int requiredWidth, int requiredHeight,
            boolean ignoreTotalNumberOfPixels) {
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
     * Helper method for loading sampled bitmaps from file descriptors
     *
     * @param fileDescriptor            File descriptor
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromFileDescriptor(FileDescriptor fileDescriptor,
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
     * Helper method for loading sampled bitmaps from resources
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
    public static Bitmap loadSampledBitmapFromResource(Resources resources, int resourceId,
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
     * Helper method for loading sampled bitmaps from byte arrays
     *
     * @param byteArray                 Byte array
     * @param requiredWidth             Required width
     * @param requiredHeight            Required height
     * @param ignoreTotalNumberOfPixels Ignore total number of pixels
     *                                  (requiredWidth * requiredHeight)
     * @return Loaded bitmap or null
     */
    @Nullable
    public static Bitmap loadSampledBitmapFromByteArray(byte[] byteArray, int requiredWidth,
            int requiredHeight, boolean ignoreTotalNumberOfPixels) {
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
     * Creates new common bitmap loader for uris
     *
     * @param context Context
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<Uri> newUriBitmapLoader(final Context context) {
        return new BitmapLoader<Uri>() {
            @Override
            public Bitmap load(Uri data) {
                return loadSampledBitmapFromUri(context, data, Integer.MAX_VALUE, Integer.MAX_VALUE,
                        true);
            }
        };
    }

    /**
     * Creates new common bitmap loader for files
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<File> newFileBitmapLoader() {
        return new BitmapLoader<File>() {
            @Override
            public Bitmap load(File data) {
                return loadSampledBitmapFromFile(data, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
            }
        };
    }

    /**
     * Creates new common bitmap loader for file descriptors
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<FileDescriptor> newFileDescriptorBitmapLoader() {
        return new BitmapLoader<FileDescriptor>() {
            @Override
            public Bitmap load(FileDescriptor data) {
                return loadSampledBitmapFromFileDescriptor(data, Integer.MAX_VALUE,
                        Integer.MAX_VALUE, true);
            }
        };
    }

    /**
     * Creates new common bitmap loader for resources
     *
     * @param context Context
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<Integer> newResourceBitmapLoader(final Context context) {
        return new BitmapLoader<Integer>() {
            @Override
            public Bitmap load(Integer data) {
                return loadSampledBitmapFromResource(context.getResources(), data,
                        Integer.MAX_VALUE, Integer.MAX_VALUE, true);
            }
        };
    }

    /**
     * Creates new common bitmap loader for byte arrays
     *
     * @return bitmap loader
     */
    @NonNull
    public static BitmapLoader<byte[]> newByteArrayBitmapLoader() {
        return new BitmapLoader<byte[]>() {
            @Override
            public Bitmap load(byte[] data) {
                return loadSampledBitmapFromByteArray(data, Integer.MAX_VALUE, Integer.MAX_VALUE,
                        true);
            }
        };
    }

    /**
     * Generates MD5 hash string for specified data
     *
     * @param data Data
     * @return MD5 hash string
     */
    @NonNull
    public static String generateMD5(byte[] data) {
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
     * Creates new common image source that is usable in most cases
     *
     * @param data Source data
     * @param <T>  Source data type
     * @return image source
     */
    @NonNull
    public static <T> ImageSource<T> newImageSource(final T data) {
        final String key = generateMD5(String.valueOf(data).getBytes());
        return new ImageSource<T>() {
            @Override
            public T getData() {
                return data;
            }

            @Override
            public String getKey() {
                return key;
            }
        };
    }

    /**
     * Load image from uri
     *
     * @param imageView Image view
     * @param uri       Source uri
     */
    public static void load(final ImageView imageView, final Uri uri) {
        Context context = imageView.getContext();
        ImageLoader<Uri> loader = new ImageLoader<>(context);
        loader.setBitmapLoader(newUriBitmapLoader(context));
        loader.setImageFadeIn(false);
        loader.loadImage(new ImageSource<Uri>() {
            @Override
            public Uri getData() {
                return uri;
            }

            @Override
            public String getKey() {
                return null;
            }
        }, imageView);
    }

    /**
     * Load image from file
     *
     * @param imageView Image view
     * @param file      Source file
     */
    public static void load(final ImageView imageView, final File file) {
        final Context context = imageView.getContext();
        ImageLoader<File> loader = new ImageLoader<>(context);
        loader.setBitmapLoader(newFileBitmapLoader());
        loader.setImageFadeIn(false);
        loader.loadImage(new ImageSource<File>() {
            @Override
            public File getData() {
                return file;
            }

            @Override
            public String getKey() {
                return null;
            }
        }, imageView);
    }

    /**
     * Load image file descriptor
     *
     * @param imageView      Image view
     * @param fileDescriptor Source file descriptor
     */
    public static void load(final ImageView imageView, final FileDescriptor fileDescriptor) {
        final Context context = imageView.getContext();
        ImageLoader<FileDescriptor> loader = new ImageLoader<>(context);
        loader.setBitmapLoader(newFileDescriptorBitmapLoader());
        loader.setImageFadeIn(false);
        loader.loadImage(new ImageSource<FileDescriptor>() {
            @Override
            public FileDescriptor getData() {
                return fileDescriptor;
            }

            @Override
            public String getKey() {
                return null;
            }
        }, imageView);
    }

    /**
     * Load image from resource
     *
     * @param imageView  Image view
     * @param resourceId Source resource identifier
     */
    public static void load(final ImageView imageView, final int resourceId) {
        final Context context = imageView.getContext();
        ImageLoader<Integer> loader = new ImageLoader<>(context);
        loader.setBitmapLoader(newResourceBitmapLoader(context));
        loader.setImageFadeIn(false);
        loader.loadImage(new ImageSource<Integer>() {
            @Override
            public Integer getData() {
                return resourceId;
            }

            @Override
            public String getKey() {
                return null;
            }
        }, imageView);
    }

    /**
     * Load image from byte array
     *
     * @param imageView Image view
     * @param bytes     Source bytes
     */
    public static void load(final ImageView imageView, final byte[] bytes) {
        final Context context = imageView.getContext();
        ImageLoader<byte[]> loader = new ImageLoader<>(context);
        loader.setBitmapLoader(newByteArrayBitmapLoader());
        loader.setImageFadeIn(false);
        loader.loadImage(new ImageSource<byte[]>() {
            @Override
            public byte[] getData() {
                return bytes;
            }

            @Override
            public String getKey() {
                return null;
            }
        }, imageView);
    }

    private static class ImageLoaderThreadFactory implements ThreadFactory {
        private final AtomicInteger mThreadCounter = new AtomicInteger(1);
        private final int mIndex;

        public ImageLoaderThreadFactory(int index) {
            mIndex = index;
        }

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable,
                    "ImageLoader-" + mIndex + "-thread-" + mThreadCounter.getAndIncrement());
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }

    protected static class LoadImageAction<T> {
        private final ImageSource<T> mImageSource;
        private final WeakReference<ImageView> mImageViewReference;
        private final ImageLoader<T> mImageLoader;
        private final Callback mCallback;
        private final AtomicBoolean mSubmitted = new AtomicBoolean(false);
        private volatile boolean mFinished = false;
        private volatile boolean mCancelled = false;
        private volatile Future<Void> mFuture = null;

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
                for (; mImageLoader.isPauseWork() && !isCancelled(); ) {
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
                        image = bitmapLoader.load(mImageSource.getData());
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
            LoadImageAction<?> loadImageAction = mImageLoader.getLoadImageAction(imageView);
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

    protected static class SetImageAction implements Runnable {
        private final BitmapDrawable mBitmapDrawable;
        private final ImageLoader<?> mImageLoader;
        private final LoadImageAction<?> mLoadImageAction;
        private final Callback mCallback;

        public SetImageAction(BitmapDrawable bitmapDrawable, ImageLoader<?> imageLoader,
                Callback callback, LoadImageAction<?> loadImageAction) {
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

    protected static class SimpleSetImageAction implements Runnable {
        private final ImageView mImageView;
        private final BitmapDrawable mBitmapDrawable;
        private final Callback mCallback;

        public SimpleSetImageAction(ImageView imageView, BitmapDrawable bitmapDrawable,
                Callback callback) {
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

    protected static class AsyncBitmapDrawable extends BitmapDrawable {
        private WeakReference<LoadImageAction<?>> mLoadImageActionReference;

        public AsyncBitmapDrawable(Resources res, Bitmap bitmap,
                LoadImageAction<?> loadImageAction) {
            super(res, bitmap);
            mLoadImageActionReference = new WeakReference<LoadImageAction<?>>(loadImageAction);
        }

        public LoadImageAction<?> getLoadImageAction() {
            return mLoadImageActionReference.get();
        }
    }

    public static class MemoryImageCache {
        private static final float DEFAULT_MEMORY_PERCENT = 0.25F;
        private final LruCache<String, RecyclingBitmapDrawable> mCache;

        public MemoryImageCache() {
            this(getMemoryPercentBytes(DEFAULT_MEMORY_PERCENT));
        }

        public MemoryImageCache(int size) {
            mCache = new LruCache<String, RecyclingBitmapDrawable>(size) {
                @Override
                protected void entryRemoved(boolean evicted, String key,
                        RecyclingBitmapDrawable oldValue, RecyclingBitmapDrawable newValue) {
                    oldValue.setCached(false);
                }

                @Override
                protected int sizeOf(String key, RecyclingBitmapDrawable value) {
                    return value.getBitmap().getAllocationByteCount();
                }
            };
        }

        public void put(@NonNull String key, @NonNull RecyclingBitmapDrawable value) {
            value.setCached(true);
            mCache.put(key, value);
        }

        @Nullable
        public RecyclingBitmapDrawable get(@NonNull String key) {
            return mCache.get(key);
        }

        public void remove(@NonNull String key) {
            mCache.remove(key);
        }

        public void clear() {
            mCache.evictAll();
        }

        public static int getMemoryPercentBytes(float percent) {
            if (percent < 0.1F || percent > 0.8F) {
                throw new IllegalArgumentException(
                        "Argument \"percent\" must be between 0.1 and 0.8 (inclusive)");
            }
            return Math.round(percent * Runtime.getRuntime().maxMemory());
        }
    }

    public static class StorageImageCache {
        private static final double DEFAULT_STORAGE_PERCENT = 0.25D;
        private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT =
                Bitmap.CompressFormat.JPEG;
        private static final int DEFAULT_COMPRESS_QUALITY = 90;
        private final Object mCacheSizeLock = new Object();
        private final File mDirectory;
        private final long mMaxSize;
        private final Bitmap.CompressFormat mCompressFormat;
        private final int mCompressQuality;

        public StorageImageCache(@NonNull File directory) {
            this(directory, getStorageFreePercentBytes(directory, DEFAULT_STORAGE_PERCENT),
                    DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY);
        }

        public StorageImageCache(@NonNull File directory,
                @NonNull Bitmap.CompressFormat compressFormat, int compressQuality) {
            this(directory, getStorageFreePercentBytes(directory, DEFAULT_STORAGE_PERCENT),
                    compressFormat, compressQuality);
        }

        public StorageImageCache(@NonNull File directory, long maxSize,
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
        public void put(@NonNull String key, @NonNull Bitmap value) {
            if (!mDirectory.exists()) {
                mDirectory.mkdirs();
            }
            try (OutputStream outputStream = new FileOutputStream(new File(mDirectory, key))) {
                value.compress(mCompressFormat, mCompressQuality, outputStream);
            } catch (IOException ignored) {
            }
            fitCacheSize();
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Nullable
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
        public void remove(@NonNull String key) {
            File file = new File(mDirectory, key);
            if (file.exists()) {
                file.delete();
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public synchronized void clear() {
            File[] files = mDirectory.listFiles();
            for (File file : files) {
                file.delete();
            }
        }

        public static long getStorageFreePercentBytes(@NonNull File path, double percent) {
            if (percent < 0.01D || percent > 1.0D) {
                throw new IllegalArgumentException(
                        "Argument \"percent\" must be between 0.01 and 1.0 (inclusive)");
            }
            StatFs stat = new StatFs(path.getAbsolutePath());
            double bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
            return Math.round(bytesAvailable * percent);
        }

        public static long getStorageTotalPercentBytes(@NonNull File path, double percent) {
            if (percent < 0.01D || percent > 1.0D) {
                throw new IllegalArgumentException(
                        "Argument \"percent\" must be between 0.01 and 1.0 (inclusive)");
            }
            StatFs stat = new StatFs(path.getAbsolutePath());
            return Math.round(stat.getTotalBytes() * percent);
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
     * Represents source data and key to cache loaded bitmaps
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
        String getKey();
    }

    /**
     * Represents bitmap loader from concrete source data type
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
        Bitmap load(T data);
    }

    /**
     * Callback for concrete image loading
     */
    public interface Callback {
        void onImageLoaded(Bitmap image, boolean fromMemoryCache, boolean fromStorageCache);

        void onImageDisplayed(ImageView imageView);
    }
}
