package fabscreen.platform.base.service.machine.structure.prop;

import com.orhanobut.logger.Logger;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;
import okio.ByteString;

public class Int16Prop extends BasicProp<Integer> {

    public Int16Prop() {
        this.mValue = 0;
    }

    @Override
    public Integer readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    public Int16Prop(int mValue) {
        this.mValue = mValue;
    }

    @Override
    public byte[] toByteArray() {
        byte[] src = new byte[2];
        src[0] = (byte) (mValue & 0xFF);
        src[1] = (byte) ((mValue >> 8));
        return src;
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = buffer.readByte() | (buffer.readByte() << 8);
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
