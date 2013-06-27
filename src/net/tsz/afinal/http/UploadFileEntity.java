package net.tsz.afinal.http;

import net.tsz.afinal.http.entityhandler.EntityCallBack;
import org.apache.http.entity.FileEntity;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-6-24
 * Time: 下午4:45
 */
public class UploadFileEntity extends FileEntity {

    public UploadFileEntity(File file, String contentType) {
        super(file, contentType);
        fileSize = file.length();
        uploadedSize = 0;
    }

    public EntityCallBack callback = null;

    private long fileSize;
    private long uploadedSize;

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream inStream = new FileInputStream(this.file);
        try {
            byte[] tmp = new byte[4096];
            int len;
            while ((len = inStream.read(tmp)) != -1) {
                outStream.write(tmp, 0, len);
                uploadedSize += len;
                if (callback != null) {
                    callback.callBack(uploadedSize, fileSize, false);
                }
            }
            outStream.flush();
            if (callback != null) {
                callback.callBack(uploadedSize, fileSize, true);
            }
        } finally {
            inStream.close();
        }
    }
}