/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
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
package net.tsz.afinal;

import net.tsz.afinal.http.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FinalHttp {

    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    private HttpContext httpContext = new BasicHttpContext();

    private String charset = "UTF-8";

    private final Map<String, String> clientHeaderMap = new HashMap<String, String>();

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread tread = new Thread(r, "FinalHttp #" + mCount.getAndIncrement());
            tread.setPriority(Thread.NORM_PRIORITY - 1);
            return tread;
        }
    };
    private static int httpThreadCount = 3;//http线程池数量
    private static final Executor executor = Executors.newFixedThreadPool(httpThreadCount, sThreadFactory);

    private final static int DEFAULT_RETRY_TIMES = 5;

    public FinalHttp() {

        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_RETRY_TIMES));

    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public void configCharset(String charSet) {
        if (charSet != null && charSet.trim().length() != 0)
            this.charset = charSet;
    }

    public void configCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }


    public void configUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }


    /**
     * 设置网络连接超时时间，默认为10秒钟
     *
     * @param timeout
     */
    public void configTimeout(int timeout) {
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

    /**
     * 设置https请求时  的 SSLSocketFactory
     *
     * @param sslSocketFactory
     */
    public void configSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        Scheme scheme = new Scheme("https", sslSocketFactory, 443);
        this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    /**
     * 配置错误重试次数
     *
     * @param count
     */
    public void configRequestExecutionRetryCount(int count) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(count));
    }

    /**
     * 添加http请求头
     *
     * @param header
     * @param value
     */
    public void addHeader(String header, String value) {
        clientHeaderMap.put(header, value);
    }


    //------------------get 请求-----------------------
    public void get(String url, AsyncCallBack<? extends Object> callBack) {
        get(url, null, callBack);
    }

    public void get(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        HttpGet getRequest = new HttpGet(getUrlWithQueryString(url, params));
        sendRequest(getRequest, null, callBack);
    }

    public void get(String url, Header[] headers, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        HttpGet getRequest = new HttpGet(getUrlWithQueryString(url, params));
        if (headers != null) getRequest.setHeaders(headers);
        sendRequest(getRequest, null, callBack);
    }

    public Object getSync(String url) {
        return getSync(url, null);
    }

    public Object getSync(String url, RequestParams params) {
        HttpGet getRequest = new HttpGet(getUrlWithQueryString(url, params));
        return sendSyncRequest(getRequest, null);
    }


    public Object getSync(String url, Header[] headers, RequestParams params) {
        HttpGet getRequest = new HttpGet(getUrlWithQueryString(url, params));
        if (headers != null) getRequest.setHeaders(headers);
        return sendSyncRequest(getRequest, null);
    }


    //------------------post 请求-----------------------
    public void post(String url, AsyncCallBack<? extends Object> callBack) {
        post(url, null, callBack);
    }

    public void post(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        post(url, paramsToEntity(params), null, callBack);
    }

    public void post(String url, RequestParams params, String contentType, AsyncCallBack<? extends Object> callBack) {
        post(url, paramsToEntity(params), contentType, callBack);
    }

    public void post(String url, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpPost request = new HttpPost(url);
        if (entity != null) {
            request.setEntity(entity);
        }

        sendRequest(request, entity, contentType, callBack);
    }

    public <T> void post(String url, Header[] headers, RequestParams params, String contentType, AsyncCallBack<T> callBack) {
        HttpPost request = new HttpPost(url);
        HttpEntity entity = null;
        if (params != null) {
            entity = paramsToEntity(params);
            request.setEntity(entity);
        }
        if (headers != null) {
            request.setHeaders(headers);
        }

        sendRequest(request, entity, contentType, callBack);
    }

    public void post(String url, Header[] headers, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpPost request = new HttpPost(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        if (headers != null) {
            request.setHeaders(headers);
        }

        sendRequest(request, entity, contentType, callBack);
    }

    public Object postSync(String url) {
        return postSync(url, null);
    }

    public Object postSync(String url, RequestParams params) {
        return postSync(url, paramsToEntity(params), null);
    }

    public Object postSync(String url, HttpEntity entity, String contentType) {
        HttpPost request = new HttpPost(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        return sendSyncRequest(request, contentType);
    }


    public Object postSync(String url, Header[] headers, RequestParams params, String contentType) {
        HttpEntityEnclosingRequestBase request = new HttpPost(url);
        if (params != null) request.setEntity(paramsToEntity(params));
        if (headers != null) request.setHeaders(headers);
        return sendSyncRequest(request, contentType);
    }

    public Object postSync(String url, Header[] headers, HttpEntity entity, String contentType) {
        HttpPost request = new HttpPost(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        if (headers != null) {
            request.setHeaders(headers);
        }
        return sendSyncRequest(request, contentType);
    }


    //------------------put 请求-----------------------
    public void put(String url, AsyncCallBack<? extends Object> callBack) {
        put(url, null, callBack);
    }

    public void put(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        put(url, paramsToEntity(params), null, callBack);
    }

    public void put(String url, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpPut request = new HttpPut(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        sendRequest(request, contentType, callBack);
    }

    public void put(String url, Header[] headers, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpPut request = new HttpPut(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        if (headers != null) {
            request.setHeaders(headers);
        }
        sendRequest(request, contentType, callBack);
    }

    public Object putSync(String url) {
        return putSync(url, null);
    }

    public Object putSync(String url, RequestParams params) {
        return putSync(url, paramsToEntity(params), null);
    }

    public Object putSync(String url, HttpEntity entity, String contentType) {
        return putSync(url, null, entity, contentType);
    }


    public Object putSync(String url, Header[] headers, HttpEntity entity, String contentType) {
        HttpPut request = new HttpPut(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        if (headers != null) request.setHeaders(headers);
        return sendSyncRequest(request, contentType);
    }


    //------------------delete 请求-----------------------
    public void delete(String url, AsyncCallBack<? extends Object> callBack) {
        final HttpDelete delete = new HttpDelete(url);
        sendRequest(delete, null, callBack);
    }

    public void delete(String url, Header[] headers, AsyncCallBack<? extends Object> callBack) {
        final HttpDelete delete = new HttpDelete(url);
        if (headers != null) delete.setHeaders(headers);
        sendRequest(delete, null, callBack);
    }

    public Object deleteSync(String url) {
        return deleteSync(url, null);
    }

    public Object deleteSync(String url, Header[] headers) {
        final HttpDelete delete = new HttpDelete(url);
        if (headers != null) delete.setHeaders(headers);
        return sendSyncRequest(delete, null);
    }


    //---------------------下载---------------------------------------
    public HttpHandler<File> download(String url, String target, AsyncCallBack<File> callback) {
        return download(url, null, target, false, callback);
    }


    public HttpHandler<File> download(String url, String target, boolean isResume, AsyncCallBack<File> callback) {
        return download(url, null, target, isResume, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, AsyncCallBack<File> callback) {
        return download(url, params, target, false, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume, AsyncCallBack<File> callback) {
        final HttpGet get = new HttpGet(getUrlWithQueryString(url, params));
        HttpHandler<File> handler = new HttpHandler<File>(httpClient, httpContext, charset, callback);
        handler.executeOnExecutor(executor, get, target, isResume);
        return handler;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private <T> void sendRequest(HttpRequestBase request, HttpEntity entity, String contentType, AsyncCallBack<T> callBack) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        HttpHandler handler = new HttpHandler(httpClient, httpContext, charset, callBack);
        if (entity instanceof MultipartEntity) {
            ((MultipartEntity) entity).callBackInfo.callback = handler;
        } else if (entity instanceof UploadFileEntity) {
            ((UploadFileEntity) entity).callback = handler;
        }

        handler.executeOnExecutor(executor, request);
    }

    protected <T> void sendRequest(HttpRequestBase request, String contentType, AsyncCallBack<T> callBack) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        new HttpHandler<T>(httpClient, httpContext, charset, callBack).executeOnExecutor(executor, request);
    }

    protected Object sendSyncRequest(HttpRequestBase request, String contentType) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }
        return new SyncRequestHandler(httpClient, httpContext, charset).sendRequest(request);
    }

    public static String getUrlWithQueryString(String url, RequestParams params) {
        if (params != null) {
            String paramString = params.getParamString();
            url += "?" + paramString;
        }
        return url;
    }

    private HttpEntity paramsToEntity(RequestParams params) {
        HttpEntity entity = null;

        if (params != null) {
            entity = params.getEntity();
        }

        return entity;
    }
}
