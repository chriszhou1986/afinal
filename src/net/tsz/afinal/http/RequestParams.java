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
package net.tsz.afinal.http;

import net.tsz.afinal.http.content.ContentBody;
import net.tsz.afinal.http.content.FileBody;
import net.tsz.afinal.http.content.InputStreamBody;
import net.tsz.afinal.http.content.StringBody;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p/>
 * 使用方法:
 * <p/>
 * <pre>
 * RequestParams params = new RequestParams();
 * params.put("username", "michael");
 * params.put("password", "123456");
 * params.put("email", "test@tsz.net");
 * params.put("profile_picture", new File("/mnt/sdcard/pic.jpg")); // 上传文件
 * params.put("profile_picture2", inputStream); // 上传数据流
 * params.put("profile_picture3", new ByteArrayInputStream(bytes)); // 提交字节流
 *
 * FinalHttp fh = new FinalHttp();
 * fh.post("http://www.yangfuhai.com", params, new AsyncCallBack<String>(){
 *        @Override
 * 		public void onLoading(long count, long current) {
 * 				textView.setText(current+"/"+count);
 * 		}
 *
 * 		@Override
 * 		public void onSuccess(String t) {
 * 			textView.setText(t==null?"null":t);
 * 		}
 * });
 * </pre>
 */
public class RequestParams {
    private static String ENCODING = "UTF-8";

    protected ConcurrentHashMap<String, String> urlParams = new ConcurrentHashMap<String, String>();
    protected ConcurrentHashMap<String, ContentBody> fileParams = new ConcurrentHashMap<String, ContentBody>();

    public RequestParams() {
    }

    public RequestParams(Map<String, String> source) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public RequestParams(String key, String value) {
        put(key, value);
    }

    public RequestParams(Object... keysAndValues) {
        int len = keysAndValues.length;
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Supplied arguments must be even");
        }
        for (int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    public void put(String key, File file) throws FileNotFoundException {
        fileParams.put(key, new FileBody(file));
    }

    public void put(String key, File file, String mimeType) throws FileNotFoundException {
        fileParams.put(key, new FileBody(file, mimeType));
    }

    public void put(String key, File file, String mimeType, String charset) throws FileNotFoundException {
        fileParams.put(key, new FileBody(file, mimeType, charset));
    }

    public void put(String key, InputStream stream, String fileName) {
        fileParams.put(key, new InputStreamBody(stream, fileName));
    }

    public void put(String key, InputStream stream, String mimeType, String fileName) {
        fileParams.put(key, new InputStreamBody(stream, mimeType, fileName));
    }

    public void remove(String key) {
        urlParams.remove(key);
        fileParams.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        return result.toString();
    }

    /**
     * Returns an HttpEntity containing all request parameters
     */
    public HttpEntity getEntity() {
        HttpEntity result = null;

        if (!fileParams.isEmpty()) {

            /*if (fileParams.size() == 1 && (urlParams == null || urlParams.size() == 0)) {
                for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
                    ContentBody body = entry.getValue();
                    if (body instanceof FileBody) {
                        FileBody fileBody = (FileBody) body;
                        UploadFileEntity fileEntity = new UploadFileEntity(fileBody.getFile(), fileBody.getMimeType());
                        return fileEntity;
                    }
                }
            }*/

            MultipartEntity multipartEntity = new MultipartEntity();

            for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
                try {
                    multipartEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
                ContentBody file = entry.getValue();
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }

            result = multipartEntity;
        } else {
            try {
                result = new UrlEncodedFormEntity(getParamsList(), ENCODING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return lparams;
    }

    public String getParamString() {
        return URLEncodedUtils.format(getParamsList(), ENCODING);
    }
}