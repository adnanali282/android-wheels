/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Yuriy Budiyev
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
package com.budiyev.wheels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author Yuriy Budiyev (yuriy.budiyev@yandex.ru)
 * @link https://github.com/yuriy-budiyev/android-wheels
 */
public final class HttpRequest {
    /**
     * Create new GET HTTP request
     *
     * @param url                URL-address
     * @param queryParameterList Query string parameter list
     * @param resultType         Type of request result in callback
     * @param callback           Response callback
     * @return New GET request instance
     */
    @NonNull
    public static Get newGetRequest(@NonNull String url,
            @Nullable List<QueryParameter> queryParameterList, @NonNull ResultType resultType,
            @Nullable Callback callback) {
        return new Get(url, queryParameterList, resultType, callback);
    }

    /**
     * Create new GET HTTP request builder
     *
     * @param url URL-address
     * @return New GET request builder instance
     */
    @NonNull
    public static Get.Builder newGetBuilder(@NonNull String url) {
        return new Get.Builder(url);
    }

    /**
     * Create new POST HTTP request
     *
     * @param url                URL-address
     * @param queryParameterList Query string parameter list
     * @param postParameterList  Request body multipart/form-data parameter list
     * @param resultType         Type of request result in callback
     * @param callback           Response callback
     * @return New GET request instance
     */
    @NonNull
    public static Post newPostRequest(@NonNull String url,
            @Nullable List<QueryParameter> queryParameterList,
            @Nullable List<PostParameter> postParameterList, @NonNull ResultType resultType,
            @Nullable Callback callback) {
        return new Post(url, queryParameterList, postParameterList, resultType, callback);
    }

    /**
     * Create new POST HTTP request builder
     *
     * @param url URL-address
     * @return New POST request instance
     */
    @NonNull
    public static Post.Builder newPostBuilder(@NonNull String url) {
        return new Post.Builder(url);
    }

    /**
     * Create new query string request parameter
     *
     * @param key   Key
     * @param value Value
     * @return New query string request parameter
     */
    @NonNull
    public static QueryParameter newQueryParameter(@NonNull String key, @Nullable String value) {
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.key = key;
        queryParameter.value = value;
        return queryParameter;
    }

    /**
     * Create new POST  request body parameter
     *
     * @param key   Key
     * @param value Value
     * @return New POST request body parameter
     */
    @NonNull
    public static PostParameter newPostParameter(@NonNull String key, @NonNull String value) {
        PostParameter postParameter = new PostParameter();
        postParameter.key = key;
        postParameter.value = value;
        return postParameter;
    }

    /**
     * Create new POST  request body parameter
     *
     * @param key  Key
     * @param file File
     * @return New POST request body parameter
     */
    @NonNull
    public static PostParameter newPostParameter(@NonNull String key, @NonNull File file) {
        PostParameter postParameter = new PostParameter();
        postParameter.key = key;
        postParameter.file = file;
        return postParameter;
    }

    /**
     * Create new POST  request body parameter
     *
     * @param key         Key
     * @param inputStream Input stream
     * @param fileName    File name
     * @param contentType Content type
     * @return New POST request body parameter
     */
    @NonNull
    public static PostParameter newPostParameter(@NonNull String key,
            @NonNull InputStream inputStream, @NonNull String fileName,
            @NonNull String contentType) {
        PostParameter postParameter = new PostParameter();
        postParameter.key = key;
        postParameter.stream = inputStream;
        postParameter.fileName = fileName;
        postParameter.contentType = contentType;
        return postParameter;
    }

