package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;

import okio.Buffer;

public class UInt16Prop extends BasicProp<Integer> {

    public UInt16Prop() {
        this.mValue = 0;
    }

    @Override
    public Integer readBufferToValue(Buffer buffer) throws EOFException {
        readBuffer(buffer);
        return mValue;
    }

    public UInt16Prop(int mValue) {
        this.mValue = mValue;
    }

    @Override
    public byte[] toByteArray() {
        byte[] src = new byte[2];
        int value = mValue;
        src[1] = (byte) ((value >> 8) & 0xff);
        src[0] = (byte) (value & 0xff);
        return src;
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = (buffer.readByte() & 0xFF) | ((buffer.readByte() & 0xFF) << 8);
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
