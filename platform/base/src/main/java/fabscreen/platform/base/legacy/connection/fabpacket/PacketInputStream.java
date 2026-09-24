package fabscreen.platform.base.legacy.connection.fabpacket;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import fabscreen.platform.base.legacy.connection.IPacket;

// TODO: 2022/4/6 unit test
public abstract class PacketInputStream implements Closeable {
    protected static int DEFAULT_BUFFER_SIZE = 81920;
    protected static int DEFAULT_FILL_BUFFER_SIZE = 128;
    protected volatile byte[] buf;
    // length of valid data in buf
    protected int count;
    protected int pos;
    protected InputStream in;

    protected PacketInputStream(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    protected PacketInputStream(InputStream in, int size) {
        this.in = in;
        buf = new byte[size];
    }

    protected byte[] getBufIfOpen() throws IOException {
        byte[] buffer = buf;
        if (buffer == null) {
            throw new IOException("Stream closed");
        }
        return buffer;
    }

    protected int fill(int length) throws IOException {
        byte[] buffer = getBufIfOpen();
        if (count + length >= buffer.length) {
            int nsz = buffer.length * 2;
            byte[] nbuf = new byte[nsz];
            System.arraycopy(buffer, pos, nbuf, 0, count - pos);
            count -= pos;
            pos = 0;
            buffer = nbuf;
        }

        final int n = in.read(buffer, count, length);
        if (n > 0) {
            count += n;
        }
        return n;
    }

    protected int fullfill(int length) throws IOException {
        int left = length;
        while (left > 0) {
            int f = fill(left);
            if (f == -1) {
                return -1;
            }
            left -= f;
        }
        return length;
    }

    @Deprecated
    public abstract IPacket readPacket() throws IOException;

    public abstract byte[] readRawPacket() throws IOException;

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
}
