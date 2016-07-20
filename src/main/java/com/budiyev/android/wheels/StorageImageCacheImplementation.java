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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of {@link StorageImageCache} for {@link ImageLoader}
 */
class StorageImageCacheImplementation implements StorageImageCache {
    private final AtomicBoolean mCacheSizeFitting = new AtomicBoolean();
    private final AtomicBoolean mCacheSizeFitRequested = new AtomicBoolean();
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

    @Nullable
    private File[] getCacheFiles() {
        return mDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteCacheFile(@NonNull File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void doFitCacheSize() {
        try {
            File[] files = getCacheFiles();
            if (files == null || files.length < 2) {
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
        } catch (Exception ignored) {
        }
        if (mCacheSizeFitRequested.compareAndSet(true, false)) {
            doFitCacheSize();
        } else {
            mCacheSizeFitting.set(false);
        }
    }

    public void fitCacheSize() {
        if (mCacheSizeFitting.compareAndSet(false, true)) {
            ExecutorUtils.getImageLoaderExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    doFitCacheSize();
                }
            });
        } else {
            mCacheSizeFitRequested.set(true);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void put(@NonNull String key, @NonNull Bitmap value) {
        if (!mDirectory.exists()) {
            mDirectory.mkdirs();
        }
        File file = new File(mDirectory, key);
        deleteCacheFile(file);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            value.compress(mCompressFormat, mCompressQuality, outputStream);
        } catch (IOException e) {
            deleteCacheFile(file);
        }
        fitCacheSize();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    @Override
    public Bitmap get(@NonNull String key) {
        File file = new File(mDirectory, key);
        if (!file.exists() && !file.isFile()) {
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
        deleteCacheFile(new File(mDirectory, key));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void clear() {
        File[] files = getCacheFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }
}
