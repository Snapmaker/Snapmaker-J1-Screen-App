package fabscreen.platform.base.service.machine.entity.parts;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class Motor implements IStructure {
    private UInt8Prop axisProp = new UInt8Prop();
    private BoolProp stateProp = new BoolProp();

    public Motor() {

    }

    public Motor(int axis, boolean state) {
        axisProp.setValue(axis);
        stateProp.setValue(state);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(axisProp.toByteArray());
        buffer.write(stateProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        axisProp.readBuffer(buffer);
        stateProp.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "Motor{" +
                "axisProp=" + axisProp +
                ", stateProp=" + stateProp +
                '}';
    }

    public void setAxis(int axis) {
        axisProp.setValue(axis);
    }

    public int getAxis() {
        return axisProp.getValue();
    }

    public void setState(boolean on) {
        stateProp.setValue(on);
    }

    public boolean getState() {
        return stateProp.getValue();
    }
}
