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

import android.support.annotation.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Result of HTTP request
 */
public enum RequestResult {
    NONE,
    SUCCESS,
    ERROR_HTTP,
    ERROR_MALFORMED_URL,
    ERROR_UNSUPPORTED_ENCODING,
    ERROR_PROTOCOL,
    ERROR_IO,
    ERROR_UNEXPECTED;

    private int mHttpCode = -1;
    private Exception mException;
    private String mDataString;
    private InputStream mDataStream;
    private HttpURLConnection mConnection;

    @Nullable
    public HttpURLConnection getConnection() {
        return mConnection;
    }

    void setConnection(@Nullable HttpURLConnection connection) {
        mConnection = connection;
    }

    public int getHttpCode() {
        return mHttpCode;
    }

    void setHttpCode(int httpCode) {
        mHttpCode = httpCode;
    }

    @Nullable
    public Exception getException() {
        return mException;
    }

    void setException(@Nullable Exception exception) {
        mException = exception;
    }

    /**
     * Request result (ResultType.STRING)
     *
     * @return Request result string
     */
    @Nullable
    public String getDataString() {
        return mDataString;
    }

    void setDataString(@Nullable String dataString) {
        mDataString = dataString;
    }

    /**
     * Request result (ResultType.STREAM)
     *
     * @return Request result data stream
     */
    @Nullable
    public InputStream getDataStream() {
        return mDataStream;
    }

    void setDataStream(@Nullable InputStream dataStream) {
        mDataStream = dataStream;
    }
}
