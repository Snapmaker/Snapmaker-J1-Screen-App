package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;

public class Int8Prop extends BasicProp<Byte> {

    public Int8Prop(byte mValue) {
        this.mValue = mValue;
    }

    public Int8Prop() {
        this.mValue = 0;
    }

    @Override
    public Byte readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[]{mValue};
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = buffer.readByte();
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }

}
