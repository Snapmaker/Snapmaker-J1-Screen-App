package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class MachineFault implements IStructure {
    private UInt8Prop levelProp = new UInt8Prop();
    private UInt16Prop ownerProp = new UInt16Prop();
    private UInt8Prop valueProp = new UInt8Prop();

    public MachineFault() {
    }

    public MachineFault(int level, int owner, int value) {
        levelProp.setValue(level);
        ownerProp.setValue(owner);
        valueProp.setValue(value);

    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(levelProp.toByteArray());
        buffer.write(ownerProp.toByteArray());
        buffer.write(valueProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        levelProp.readBuffer(buffer);
        ownerProp.readBuffer(buffer);
        valueProp.readBuffer(buffer);
        return buffer;
    }

    public int getLevel() {
        return levelProp.getValue();
    }

    public void setLevel(int level) {
        levelProp.setValue(level);
    }

    public int getOwner() {
        return ownerProp.getValue();
    }

    public void setOwner(int owner) {
        ownerProp.setValue(owner);
    }

    public int getValue() {
        return valueProp.getValue();
    }

    public void setValueProp(int value) {
        valueProp.setValue(value);
    }

    public MachineFault readBufferToValue(Buffer buffer) throws IOException {
        readBuffer(buffer);
        return this;
    }

    @Override
    public String toString() {
        return "MachineFault{" +
                "level=" + levelProp.getValue() +
                ", owner=" + ownerProp.getValue() +
                ", value=" + valueProp.getValue() +
                '}';
    }

    public boolean isJ1() {
        return ownerProp.getValue().equals(Module.ModuleType.J1_CONTROL);
    }

    public boolean isEmergencyStop() {
        return ownerProp.getValue().equals(Module.ModuleType.EMERGENCY_BUTTON_A400);
    }
}
