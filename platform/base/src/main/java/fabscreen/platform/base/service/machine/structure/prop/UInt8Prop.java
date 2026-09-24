package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;

public class UInt8Prop extends BasicProp<Integer> {


    public UInt8Prop() {
        this.mValue = 0;
    }

    @Override
    public Integer readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    public UInt8Prop(int mValue) {
        this.mValue = mValue;
    }

    @Override
    public byte[] toByteArray() {
        byte[] src = new byte[1];
        src[0] = (byte) (mValue & 0xff);
        return src;
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = buffer.readByte() & 0xff;
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
