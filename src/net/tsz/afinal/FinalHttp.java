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
import net.tsz.afinal.http.entityhandler.DownloadRedirectHandler;
import net.tsz.afinal.http.entityhandler.UploadCallBack;
import net.tsz.afinal.http.methods.HttpCopy;
import net.tsz.afinal.http.methods.HttpMove;
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
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FinalHttp {

    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    private HttpContext httpContext = new BasicHttpContext();

    private String charset = "UTF-8";

    private final List<Header> headers = new ArrayList<Header>();

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
     */
    public void addHeader(Header header) {
        headers.add(header);
    }

    /**
     * 添加http请求头
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
    }

    /**
     * 添加http请求头
     *
     * @param headers
     */
    public void addHeaders(List<Header> headers) {
        this.headers.addAll(headers);
    }

    private void setHeaders2Request(HttpRequestBase request) {
        if (request != null && headers.size() > 0) {
            Header[] headerArray = new Header[headers.size()];
            request.setHeaders(headers.toArray(headerArray));
        }
    }


    //------------------get 请求-----------------------
    public void get(String url, AsyncCallBack<? extends Object> callBack) {
        get(url, null, callBack);
    }

    public void get(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        HttpGet request = new HttpGet(getUrlWithQueryString(url, params));
        setHeaders2Request(request);
        sendRequest(request, null, callBack);
    }

    public Object getSync(String url) {
        return getSync(url, null);
    }

    public Object getSync(String url, RequestParams params) {
        HttpGet request = new HttpGet(getUrlWithQueryString(url, params));
        setHeaders2Request(request);
        return sendSyncRequest(request, null);
    }

    //------------------post 请求-----------------------
    public HttpHandler post(String url, AsyncCallBack<? extends Object> callBack) {
        return post(url, null, callBack);
    }

    public HttpHandler post(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        return post(url, paramsToEntity(params), null, callBack);
    }

    public HttpHandler post(String url, RequestParams params, String contentType, AsyncCallBack<? extends Object> callBack) {
        return post(url, paramsToEntity(params), contentType, callBack);
    }

    public HttpHandler post(String url, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpPost request = new HttpPost(url);
        if (entity != null) {
            request.setEntity(entity);
        }

        setHeaders2Request(request);
        return sendRequest(request, entity, contentType, callBack);
    }

    public Object postSync(String url) {
        return postSync(url, null);
    }

    public Object postSync(String url, RequestParams params) {
        return postSync(url, paramsToEntity(params), null);
    }

    public Object postSync(String url, RequestParams params, String contentType) {
        return postSync(url, paramsToEntity(params), contentType);
    }

    public Object postSync(String url, HttpEntity entity, String contentType) {
        HttpPost request = new HttpPost(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        setHeaders2Request(request);
        return sendSyncRequest(request, contentType);
    }

    //------------------move 请求-----------------------
    public HttpHandler move(String url, AsyncCallBack<? extends Object> callBack) {
        return move(url, null, callBack);
    }

    public HttpHandler move(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        return move(url, paramsToEntity(params), null, callBack);
    }

    public HttpHandler move(String url, RequestParams params, String contentType, AsyncCallBack<? extends Object> callBack) {
        return move(url, paramsToEntity(params), contentType, callBack);
    }

    public HttpHandler move(String url, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpMove request = new HttpMove(url);
        if (entity != null) {
            request.setEntity(entity);
        }

        setHeaders2Request(request);
        return sendRequest(request, entity, contentType, callBack);
    }

    public Object moveSync(String url) {
        return moveSync(url, null);
    }

    public Object moveSync(String url, RequestParams params) {
        return moveSync(url, paramsToEntity(params), null);
    }

    public Object moveSync(String url, RequestParams params, String contentType) {
        return moveSync(url, paramsToEntity(params), contentType);
    }

    public Object moveSync(String url, HttpEntity entity, String contentType) {
        HttpMove request = new HttpMove(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        setHeaders2Request(request);
        return sendSyncRequest(request, contentType);
    }

    //------------------copy 请求-----------------------
    public HttpHandler copy(String url, AsyncCallBack<? extends Object> callBack) {
        return copy(url, null, callBack);
    }

    public HttpHandler copy(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        return copy(url, paramsToEntity(params), null, callBack);
    }

    public HttpHandler copy(String url, RequestParams params, String contentType, AsyncCallBack<? extends Object> callBack) {
        return copy(url, paramsToEntity(params), contentType, callBack);
    }

    public HttpHandler copy(String url, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpCopy request = new HttpCopy(url);
        if (entity != null) {
            request.setEntity(entity);
        }

        setHeaders2Request(request);
        return sendRequest(request, entity, contentType, callBack);
    }

    public Object copySync(String url) {
        return copySync(url, null);
    }

    public Object copySync(String url, RequestParams params) {
        return copySync(url, paramsToEntity(params), null);
    }

    public Object copySync(String url, RequestParams params, String contentType) {
        return copySync(url, paramsToEntity(params), contentType);
    }

    public Object copySync(String url, HttpEntity entity, String contentType) {
        HttpCopy request = new HttpCopy(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        setHeaders2Request(request);
        return sendSyncRequest(request, contentType);
    }

    //------------------put 请求-----------------------
    public void put(String url, AsyncCallBack<? extends Object> callBack) {
        put(url, null, callBack);
    }

    public void put(String url, RequestParams params, AsyncCallBack<? extends Object> callBack) {
        put(url, paramsToEntity(params), null, callBack);
    }

    public void put(String url, RequestParams params, String contentType, AsyncCallBack<? extends Object> callBack) {
        put(url, paramsToEntity(params), contentType, callBack);
    }

    public void put(String url, HttpEntity entity, String contentType, AsyncCallBack<? extends Object> callBack) {
        HttpPut request = new HttpPut(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        setHeaders2Request(request);
        sendRequest(request, contentType, callBack);
    }

    public Object putSync(String url) {
        return putSync(url, null);
    }

    public Object putSync(String url, RequestParams params) {
        return putSync(url, paramsToEntity(params), null);
    }

    public Object putSync(String url, RequestParams params, String contentType) {
        return putSync(url, paramsToEntity(params), contentType);
    }

    public Object putSync(String url, HttpEntity entity, String contentType) {
        HttpPut request = new HttpPut(url);
        if (entity != null) {
            request.setEntity(entity);
        }
        setHeaders2Request(request);
        return sendSyncRequest(request, contentType);
    }


    //------------------delete 请求-----------------------
    public void delete(String url, AsyncCallBack<? extends Object> callBack) {
        final HttpDelete request = new HttpDelete(url);
        setHeaders2Request(request);
        sendRequest(request, null, callBack);
    }

    public Object deleteSync(String url) {
        final HttpDelete request = new HttpDelete(url);
        setHeaders2Request(request);
        return sendSyncRequest(request, null);
    }


    //---------------------下载---------------------------------------
    public HttpHandler<File> download(String url, String target,
                                      AsyncCallBack<File> callback) {
        return download(url, null, target, false, null, callback);
    }

    public HttpHandler<File> download(String url, String target,
                                      DownloadRedirectHandler downloadRedirectHandler, AsyncCallBack<File> callback) {
        return download(url, null, target, false, downloadRedirectHandler, callback);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      AsyncCallBack<File> callback) {
        return download(url, null, target, isResume, null, callback);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      DownloadRedirectHandler downloadRedirectHandler, AsyncCallBack<File> callback) {
        return download(url, null, target, isResume, downloadRedirectHandler, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      AsyncCallBack<File> callback) {
        return download(url, params, target, false, null, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      DownloadRedirectHandler downloadRedirectHandler, AsyncCallBack<File> callback) {
        return download(url, params, target, false, downloadRedirectHandler, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      AsyncCallBack<File> callback) {
        return download(url, params, target, isResume, null, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      DownloadRedirectHandler downloadRedirectHandler, AsyncCallBack<File> callback) {
        final HttpGet request = new HttpGet(getUrlWithQueryString(url, params));
        HttpHandler<File> handler = new HttpHandler<File>(httpClient, httpContext, charset, callback);
        handler.setDownloadRedirectHandler(downloadRedirectHandler);
        setHeaders2Request(request);
        handler.executeOnExecutor(executor, request, target, isResume);
        return handler;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private <T> HttpHandler<T> sendRequest(HttpRequestBase request, HttpEntity entity, String contentType, AsyncCallBack<T> callBack) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        HttpHandler handler = new HttpHandler(httpClient, httpContext, charset, callBack);

        if (entity != null && entity instanceof UploadCallBack) {
            ((UploadCallBack) entity).setCallBack(handler);
        }

        handler.executeOnExecutor(executor, request);
        return handler;
    }

    protected <T> HttpHandler<T> sendRequest(HttpRequestBase request, String contentType, AsyncCallBack<T> callBack) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        HttpHandler handler = new HttpHandler(httpClient, httpContext, charset, callBack);
        handler.executeOnExecutor(executor, request);
        return handler;
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
