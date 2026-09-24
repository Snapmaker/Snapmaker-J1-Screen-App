package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class CncWorkStateStructure implements IStructure {
    private UInt8Prop keyProp = new UInt8Prop();
    private UInt8Prop spindleStatueProp = new UInt8Prop();
    private UInt8Prop currentPowerProp = new UInt8Prop();
    private UInt8Prop targetPowerProp = new UInt8Prop();
    private UInt32Prop currentSpeedProp = new UInt32Prop();
    private UInt32Prop targetSpeedProp = new UInt32Prop();
    private UInt8Prop controlModeProp = new UInt8Prop();

    public CncWorkStateStructure() {
    }

    public CncWorkStateStructure(int key, int spindleStatue, int currentPower, int targetPower, long currentSpeed, long targetSpeed) {
        keyProp.setValue(key);
        spindleStatueProp.setValue(spindleStatue);
        currentPowerProp.setValue(currentPower);
        targetPowerProp.setValue(targetPower);
        currentSpeedProp.setValue(currentSpeed);
        targetSpeedProp.setValue(targetSpeed);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(keyProp.toByteArray());
        buffer.write(spindleStatueProp.toByteArray());
        buffer.write(currentPowerProp.toByteArray());
        buffer.write(targetPowerProp.toByteArray());
        buffer.write(currentSpeedProp.toByteArray());
        buffer.write(targetSpeedProp.toByteArray());
        buffer.write(controlModeProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        keyProp.readBuffer(buffer);
        spindleStatueProp.readBuffer(buffer);
        currentPowerProp.readBuffer(buffer);
        targetPowerProp.readBuffer(buffer);
        currentSpeedProp.readBuffer(buffer);
        targetSpeedProp.readBuffer(buffer);
        controlModeProp.readBuffer(buffer);
        return buffer;
    }

    public int getSpindleStatue() {
        return spindleStatueProp.getValue();
    }

    public void setSpindleStatue(int spindleStatue) {
        spindleStatueProp.setValue(spindleStatue);
    }

    public int getCurrentPower() {
        return currentPowerProp.getValue();
    }

    public void setCurrentPower(int currentPower) {
        currentPowerProp.setValue(currentPower);
    }

    public int getTargetPower() {
        return targetPowerProp.getValue();
    }

    public void setTargetPower(int targetPower) {
        targetPowerProp.setValue(targetPower);
    }

    public long getCurrentSpeed() {
        return currentSpeedProp.getValue();
    }

    public void setCurrentSpeed(long currentSpeed) {
        currentSpeedProp.setValue(currentSpeed);
    }

    public long getTargetSpeed() {
        return targetSpeedProp.getValue();
    }

    public void setTargetSpeed(long targetSpeed) {
        targetSpeedProp.setValue(targetSpeed);
    }

    public int getKey() {
        return keyProp.getValue();
    }

    public void setKeyProp(int key) {
        keyProp.setValue(key);
    }

    @Override
    public String toString() {
        return "CncWorkStateStructure{" +
                "keyProp=" + keyProp +
                ", spindleStatueProp=" + spindleStatueProp +
                ", currentPowerProp=" + currentPowerProp +
                ", targetPowerProp=" + targetPowerProp +
                ", currentSpeedProp=" + currentSpeedProp +
                ", targetSpeedProp=" + targetSpeedProp +
                ", controlModeProp=" + controlModeProp +
                '}';
    }
}
