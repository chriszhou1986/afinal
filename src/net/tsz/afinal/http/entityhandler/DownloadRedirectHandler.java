package net.tsz.afinal.http.entityhandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-7-17
 * Time: 上午10:36
 */
public interface DownloadRedirectHandler {
    HttpRequestBase getDirectRequest(HttpResponse response);
}
