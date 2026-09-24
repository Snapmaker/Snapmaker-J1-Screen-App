package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;

public class BoolProp extends BasicProp<Boolean> {

    public BoolProp(boolean b) {
        mValue = b;
    }

    public BoolProp() {
        mValue = false;
    }

    @Override
    public Boolean readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[]{(byte) (mValue ? 1 : 0)};
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = buffer.readByte() == 1;
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
