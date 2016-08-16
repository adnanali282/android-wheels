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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * {@link RequestResult#STRING} - {@link String} via {@link RequestResult#getDataString()}
     * <br>
     * {@link RequestResult#STREAM} - {@link InputStream} via {@link RequestResult#getDataStream()}
     */
    @IntDef({NONE, STRING, STREAM})
    public @interface DataType {
    }

    private static final int BUFFER_SIZE = 8192;

    private int mResultType = NONE;
    private int mDataType = NONE;
    private int mHttpCode = NONE;
    private String mDataString;
    private InputStream mDataStream;
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
     * Result data of the request as {@link String}
     * <br>
     * Available if {@link RequestResult#getDataType()} is {@link RequestResult#STRING}
     * or {@link RequestResult#STREAM}.
     * <br>
     * If {@link RequestResult#getDataType()} is {@link RequestResult#STREAM}, it can be
     * changed to {@link RequestResult#STRING} or {@link RequestResult#NONE} after call
     * of this method. The response stream will be read and closed.
     *
     * @return Result string
     */
    @Nullable
    public String getDataString() {
        if (mDataType == STREAM) {
            InputStream stream = mDataStream;
            if (stream == null) {
                return null;
            }
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(stream))) {
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
                mDataStream = null;
                return null;
            }
        } else if (mDataType == STRING) {
            return mDataString;
        } else {
            return null;
        }
    }

    /**
     * Result data of the request as {@link InputStream}
     * <br>
     * Available if {@link RequestResult#getDataType()} is {@link RequestResult#STREAM} and
     * {@link RequestResult#getResultType()} is {@link RequestResult#SUCCESS}
     *
     * @return Result data stream
     */
    @Nullable
    public InputStream getDataStream() {
        return mDataStream;
    }

    /**
     * Returns an input stream from the server in the case of an error such as
     * the requested file has not been found on the remote server. This stream
     * can be used to read the data the server will send back.
     *
     * @return The error input stream returned by the server.
     */
    @Nullable
    public InputStream getErrorStream() {
        HttpURLConnection connection = mConnection;
        if (connection == null) {
            return null;
        } else {
            return connection.getErrorStream();
        }
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

    void setHttpCode(int httpCode) {
        mHttpCode = httpCode;
    }

    void setDataStream(@Nullable InputStream dataStream) {
        mDataStream = dataStream;
        if (dataStream == null) {
            mDataType = NONE;
        } else {
            mDataType = STREAM;
        }
    }

    void setException(@Nullable Exception exception) {
        mException = exception;
    }

    void setConnection(@Nullable HttpURLConnection connection) {
        mConnection = connection;
    }
}
