package fabscreen.platform.base.service.machine;

import java.io.IOException;

import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class MachineBehaviorState implements IStructure {
    private UInt8Prop machineBehaviorProp = new UInt8Prop();
    private BoolProp stateProp = new BoolProp();

    public MachineBehaviorState() {
    }

    public MachineBehaviorState(int machineBehavior, boolean state) {
        machineBehaviorProp.setValue(machineBehavior);
        stateProp.setValue(state);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(machineBehaviorProp.toByteArray());
        buffer.write(stateProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        machineBehaviorProp.readBuffer(buffer);
        stateProp.readBuffer(buffer);
        return buffer;
    }

    public int getMachineBehavior() {
        return machineBehaviorProp.getValue();
    }

    public void setMachineBehavior(int machineBehavior) {
        machineBehaviorProp.setValue(machineBehavior);
    }

    public boolean getState() {
        return stateProp.getValue();
    }

    public void setState(boolean state) {
        stateProp.setValue(state);
    }
}
