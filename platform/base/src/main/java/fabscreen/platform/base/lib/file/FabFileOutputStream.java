package fabscreen.platform.base.lib.file;

import java.io.IOException;
import java.io.OutputStream;

public class FabFileOutputStream {
    OutputStream mOutputStream;
    FabUsbFile.FabFileStreamListener mFabFileOutputStreamListener;

    public FabFileOutputStream(OutputStream inputStream) {
        mOutputStream = inputStream;
    }

    public FabFileOutputStream(OutputStream inputStream, FabUsbFile.FabFileStreamListener fabFileOutputStreamListener) {
        mOutputStream = inputStream;
        mFabFileOutputStreamListener = fabFileOutputStreamListener;
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public void write(int b) throws IOException {
        mOutputStream.write(b);
    }

    public void close() throws IOException {
        mOutputStream.close();
        if (mFabFileOutputStreamListener != null) {
            mFabFileOutputStreamListener.close();
        }

    }

}
