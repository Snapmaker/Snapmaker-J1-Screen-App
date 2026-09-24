package fabscreen.platform.base.service.machine.entity.parts;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class LinearLimit implements IStructure {
    private final UInt8Prop index = new UInt8Prop();
    private final BoolProp trigger = new BoolProp();

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(index.toByteArray());
        buffer.write(trigger.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        index.readBuffer(buffer);
        trigger.readBuffer(buffer);
        return buffer;
    }

    public int getIndex() {
        return index.getValue();
    }

    public boolean getTrigger() {
        return trigger.getValue();
    }

    @Override
    public String toString() {
        return "LinearLimit{" +
                "index=" + index.getValue() +
                ", trigger=" + trigger.getValue() +
                '}';
    }
}
