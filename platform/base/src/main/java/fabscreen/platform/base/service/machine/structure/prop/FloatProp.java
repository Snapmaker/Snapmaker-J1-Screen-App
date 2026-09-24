package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;

import okio.Buffer;

public class FloatProp extends BasicProp<Float> {


    public FloatProp(float mValue) {
        this.mValue = mValue;
    }

    public FloatProp() {
        this.mValue = 0f;
    }

    @Override
    public Float readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.writeIntLe(Math.round(mValue * 1000));
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mValue = (float) (buffer.readIntLe() / 1000.0);
        return buffer;
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
