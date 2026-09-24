package fabscreen.platform.base.service.machine.entity.parts;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class Fan implements IStructure {
    private UInt8Prop indexProp = new UInt8Prop();
    private UInt8Prop typeProp = new UInt8Prop();
    private UInt8Prop speedLevelProp = new UInt8Prop();

    public Fan() {
    }

    public Fan(int index, int type, int speedLevel) {
        indexProp.setValue(index);
        typeProp.setValue(type);
        speedLevelProp.setValue(speedLevel);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(indexProp.toByteArray());
        buffer.write(typeProp.toByteArray());
        buffer.write(speedLevelProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        indexProp.readBuffer(buffer);
        typeProp.readBuffer(buffer);
        speedLevelProp.readBuffer(buffer);
        return buffer;
    }

    public int getId() {
        return indexProp.getValue();
    }

    public void setId(int id) {
        indexProp.setValue(id);
    }

    public int getType() {
        return typeProp.getValue();
    }

    public void setType(int type) {
        typeProp.setValue(type);
    }

    public int getSpeedLevel() {
        return speedLevelProp.getValue();
    }

    public void setSpeedLevel(int speedLevel) {
        speedLevelProp.setValue(speedLevel);
    }

    @Override
    public String toString() {
        return "Fan{" +
                "\nid=" + indexProp +
                ",\n type=" + typeProp +
                ",\n speedLevel=" + speedLevelProp +
                '}';
    }
}
