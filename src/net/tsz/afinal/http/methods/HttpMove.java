package net.tsz.afinal.http.methods;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-7-12
 * Time: 下午9:03
 */
public class HttpMove extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "MOVE";

    public HttpMove() {
        super();
    }

    public HttpMove(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpMove(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}