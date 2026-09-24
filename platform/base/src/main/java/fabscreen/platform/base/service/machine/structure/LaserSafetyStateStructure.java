package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class LaserSafetyStateStructure implements IStructure {
    UInt8Prop keyProp = new UInt8Prop();
    UInt8Prop stateProp = new UInt8Prop();
    FloatProp tubeTemperatureProp = new FloatProp();
    FloatProp tubeRollAngleProp = new FloatProp();
    FloatProp tubePitchAngleProp = new FloatProp();

    public LaserSafetyStateStructure(int key, int state, float tubeTemperature, float tubeRollAngle, float tubePitchAngle) {
        keyProp.setValue(key);
        stateProp.setValue(state);
        tubeTemperatureProp.setValue(tubeTemperature);
        tubeRollAngleProp.setValue(tubeRollAngle);
        tubePitchAngleProp.setValue(tubePitchAngle);
    }

    public LaserSafetyStateStructure() {
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(keyProp.toByteArray());
        buffer.write(stateProp.toByteArray());
        buffer.write(tubeTemperatureProp.toByteArray());
        buffer.write(tubeRollAngleProp.toByteArray());
        buffer.write(tubePitchAngleProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        keyProp.readBuffer(buffer);
        stateProp.readBuffer(buffer);
        tubeTemperatureProp.readBuffer(buffer);
        tubeRollAngleProp.readBuffer(buffer);
        tubePitchAngleProp.readBuffer(buffer);
        return buffer;
    }

    public int getKey() {
        return keyProp.getValue();
    }

    public void setKey(int key) {
        keyProp.setValue(key);
    }

    public int getState() {
        return stateProp.getValue();
    }

    public void setState(int state) {
        stateProp.setValue(state);
    }

    public float getTubeTemperature() {
        return tubeTemperatureProp.getValue();
    }

    public void setTubeTemperature(float tubeTemperature) {
        tubeTemperatureProp.setValue(tubeTemperature);
    }

    public float getTubeRollAngle() {
        return tubeRollAngleProp.getValue();
    }

    public void setTubeRollAngle(float tubeRollAngle) {
        tubeRollAngleProp.setValue(tubeRollAngle);
    }

    public float getTubePitchAngle() {
        return tubePitchAngleProp.getValue();
    }

    public void setTubePitchAngle(float tubePitchAngle) {
        tubePitchAngleProp.setValue(tubePitchAngle);
    }

    @Override
    public String toString() {
        return "LaserSafetyState {" +
                "key =" + keyProp.getValue() +
                ", state =" + stateProp.getValue() +
                "\ntubeTemperature=" + tubeTemperatureProp.getValue() +
                "\ntubeRollAngle=" + tubeRollAngleProp.getValue() +
                ", tubePitchAngle=" + tubePitchAngleProp.getValue() +
                '}';
    }
}
