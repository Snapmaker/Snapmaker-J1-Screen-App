package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class OpenDoorDetectionState implements IStructure {
    private UInt8Prop workTypeProp = new UInt8Prop();
    private BoolProp stateProp = new BoolProp();

    public OpenDoorDetectionState() {
    }

    public OpenDoorDetectionState(int workType, boolean state) {
        this.workTypeProp.setValue(workType);
        this.stateProp.setValue(state);
    }


    public int getWorkType() {
        return workTypeProp.getValue();
    }

    public void setWorkType(int workType) {
        this.workTypeProp.setValue(workType);
    }

    public boolean getState() {
        return stateProp.getValue();
    }

    public void setState(boolean state) {
        this.stateProp.setValue(state);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(workTypeProp.toByteArray());
        buffer.write(stateProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        workTypeProp.readBuffer(buffer);
        stateProp.readBuffer(buffer);
        return buffer;
    }
}
