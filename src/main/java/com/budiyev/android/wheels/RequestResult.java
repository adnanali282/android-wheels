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
     * <br>
     * {@link RequestResult#NONE} - No result
     * <br>
     * {@link RequestResult#SUCCESS} - Success (response code is {@link HttpURLConnection#HTTP_OK})
     * <br>
     * {@link RequestResult#ERROR_HTTP} - Response code isn't {@link HttpURLConnection#HTTP_OK},
     * see {@link RequestResult#getHttpCode()} for details
     * <br>
     * {@link RequestResult#ERROR_MALFORMED_URL} - Malformed URL,
     * see {@link RequestResult#getException()} for details
     * <br>
     * {@link RequestResult#ERROR_UNSUPPORTED_ENCODING} - Unsupported text encoding,
     * see {@link RequestResult#getException()} for details
     * <br>
     * {@link RequestResult#ERROR_PROTOCOL} - Protocol error,
     * see {@link RequestResult#getException()} for details
     * <br>
     * {@link RequestResult#ERROR_IO} - IO error,
     * see {@link RequestResult#getException()} for details
     * <br>
     * {@link RequestResult#ERROR_UNEXPECTED} - Unexpected error,
     * see {@link RequestResult#getException()} for details
     */
    @IntDef({NONE, SUCCESS, ERROR_HTTP, ERROR_MALFORMED_URL, ERROR_UNSUPPORTED_ENCODING,
            ERROR_PROTOCOL, ERROR_IO, ERROR_UNEXPECTED})
    public @interface ResultType {
    }

    public static final int STRING = 1;
    public static final int STREAM = 2;

    /**
     * Result data type
     * <br>
     * {@link RequestResult#NONE} - Don't receive any data / no data received
     * <br>
     * {@link RequestResult#STRING} - {@link String} via {@link RequestResult#getString()}
     * <br>
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
    private Exception mException;
    private HttpURLConnection mConnection;

    RequestResult() {
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
        return mDataType;
    }

    /**
     * HTTP response code, can be {@link RequestResult#NONE}
     */
    public int getHttpCode() {
        return mHttpCode;
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

    /**
     * Result of the request ({@link RequestResult#STREAM})
     *
     * @return Result data stream
     */
    @Nullable
    public InputStream getStream() {
        return mStream;
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

    void setDataType(@DataType int dataType) {
        mDataType = dataType;
    }

    void setHttpCode(int httpCode) {
        mHttpCode = httpCode;
    }

    void setString(@Nullable String dataString) {
        mString = dataString;
    }

    void setStream(@Nullable InputStream dataStream) {
        mStream = dataStream;
    }

    void setException(@Nullable Exception exception) {
        mException = exception;
    }

    void setConnection(@Nullable HttpURLConnection connection) {
        mConnection = connection;
    }
}
