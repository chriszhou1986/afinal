package net.tsz.afinal.http.entityhandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-7-17
 * Time: 上午10:41
 */
public class DefaultDownloadRedirectHandler implements DownloadRedirectHandler {
    @Override
    public HttpRequestBase getDirectRequest(HttpResponse response) {
        if (response.containsHeader("Location")) {
            String location = response.getFirstHeader("Location").getValue();
            HttpGet request = new HttpGet(location);
            if (response.containsHeader("Set-Cookie")) {
                String cookie = response.getFirstHeader("Set-Cookie").getValue();
                request.addHeader("Cookie", cookie);
            }
            return request;
        }
        return null;
    }
}
