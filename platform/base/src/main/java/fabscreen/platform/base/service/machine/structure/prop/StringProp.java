package fabscreen.platform.base.service.machine.structure.prop;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okio.Buffer;


public class StringProp extends BasicProp<String> {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    private int mLen;

    public StringProp() {
        this.mValue = "";
    }

    @Override
    public String readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }

    public StringProp(String mValue) {
        this.mValue = mValue;
        this.mLen = mValue.length();
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.writeShortLe(mValue.getBytes().length);
        buffer.write(mValue.getBytes());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws EOFException {
        mLen = buffer.readShortLe() & 0xffff;
        mValue = buffer.readString(mLen, UTF_8);
        return buffer;
    }

    @Override
    public String toString() {
        return "Len is: " + mLen + " content is: " + mValue;
    }
}
