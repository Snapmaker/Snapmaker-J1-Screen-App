package fabscreen.platform.base.service.machine.entity.parts;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class LaserTube implements IStructure {
    UInt8Prop keyProp = new UInt8Prop();
    FloatProp currentPowerProp = new FloatProp();
    FloatProp targetPowerProp = new FloatProp();

    public LaserTube() {
    }

    public float getCurrentPower() {
        return currentPowerProp.getValue();
    }

    public void setCurrentPower(float currentPower) {
        currentPowerProp.setValue(currentPower);
    }

    public float getTargetPower() {
        return targetPowerProp.getValue();
    }

    public void setTargetPower(float targetPower) {
        targetPowerProp.setValue(targetPower);
    }

    public int getKey() {
        return keyProp.getValue();
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(keyProp.toByteArray());
        buffer.write(currentPowerProp.toByteArray());
        buffer.write(targetPowerProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        keyProp.readBuffer(buffer);
        currentPowerProp.readBuffer(buffer);
        targetPowerProp.readBuffer(buffer);
        return buffer;
    }
}
