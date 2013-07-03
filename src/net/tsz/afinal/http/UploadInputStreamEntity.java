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
public class UploadInputStreamEntity extends AbstractHttpEntity implements UploadCallBack {

    private final static int BUFFER_SIZE = 2048;

    private final InputStream content;
    private final long length;

    public UploadInputStreamEntity(final InputStream inputStream, long length) {
        super();
        if (inputStream == null) {
            throw new IllegalArgumentException("Source input stream may not be null");
        }
        this.content = inputStream;
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

    private long uploadedSize;

    public void writeTo(final OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream inStream = this.content;
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int l;
            if (this.length < 0) {
                // consume until EOF
                while ((l = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, l);
                    uploadedSize += l;
                    if (callback != null) {
                        if (!callback.updateProgress(uploadedSize + 1, uploadedSize, false)) {
                            throw new IOException("stop");
                        }
                    }
                }
            } else {
                // consume no more than length
                long remaining = this.length;
                while (remaining > 0) {
                    l = inStream.read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
                    if (l == -1) {
                        break;
                    }
                    outStream.write(buffer, 0, l);
                    remaining -= l;
                    uploadedSize += l;
                    if (callback != null) {
                        if (!callback.updateProgress(length, uploadedSize, false)) {
                            throw new IOException("stop");
                        }
                    }
                }
            }
            outStream.flush();
            if (callback != null) {
                callback.updateProgress(length, uploadedSize, true);
            }
        } finally {
            inStream.close();
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

    private EntityCallBack callback = null;

    @Override
    public void setCallBack(EntityCallBack callBack) {
        this.callback = callBack;
    }
}