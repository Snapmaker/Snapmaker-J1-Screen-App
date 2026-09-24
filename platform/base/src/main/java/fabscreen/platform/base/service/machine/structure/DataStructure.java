package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BytesProp;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import okio.Buffer;

public class DataStructure implements IStructure {
    StringProp md5Prop = new StringProp();
    UInt16Prop indexProp = new UInt16Prop();
    BytesProp dataProp = new BytesProp();

    public DataStructure() {
    }

    public DataStructure(String md5, byte[] data) {
        md5Prop.setValue(md5);
        dataProp.setValue(data);
    }

    public String getMd5() {
        return md5Prop.getValue();
    }

    public void setMd5(String md5) {
        md5Prop.setValue(md5);
    }

    public void setIndex(int index) {
        indexProp.setValue(index);
    }

    public int getIndex() {
        return indexProp.getValue();
    }

    public byte[] getData() {
        return dataProp.getValue();
    }

    public void setData(byte[] data) {
        dataProp.setValue(data);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(md5Prop.toByteArray());
        buffer.write(indexProp.toByteArray());
        buffer.write(dataProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        md5Prop.readBuffer(buffer);
        indexProp.readBuffer(buffer);
        dataProp.readBuffer(buffer);
        return buffer;
    }
}
