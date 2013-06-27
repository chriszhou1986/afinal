package net.tsz.afinal.http;

import net.tsz.afinal.http.entityhandler.EntityCallBack;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-6-28
 * Time: 上午12:14
 */
public class UploadInputStreamEntity extends AbstractHttpEntity {

    private final static int BUFFER_SIZE = 2048;

    private final InputStream content;
    private final long length;

    public UploadInputStreamEntity(final InputStream instream, long length) {
        super();
        if (instream == null) {
            throw new IllegalArgumentException("Source input stream may not be null");
        }
        this.content = instream;
        this.length = length;

        uploadedSize = 0;
    }

    public boolean isRepeatable() {
        return false;
    }

    public long getContentLength() {
        return this.length;
    }

    public InputStream getContent() throws IOException {
        return this.content;
    }

    public EntityCallBack callback = null;

    private long uploadedSize;

    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = this.content;
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int l;
            if (this.length < 0) {
                // consume until EOF
                while ((l = instream.read(buffer)) != -1) {
                    outstream.write(buffer, 0, l);
                    uploadedSize += l;
                    if (callback != null) {
                        callback.callBack(uploadedSize, uploadedSize + 1, false);
                    }
                }
            } else {
                // consume no more than length
                long remaining = this.length;
                while (remaining > 0) {
                    l = instream.read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
                    if (l == -1) {
                        break;
                    }
                    outstream.write(buffer, 0, l);
                    remaining -= l;
                    uploadedSize += l;
                    if (callback != null) {
                        callback.callBack(uploadedSize, length, false);
                    }
                }
            }
            outstream.flush();
            if (callback != null) {
                callback.callBack(uploadedSize, uploadedSize, true);
            }
        } finally {
            instream.close();
        }
    }

    public boolean isStreaming() {
        return true;
    }

    /**
     * @deprecated Either use {@link #getContent()} and call {@link java.io.InputStream#close()} on that;
     *             otherwise call {@link #writeTo(OutputStream)} which is required to free the resources.
     */
    public void consumeContent() throws IOException {
        // If the input stream is from a connection, closing it will read to
        // the end of the content. Otherwise, we don't care what it does.
        this.content.close();
    }

}