package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;


public class BytesProp extends BasicProp<byte[]> {
    private int mLen;

    public BytesProp() {
        this.mValue = new byte[mLen];
    }

    @Override
    public byte[] readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    public BytesProp(byte[] mValue) {
        this.mValue = mValue;
        this.mLen = mValue.length;
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.writeShortLe(mValue.length);
        buffer.write(mValue);
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mLen = buffer.readShortLe() & 0xffff;
        mValue = buffer.readByteArray(mLen);
        return buffer;
    }

    @Override
    public String toString() {
        return "Len is: " + mLen + " content is: " + mValue;
    }
}
