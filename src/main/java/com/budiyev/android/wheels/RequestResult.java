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
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Request result of {@link HttpRequest}
 */
public final class RequestResult {
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
     */
    @IntDef({NONE, SUCCESS, ERROR_HTTP, ERROR_MALFORMED_URL, ERROR_UNSUPPORTED_ENCODING,
            ERROR_PROTOCOL, ERROR_IO, ERROR_UNEXPECTED})
    public @interface ResultType {
    }

    public static final int STRING = 1;
    public static final int STREAM = 2;

    /**
     * Result data type
     * {@link RequestResult#NONE} - don't receive any data / no data received
     * {@link RequestResult#STRING} - {@link String} via {@link RequestResult#getString()}
     * {@link RequestResult#STREAM} - {@link InputStream} via {@link RequestResult#getStream()}
     */
    @IntDef({NONE, STRING, STREAM})
    public @interface DataType {
    }

    private int mResultType = NONE;
    private int mDataType = NONE;
    private int mHttpCode = NONE;
    private String mString;
    private InputStream mStream;
    private HttpURLConnection mConnection;
    private Exception mException;

    RequestResult() {
    }

    /**
     * Result type
     */
    @ResultType
    public int getResultType() {
        return mResultType;
    }

    void setResultType(@ResultType int resultType) {
        mResultType = resultType;
    }

    /**
     * Result data type
     */
    @DataType
    public int getDataType() {
        return mDataType;
    }

    void setDataType(@DataType int dataType) {
        mDataType = dataType;
    }

    /**
     * HTTP response code, can be {@link RequestResult#NONE}
     */
    public int getHttpCode() {
        return mHttpCode;
    }

    void setHttpCode(int httpCode) {
        mHttpCode = httpCode;
    }

    /**
     * Result of the request ({@link RequestResult#STRING})
     *
     * @return Result string
     */
    @Nullable
    public String getString() {
        return mString;
    }

    void setString(@Nullable String dataString) {
        mString = dataString;
    }

    /**
     * Result of the request ({@link RequestResult#STREAM})
     *
     * @return Result data stream
     */
    @Nullable
    public InputStream getStream() {
        return mStream;
    }

    void setStream(@Nullable InputStream dataStream) {
        mStream = dataStream;
    }

    /**
     * {@link HttpURLConnection} instance of this request
     *
     * @return HttpUrlConnection or null
     */
    @Nullable
    public HttpURLConnection getConnection() {
        return mConnection;
    }

    void setConnection(@Nullable HttpURLConnection connection) {
        mConnection = connection;
    }

    /**
     * Exception, if {@link RequestResult#getResultType()} is one of
     * {@link RequestResult#ERROR_MALFORMED_URL}, {@link RequestResult#ERROR_UNSUPPORTED_ENCODING},
     * {@link RequestResult#ERROR_PROTOCOL}, {@link RequestResult#ERROR_IO},
     * {@link RequestResult#ERROR_UNEXPECTED}
     */
    @Nullable
    public Exception getException() {
        return mException;
    }

    void setException(@Nullable Exception exception) {
        mException = exception;
    }
}
