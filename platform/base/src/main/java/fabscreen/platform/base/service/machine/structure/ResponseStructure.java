package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class ResponseStructure<T extends IStructure> implements IStructure {
    // errorCode
    public UInt8Prop resultProp = new UInt8Prop(0);
    // "body"
    public T dataProp;

    public ResponseStructure(T data) {
        dataProp = data;
    }

    public ResponseStructure() {
    }

    public ResponseStructure(int result) {
        resultProp.setValue(result);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(resultProp.toByteArray());
        if (dataProp != null) {
            buffer.write(dataProp.toByteArray());
        }
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        resultProp.readBuffer(buffer);
        if (dataProp != null) {
            dataProp.readBuffer(buffer);
        }
        return buffer;
    }

    @Override
    public String toString() {
        return "ResponseStructure{" +
                "result=" + resultProp +
                ", data=" + dataProp +
                '}';
    }

    public boolean isSuccess() {
        return resultProp.getValue() == 0;
    }

    public boolean isGeneralError() {
        return resultProp.getValue() > 0 && resultProp.getValue() < 200;
    }

    public ResponseStructure<T> readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return this;
    }
}
