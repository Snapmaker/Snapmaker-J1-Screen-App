package fabscreen.platform.base.lib.file;

import java.io.IOException;
import java.io.InputStream;

public class FabFileInputStream {
    InputStream mInputStream;
    FabUsbFile.FabFileStreamListener mFabFileInputStreamListener;

    public FabFileInputStream(InputStream inputStream) {
        mInputStream = inputStream;
    }

    public FabFileInputStream(InputStream inputStream, FabUsbFile.FabFileStreamListener fabFileInputStreamListener) {
        mInputStream = inputStream;
        mFabFileInputStreamListener = fabFileInputStreamListener;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public int read() throws IOException {
        return mInputStream.read();
    }

    public void close() throws IOException {
        mInputStream.close();
        if (mFabFileInputStreamListener != null) {
            mFabFileInputStreamListener.close();
        }

    }
}