    @NonNull
    private static String buildParamsUrlString(@NonNull List<QueryParameter> params,
            @NonNull String charset) throws UnsupportedEncodingException {
        StringBuilder dataBuilder = new StringBuilder();
        boolean firstEntry = true;
        for (QueryParameter queryParameter : params) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                dataBuilder.append("&");
            }
            if (queryParameter.key != null) {
                dataBuilder.append(URLEncoder.encode(queryParameter.key, charset));
                if (queryParameter.value != null) {
                    dataBuilder.append("=");
                    dataBuilder.append(URLEncoder.encode(queryParameter.value, charset));
                }
            }
        }
        return dataBuilder.toString();
    }

    private HttpRequest() {
    }

    /**
     * Query string request parameter
     */
    public static final class QueryParameter {
        private String key = null;
        private String value = null;

        private QueryParameter() {
        }
    }

    /**
     * Parameter of HTTP request (multipart/form-data)
     */
    public static final class PostParameter {
        private String key = null;
        private String value = null;
        private File file = null;
        private InputStream stream = null;
        private String fileName = null;
        private String contentType = null;

        private PostParameter() {
        }
    }

    /**
     * Result of HTTP request
     */
    public enum Result {
        SUCCESS,
        ERROR_HTTP,
        ERROR_MALFORMED_URL,
        ERROR_UNSUPPORTED_ENCODING,
        ERROR_PROTOCOL,
        ERROR_IO,
        ERROR_UNEXPECTED;

        private int mHttpCode = -1;
        private Exception mException = null;
        private String mDataString = null;
        private InputStream mDataStream = null;
        private HttpURLConnection mConnection = null;

        /**
         * Releases this connection so that its resources may be either reused or closed
         */
        public void disconnect() {
            if (mConnection != null) {
                try {
                    mConnection.disconnect();
                } catch (Exception ignored) {
                }
            }
        }

        private void setConnection(HttpURLConnection connection) {
            mConnection = connection;
        }

        public int getHttpCode() {
            return mHttpCode;
        }

        private void setHttpCode(int httpCode) {
            mHttpCode = httpCode;
        }

        @Nullable
        public Exception getException() {
            return mException;
        }

        private void setException(@Nullable Exception exception) {
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

        private void setDataString(@Nullable String dataString) {
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

        private void setDataStream(@Nullable InputStream dataStream) {
            mDataStream = dataStream;
        }
    }

    /**
     * Type of HTTP request result in Result
     */
    public enum ResultType {
        STRING,
        STREAM
    }

    /**
     * HTTP request callback.
     */
    public interface Callback {
        void onResult(Result requestResult);
    }

    /**
     * Common interface of HTTP requests.
     */
    public interface Request {
        Result execute();

        void executeOnNewThread();

        void executeOnExecutor(Executor executor);

        Runnable getAction();

        Result getResult();
    }

    /**
     * Instance class for GET requests.
     */
    public static final class Get implements Request {
        private static final String UTF_8 = "UTF-8";
        private static final String GET = "GET";
        private static final String ACCEPT_CHARSET = "Accept-Charset";
        private static final int CONNECTION_TIMEOUT = 10000;
        private final Object mResultLock = new Object();
        private final String mUrl;
        private final List<QueryParameter> mQueryParameterList;
        private final Callback mCallback;
        private final ResultType mResultType;
        private volatile Result mResult = null;

        private final Runnable mRequestAction = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    mResult = null;
                    String request = mUrl;
                    if (mQueryParameterList != null && mQueryParameterList.size() > 0) {
                        request += "?" + buildParamsUrlString(mQueryParameterList, UTF_8);
                    }
                    URL url = new URL(request);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(GET);
                    connection.setRequestProperty(ACCEPT_CHARSET, UTF_8);
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        switch (mResultType) {
                            case STREAM: {
                                Result result = Result.SUCCESS;
                                result.setConnection(connection);
                                result.setHttpCode(responseCode);
                                result.setDataStream(connection.getInputStream());
                                setResult(result);
                                if (mCallback != null) {
                                    mCallback.onResult(result);
                                }
                                break;
                            }
                            case STRING: {
                                try (BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(connection.getInputStream()))) {
                                    StringBuilder responseBuilder = new StringBuilder();
                                    for (; ; ) {
                                        String line = bufferedReader.readLine();
                                        if (line == null) {
                                            break;
                                        }
                                        responseBuilder.append(line);
                                    }
                                    Result result = Result.SUCCESS;
                                    result.setConnection(connection);
                                    result.setHttpCode(responseCode);
                                    result.setDataString(responseBuilder.toString());
                                    setResult(result);
                                    if (mCallback != null) {
                                        mCallback.onResult(result);
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        Result result = Result.ERROR_HTTP;
                        result.setConnection(connection);
                        result.setHttpCode(responseCode);
                        setResult(result);
                        if (mCallback != null) {
                            mCallback.onResult(result);
                        }
                    }
                } catch (MalformedURLException e) {
                    Result result = Result.ERROR_MALFORMED_URL;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (UnsupportedEncodingException e) {
                    Result result = Result.ERROR_UNSUPPORTED_ENCODING;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (ProtocolException e) {
                    Result result = Result.ERROR_PROTOCOL;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (IOException e) {
                    Result result = Result.ERROR_IO;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (Exception e) {
                    Result result = Result.ERROR_UNEXPECTED;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                }
            }
        };

        private Get(@NonNull String url, @Nullable List<QueryParameter> queryParameterList,
                @NonNull ResultType resultType, @Nullable Callback callback) {
            mUrl = url;
            mQueryParameterList = queryParameterList;
            mCallback = callback;
            mResultType = resultType;
        }

        private void setResult(Result result) {
            mResult = result;
            synchronized (mResultLock) {
                mResultLock.notifyAll();
            }
        }

        @Override
        public Result execute() {
            mRequestAction.run();
            return mResult;
        }

        @Override
        public void executeOnNewThread() {
            new Thread(mRequestAction).start();
        }

        @Override
        public void executeOnExecutor(@NonNull Executor executor) {
            executor.execute(mRequestAction);
        }

        @NonNull
        @Override
        public Runnable getAction() {
            return mRequestAction;
        }

        @Override
        public Result getResult() {
            for (; mResult == null; ) {
                try {
                    mResultLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return mResult;
        }

        /**
         * Builder for GET requests.
         */
        public static final class Builder {
            private final String mUrl;
            private List<QueryParameter> mQueryParameters = null;
            private ResultType mResultType = ResultType.STRING;
            private Callback mCallback = null;

            private Builder(@NonNull String url) {
                mUrl = url;
            }

            public Builder appendQueryParameter(@NonNull String key, @Nullable String value) {
                if (mQueryParameters == null) {
                    mQueryParameters = new ArrayList<>();
                }
                mQueryParameters.add(newQueryParameter(key, value));
                return this;
            }

            public Builder setResultType(@NonNull ResultType resultType) {
                mResultType = resultType;
                return this;
            }

            public Builder setCallback(@Nullable Callback callback) {
                mCallback = callback;
                return this;
            }

            @NonNull
            public Get build() {
                return new Get(mUrl, mQueryParameters, mResultType, mCallback);
            }
        }
    }

    /**
     * Instance class for POST requests.
     */
    public static final class Post implements Request {
        private static final String UTF_8 = "UTF-8";
        private static final String POST = "POST";
        private static final String KEY_CONTENT_TYPE = "Content-Type";
        private static final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=";
        private static final String KEY_ACCEPT_CHARSET = "Accept-Charset";
        private static final String LINE_END = "\r\n";
        private static final String KEY_CONNECTION = "Connection";
        private static final String KEEP_ALIVE = "keep-alive";
        private static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\"";
        private static final String QUOTE = "\"";
        private static final String DOUBLE_DASH = "--";
        private static final String CONTENT_TYPE_REQUEST = "Content-Type: ";
        private static final String PLAIN_TEXT = "text/plain; charset=" + UTF_8;
        private static final String FILENAME = "; filename=\"";
        private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: ";
        private static final String BINARY = "binary";
        private static final int CONNECTION_TIMEOUT = 10000;
        private static final int BUFFER_SIZE = 4096;
        private final Object mResultLock = new Object();
        private final String mUrl;
        private final List<QueryParameter> mQueryParameterList;
        private final List<PostParameter> mPostParameterList;
        private final Callback mCallback;
        private final ResultType mResultType;
        private volatile Result mResult = null;

        private final Runnable mRequestAction = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    mResult = null;
                    String boundary = "===" + System.currentTimeMillis() + "===";
                    String request = mUrl;
                    if (mQueryParameterList != null && mQueryParameterList.size() > 0) {
                        request += "?" + buildParamsUrlString(mQueryParameterList, UTF_8);
                    }
                    URL url = new URL(request);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod(POST);
                    connection.setRequestProperty(KEY_ACCEPT_CHARSET, UTF_8);
                    connection.setRequestProperty(KEY_CONTENT_TYPE, MULTIPART_FORM_DATA + boundary);
                    connection.setRequestProperty(KEY_CONNECTION, KEEP_ALIVE);
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);
                    OutputStream outputStream = connection.getOutputStream();
                    try (PrintWriter writer = new PrintWriter(
                            new OutputStreamWriter(outputStream, UTF_8), false)) {
                        if (mPostParameterList != null && mPostParameterList.size() > 0) {
                            for (PostParameter postParameter : mPostParameterList) {
                                if (postParameter.key != null) {
                                    if (postParameter.value != null) {
                                        writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                                .append(CONTENT_DISPOSITION)
                                                .append(postParameter.key).append(QUOTE)
                                                .append(LINE_END).append(CONTENT_TYPE_REQUEST)
                                                .append(PLAIN_TEXT).append(LINE_END)
                                                .append(LINE_END).append(postParameter.value)
                                                .append(LINE_END).flush();
                                    } else if (postParameter.file != null) {
                                        String fileName = postParameter.file.getName();
                                        String contentType =
                                                URLConnection.guessContentTypeFromName(fileName);
                                        writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                                .append(CONTENT_DISPOSITION)
                                                .append(postParameter.key).append(QUOTE)
                                                .append(FILENAME).append(fileName).append(QUOTE)
                                                .append(LINE_END).append(CONTENT_TYPE_REQUEST)
                                                .append(contentType).append(LINE_END)
                                                .append(CONTENT_TRANSFER_ENCODING).append(BINARY)
                                                .append(LINE_END).append(LINE_END).flush();
                                        try (FileInputStream fileInput = new FileInputStream(
                                                postParameter.file)) {
                                            byte[] buffer = new byte[BUFFER_SIZE];
                                            for (int read;
                                                    (read = fileInput.read(buffer)) != -1; ) {
                                                outputStream.write(buffer, 0, read);
                                            }
                                            outputStream.flush();
                                        }
                                        writer.append(LINE_END).flush();
                                    } else if (postParameter.stream != null &&
                                            postParameter.fileName != null &&
                                            postParameter.contentType != null) {
                                        writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                                .append(CONTENT_DISPOSITION)
                                                .append(postParameter.key).append(QUOTE)
                                                .append(FILENAME).append(postParameter.fileName)
                                                .append(QUOTE).append(LINE_END)
                                                .append(CONTENT_TYPE_REQUEST)
                                                .append(postParameter.contentType).append(LINE_END)
                                                .append(CONTENT_TRANSFER_ENCODING).append(BINARY)
                                                .append(LINE_END).append(LINE_END).flush();
                                        try (InputStream inputStream = postParameter.stream) {
                                            byte[] buffer = new byte[BUFFER_SIZE];
                                            for (int read;
                                                    (read = inputStream.read(buffer)) != -1; ) {
                                                outputStream.write(buffer, 0, read);
                                            }
                                            outputStream.flush();
                                        }
                                        writer.append(LINE_END).flush();
                                    }
                                }
                            }
                        }
                        writer.append(DOUBLE_DASH).append(boundary).append(DOUBLE_DASH)
                                .append(LINE_END).flush();
                    }
                    outputStream.flush();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        switch (mResultType) {
                            case STREAM: {
                                Result result = Result.SUCCESS;
                                result.setConnection(connection);
                                result.setHttpCode(responseCode);
                                result.setDataStream(connection.getInputStream());
                                setResult(result);
                                if (mCallback != null) {
                                    mCallback.onResult(result);
                                }
                                break;
                            }
                            case STRING: {
                                try (BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(connection.getInputStream()))) {
                                    StringBuilder responseBuilder = new StringBuilder();
                                    for (; ; ) {
                                        String line = bufferedReader.readLine();
                                        if (line == null) {
                                            break;
                                        }
                                        responseBuilder.append(line);
                                    }
                                    Result result = Result.SUCCESS;
                                    result.setConnection(connection);
                                    result.setHttpCode(responseCode);
                                    result.setDataString(responseBuilder.toString());
                                    setResult(result);
                                    if (mCallback != null) {
                                        mCallback.onResult(result);
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        Result result = Result.ERROR_HTTP;
                        result.setConnection(connection);
                        result.setHttpCode(responseCode);
                        setResult(result);
                        if (mCallback != null) {
                            mCallback.onResult(result);
                        }
                    }
                } catch (MalformedURLException e) {
                    Result result = Result.ERROR_MALFORMED_URL;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (UnsupportedEncodingException e) {
                    Result result = Result.ERROR_UNSUPPORTED_ENCODING;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (ProtocolException e) {
                    Result result = Result.ERROR_PROTOCOL;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (IOException e) {
                    Result result = Result.ERROR_IO;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                } catch (Exception e) {
                    Result result = Result.ERROR_UNEXPECTED;
                    result.setConnection(connection);
                    result.setException(e);
                    setResult(result);
                    if (mCallback != null) {
                        mCallback.onResult(result);
                    }
                }
            }
        };

        private Post(@NonNull String url, @Nullable List<QueryParameter> queryParameterList,
                @Nullable List<PostParameter> postParameterList, @NonNull ResultType resultType,
                @Nullable Callback callback) {
            mUrl = url;
            mQueryParameterList = queryParameterList;
            mPostParameterList = postParameterList;
            mCallback = callback;
            mResultType = resultType;
        }

        private void setResult(Result result) {
            mResult = result;
            synchronized (mResultLock) {
                mResultLock.notifyAll();
            }
        }

        @Override
        public Result execute() {
            mRequestAction.run();
            return mResult;
        }

        @Override
        public void executeOnNewThread() {
            new Thread(mRequestAction).start();
        }

        @Override
        public void executeOnExecutor(@NonNull Executor executor) {
            executor.execute(mRequestAction);
        }

        @NonNull
        @Override
        public Runnable getAction() {
            return mRequestAction;
        }

        @Override
        public Result getResult() {
            for (; mResult == null; ) {
                try {
                    mResultLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return mResult;
        }

        /**
         * Builder for POST requests.
         */
        public static final class Builder {
            private final String mUrl;
            private List<QueryParameter> mQueryParameters = null;
            private List<PostParameter> mPostParameters = null;
            private ResultType mResultType = ResultType.STRING;
            private Callback mCallback = null;

            private Builder(@NonNull String url) {
                mUrl = url;
            }

            public Builder appendQueryParameter(@NonNull String key, @Nullable String value) {
                if (mQueryParameters == null) {
                    mQueryParameters = new ArrayList<>();
                }
                mQueryParameters.add(newQueryParameter(key, value));
                return this;
            }

            public Builder appendPostParameter(@NonNull String key, @NonNull String value) {
                if (mPostParameters == null) {
                    mPostParameters = new ArrayList<>();
                }
                mPostParameters.add(newPostParameter(key, value));
                return this;
            }

            public Builder appendPostParameter(@NonNull String key, @NonNull File file) {
                if (mPostParameters == null) {
                    mPostParameters = new ArrayList<>();
                }
                mPostParameters.add(newPostParameter(key, file));
                return this;
            }

            public Builder appendPostParameter(@NonNull String key,
                    @NonNull InputStream inputStream, @NonNull String fileName,
                    @NonNull String contentType) {
                if (mPostParameters == null) {
                    mPostParameters = new ArrayList<>();
                }
                mPostParameters.add(newPostParameter(key, inputStream, fileName, contentType));
                return this;
            }

            public Builder setResultType(@NonNull ResultType resultType) {
                mResultType = resultType;
                return this;
            }

            public Builder setCallback(@Nullable Callback callback) {
                mCallback = callback;
                return this;
            }

            @NonNull
            public Post build() {
                return new Post(mUrl, mQueryParameters, mPostParameters, mResultType, mCallback);
            }
        }
    }
}
