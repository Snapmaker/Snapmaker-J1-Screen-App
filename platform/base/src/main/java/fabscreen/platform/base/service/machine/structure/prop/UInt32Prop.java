package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;

public class UInt32Prop extends BasicProp<Long> {
    public UInt32Prop() {
        this.mValue = 0L;
    }

    @Override
    public Long readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    public UInt32Prop(long value) {
        mValue = value;
    }

    @Override
    public byte[] toByteArray() {
        byte[] src = new byte[4];
        src[3] = (byte) ((mValue >> 24) & 0xff);
        src[2] = (byte) ((mValue >> 16) & 0xff);
        src[1] = (byte) ((mValue >> 8) & 0xff);
        src[0] = (byte) (mValue & 0xff);
        return src;
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = (long) (((buffer.readByte() & 0xFF))
                | ((buffer.readByte() & 0xFF) << 8)
                | ((buffer.readByte() & 0xFF) << 16)
                | ((buffer.readByte() & 0xFF) << 24));
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
