/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Request result of {@link HttpRequest}
 */
public final class HttpRequestResult {
    public static final int NONE = -1;
    public static final int SUCCESS = 1;
    public static final int ERROR_HTTP = 2;
    public static final int ERROR_MALFORMED_URL = 3;
    public static final int ERROR_UNSUPPORTED_ENCODING = 4;
    public static final int ERROR_PROTOCOL = 5;
    public static final int ERROR_IO = 6;
    public static final int ERROR_UNEXPECTED = 7;

    /**
     * Type of this result
     * <br>
     * {@link #NONE} - No result
     * <br>
     * {@link #SUCCESS} - Success, see {@link #getHttpCode()} for details
     * <br>
     * {@link #ERROR_HTTP} - HTTP error, see {@link #getHttpCode()}
     * and {@link #getException()} for details
     * <br>
     * {@link #ERROR_MALFORMED_URL} - Malformed URL,
     * see {@link #getException()} for details
     * <br>
     * {@link #ERROR_UNSUPPORTED_ENCODING} - Unsupported text encoding,
     * see {@link #getException()} for details
     * <br>
     * {@link #ERROR_PROTOCOL} - Protocol error,
     * see {@link #getException()} for details
     * <br>
     * {@link #ERROR_IO} - IO error,
     * see {@link #getException()} for details
     * <br>
     * {@link #ERROR_UNEXPECTED} - Unexpected error,
     * see {@link #getException()} for details
     */
    @IntDef({NONE, SUCCESS, ERROR_HTTP, ERROR_MALFORMED_URL, ERROR_UNSUPPORTED_ENCODING,
            ERROR_PROTOCOL, ERROR_IO, ERROR_UNEXPECTED})
    public @interface ResultType {
    }

    public static final int STREAM = 1;
    public static final int STRING = 2;

    /**
     * Result data type
     * <br>
     * {@link #NONE} - No data received
     * <br>
     * {@link #STREAM} - {@link InputStream}
     * via {@link #getDataStream()}
     * <br>
     * {@link #STRING} - {@link String}
     * via {@link #getDataString()}
     */
    @IntDef({NONE, STRING, STREAM})
    public @interface DataType {
    }

    private static final int BUFFER_SIZE = 8192;

    private final Lock mDataLock = new ReentrantLock();
    private volatile String mDataString;
    private volatile int mDataType = NONE;
    private InputStream mDataStream;
    private Map<String, List<String>> mHeaderFields;
    private Exception mException;
    private HttpURLConnection mConnection;
    private int mResultType = NONE;
    private int mHttpCode = NONE;

    HttpRequestResult() {
    }

    /**
     * Result type
     */
    @ResultType
    public int getResultType() {
        return mResultType;
    }

    /**
     * Result data type
     */
    @DataType
    public int getDataType() {
        mDataLock.lock();
        try {
            return mDataType;
        } finally {
            mDataLock.unlock();
        }
    }

    /**
     * HTTP response code, can be {@link #NONE}
     */
    public int getHttpCode() {
        return mHttpCode;
    }

    /**
     * Result data of the request as {@link String}
     * <br>
     * Available if {@link #getDataType()} is {@link #STRING}
     * or {@link #STREAM}.
     * <br>
     * If {@link #getDataType()} is {@link #STREAM}, it can be
     * changed to {@link #STRING} or {@link #NONE} after call
     * of this method. Response stream will be read and closed.
     *
     * @return Result string
     */
    @Nullable
    public String getDataString() {
        return getDataString(HttpRequest.CHARSET_UTF_8);
    }

    /**
     * Result data of the request as {@link String}
     * <br>
     * Available if {@link #getDataType()} is {@link #STRING} or {@link #STREAM}.
     * <br>
     * If {@link #getDataType()} is {@link #STREAM}, it can be
     * changed to {@link #STRING} or {@link #NONE} after call
     * of this method. The response stream will be read and closed.
     *
     * @param charset Response stream charset name
     * @return Result string
     */
    @Nullable
    public String getDataString(@NonNull String charset) {
        mDataLock.lock();
        try {
            if (mDataType == STREAM) {
                InputStream stream = mDataStream;
                if (stream == null) {
                    return null;
                }
                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(stream, charset))) {
                    StringBuilder responseBuilder = new StringBuilder();
                    char[] buffer = new char[BUFFER_SIZE];
                    for (; ; ) {
                        int read = bufferedReader.read(buffer);
                        if (read > -1) {
                            responseBuilder.append(buffer, 0, read);
                        } else {
                            break;
                        }
                    }
                    String response = responseBuilder.toString();
                    mDataString = response;
                    mDataType = STRING;
                    return response;
                } catch (IOException e) {
                    mDataType = NONE;
                    mDataString = null;
                    return null;
                }
            } else if (mDataType == STRING) {
                return mDataString;
            } else {
                return null;
            }
        } finally {
            mDataLock.unlock();
        }
    }

    /**
     * Result data of the request as {@link InputStream}
     * <br>
     * Available if {@link #getDataType()} is {@link #STREAM}
     *
     * @return Result data stream
     */
    @Nullable
    public InputStream getDataStream() {
        mDataLock.lock();
        try {
            return mDataStream;
        } finally {
            mDataLock.unlock();
        }
    }

    /**
     * Unmodifiable map of response header fields and values
     * <br>
     * Available if {@link #getResultType()} is {@link #SUCCESS} of {@link #ERROR_HTTP}
     */
    @Nullable
    public Map<String, List<String>> getHeaderFields() {
        return mHeaderFields;
    }

    /**
     * Exception, if {@link #getResultType()} is one of
     * {@link #ERROR_MALFORMED_URL}, {@link #ERROR_UNSUPPORTED_ENCODING},
     * {@link #ERROR_PROTOCOL}, {@link #ERROR_IO},
     * {@link #ERROR_UNEXPECTED}
     */
    @Nullable
    public Exception getException() {
        return mException;
    }

    /**
     * Releases current connection so that its resources may be either reused or closed
     */
    public void disconnect() {
        HttpURLConnection connection = mConnection;
        if (connection != null) {
            connection.disconnect();
        }
    }

    void setResultType(@ResultType int resultType) {
        mResultType = resultType;
    }

    void setHttpCode(int httpCode) {
        mHttpCode = httpCode;
    }

    void setDataStream(@Nullable InputStream dataStream) {
        mDataLock.lock();
        try {
            mDataStream = dataStream;
            if (dataStream == null) {
                mDataType = NONE;
            } else {
                mDataType = STREAM;
            }
        } finally {
            mDataLock.unlock();
        }
    }

    void setHeaderFields(@Nullable Map<String, List<String>> headerFields) {
        mHeaderFields = headerFields;
    }

    void setException(@Nullable Exception exception) {
        mException = exception;
    }

    void setConnection(@Nullable HttpURLConnection connection) {
        mConnection = connection;
    }
}
