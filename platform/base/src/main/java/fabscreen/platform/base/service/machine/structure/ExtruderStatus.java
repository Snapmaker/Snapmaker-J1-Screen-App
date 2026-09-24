package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class ExtruderStatus implements IStructure {
    public UInt8Prop toolheadIdProp = new UInt8Prop();
    public ArrayProp<Extruder> extruderListProp = new ArrayProp<>();

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(toolheadIdProp.toByteArray());
        buffer.write(extruderListProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        toolheadIdProp.readBuffer(buffer);
        extruderListProp.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "ExtruderStatus{" +
                "\ntoolheadIdProp=" + toolheadIdProp +
                ",\n extruderListProp=" + extruderListProp +
                '}';
    }

    public int getToolheadId() {
        return toolheadIdProp.getValue();
    }

    public List<Extruder> getExtruderList() {
        return extruderListProp.getValue();
    }
}
