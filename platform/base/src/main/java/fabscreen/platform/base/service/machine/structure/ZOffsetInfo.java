package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class ZOffsetInfo implements IStructure {
    private UInt8Prop index;
    private FloatProp zOffset;

    public ZOffsetInfo() {
        index = new UInt8Prop();
        zOffset = new FloatProp();

    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(index.toByteArray());
        buffer.write(zOffset.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        index.readBufferToValue(buffer);
        zOffset.readBufferToValue(buffer);
        return buffer;
    }

    public int getIndex() {
        return index.getValue();
    }

    public void setIndex(int i) {
        index.setValue(i);
    }

    public void setZOffset(float zOffset) {
        this.zOffset.setValue(zOffset);
    }

    public float getZOffset() {
        return zOffset.getValue();
    }

    @Override
    public String toString() {
        return "ZOffsetInfo{" +
                "index=" + index +
                ", zOffset=" + zOffset +
                '}';
    }
}
