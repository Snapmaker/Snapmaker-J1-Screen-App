package fabscreen.platform.base.service.machine.structure.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.lib.LogHelper;
import okio.Buffer;


public class ArrayProp<T extends IStructure> extends BasicProp<List<T>> {

    public ArrayProp(List<T> mValue) {
        this.mValue = mValue;
    }

    public ArrayProp(T element) {
        mValue = new ArrayList<>();
        mValue.add(element);
    }

    public ArrayProp() {
        mValue = new ArrayList<>();
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.writeByte(mValue.size());
        for (T mValue : mValue) {
            buffer.write(mValue.toByteArray());
        }
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        // TODO: 2022/1/21  need better implement
        try {
            Class<? extends IStructure> aClass = mValue.get(0).getClass();
            List<T> tempValue = new ArrayList<>();
            int len = buffer.readByte();
            for (int i = 0; i < len; i++) {
                IStructure t = aClass.newInstance();
                t.readBuffer(buffer);
//                Logger.d("sacp-debug: t at %1$d is %2$s", i, ByteString.of(t.toByteArray()).hex());
                tempValue.add((T) t);
            }
            mValue = tempValue;
        } catch (IllegalAccessException | InstantiationException e) {
            LogHelper.log(e);
        }
        return buffer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T value : mValue) {
            sb.append(value.toString()).append("\n");
        }
        return sb.toString();
    }

    public void addElement(T value) {
        mValue.add(value);
    }

    @Override
    public void setValue(List<T> values) {
        mValue.clear();
        mValue.addAll(values);
    }

    @Override
    public List<T> readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return mValue;
    }
}
