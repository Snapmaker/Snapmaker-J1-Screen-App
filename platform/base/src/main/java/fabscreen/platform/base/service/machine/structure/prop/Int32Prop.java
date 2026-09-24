package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;

public class Int32Prop extends BasicProp<Integer> {

    public Int32Prop() {
        this.mValue = 0;
    }

    @Override
    public Integer readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    public Int32Prop(int mValue) {
        this.mValue = mValue;
    }

    @Override
    public byte[] toByteArray() {
        byte[] src = new byte[4];
        src[3] = (byte) ((mValue >> 24));
        src[2] = (byte) ((mValue >> 16));
        src[1] = (byte) ((mValue >> 8));
        src[0] = (byte) (mValue & 0xFF);
        return src;
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue =
                (buffer.readByte()) |
                        ((buffer.readByte()) << 8) |
                        ((buffer.readByte()) << 16) |
                        ((buffer.readByte()) << 24);
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
