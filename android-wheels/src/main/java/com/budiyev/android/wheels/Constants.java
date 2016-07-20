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

final class Constants {
    private Constants() {
    }

    public static final class Threads {
        public static final String BACKGROUND_THREAD_NAME = "ImageLoader-background-thread";

        private Threads() {
        }
    }

    public static final class MessageDigest {
        public static final String MD5 = "MD5";

        private MessageDigest() {
        }
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
        public static final String FRACTION_RANGE_ERROR_MESSAGE =
                "Argument \"fraction\" must be between 0.1 and 0.8 (inclusive)";

        private MemoryImageCache() {
        }
    }

    public static final class StorageImageCache {
        public static final double DEFAULT_FRACTION = 0.1D;
        public static final int DEFAULT_QUALITY = 80;
        public static final Bitmap.CompressFormat DEFAULT_FORMAT = Bitmap.CompressFormat.JPEG;
        public static final String DEFAULT_DIRECTORY = "image_loader_cache";
        public static final String FRACTION_RANGE_ERROR_MESSAGE =
                "Argument \"fraction\" must be between 0.01 and 1.0 (inclusive)";

        private StorageImageCache() {
        }
    }
}
