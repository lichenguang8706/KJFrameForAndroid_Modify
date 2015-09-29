/*
 * Copyright (C) 2011 The Android Open Source Project, 张涛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kymjs.kjframe.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.kymjs.kjframe.utils.KJLoger;

/**
 * 网络请求执行器，将传入的Request使用HttpStack客户端发起网络请求，并返回一个NetworkRespond结果
 */
public class Network {
    protected static final boolean DEBUG = HttpConfig.DEBUG;
    protected final HttpStack mHttpStack;

    public Network(HttpStack httpStack) {
        mHttpStack = httpStack;
    }

    /**
     * 实际执行一个请求的方法
     * 将原有的HttpResponse替换成自定义的KJHttpResponse，彻底消除联网核心逻辑与HttpClient的关联
     * @param request
     *            一个请求任务
     * @return 一个不会为null的响应
     * @throws KJHttpException
     */
    public NetworkResponse performRequest(Request<?> request)
            throws KJHttpException {
        while (true) {
            KJHttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = new HashMap<String, String>();
            try {
                // 标记Http响应头在Cache中的tag
                Map<String, String> headers = new HashMap<String, String>();
                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);

                int statusCode = httpResponse.getStatusCode();
                responseHeaders = httpResponse.getHeaders();
                if (statusCode == KJHttpStatus.SC_NOT_MODIFIED) { // 304
                    return new NetworkResponse(KJHttpStatus.SC_NOT_MODIFIED,
                            request.getCacheEntry() == null ? null : request
                                    .getCacheEntry().data,
                            responseHeaders, true);
                }

                if (httpResponse.getContent() != null) {
                    if (request instanceof FileRequest) {
                        responseContents = ((FileRequest) request)
                                .handleResponse(httpResponse);
                    } else {
                        responseContents = streamToBytes(httpResponse);
                    }
                } else {
                    responseContents = new byte[0];
                }

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents,
                        responseHeaders, false);
            } catch (SocketTimeoutException e) {
                throw new KJHttpException(new SocketTimeoutException(
                        "socket timeout"));
            } catch (InterruptedIOException e) {
                throw new KJHttpException(new InterruptedIOException(
                        "connection timeout"));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusCode();
                } else {
                    throw new KJHttpException("NoConnection error", e);
                }
                KJLoger.debug("Unexpected response code %d for %s", statusCode,
                        request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode,
                            responseContents, responseHeaders, false);
                    if (statusCode == KJHttpStatus.SC_UNAUTHORIZED
                            || statusCode == KJHttpStatus.SC_FORBIDDEN) {
                        throw new KJHttpException("auth error");
                    } else {
                        throw new KJHttpException(
                                "server error, Only throw ServerError for 5xx status codes.",
                                networkResponse);
                    }
                } else {
                    throw new KJHttpException(networkResponse);
                }
            }
        }
    }

    /**
     * 标记Respondeader响应头在Cache中的tag
     * 
     * @param headers
     * @param entry
     */
    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        if (entry == null) {
            return;
        }
        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }
        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            headers.put("If-Modified-Since", sdf.format(refTime));
        }
    }

    /**
     * 把报文流转换为byte[]（by 我是无名氏）
     * 
     * @param kjHttpResponse
     * @return
     * @throws IOException
     * @throws KJHttpException
     */
    private byte[] streamToBytes(KJHttpResponse kjHttpResponse) throws IOException,
            KJHttpException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
                ByteArrayPool.get(), (int) kjHttpResponse.getContentLength());
        byte[] buffer = null;
        InputStream in = null;
        try {
            in = kjHttpResponse.getContent();
            if (in == null) {
                throw new KJHttpException("server error");
            }
            buffer = ByteArrayPool.get().getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                if(in != null) {
                	in.close();
                }
            } catch (IOException e) {
                KJLoger.debug("Error occured when calling consumingContent");
            }
            ByteArrayPool.get().returnBuf(buffer);
            bytes.close();
        }
    }
}
