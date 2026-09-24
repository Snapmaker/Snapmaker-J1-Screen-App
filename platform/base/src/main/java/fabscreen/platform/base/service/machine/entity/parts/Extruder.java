package fabscreen.platform.base.service.machine.entity.parts;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class Extruder implements IStructure {
    public static final float EXTRUDER_DIAMETER_0_2 = 0.2f;
    public static final float EXTRUDER_DIAMETER_0_4 = 0.4f;
    public static final float EXTRUDER_DIAMETER_0_6 = 0.6f;
    public static final float EXTRUDER_DIAMETER_0_8 = 0.8f;
    public static final int EXTRUDER_MATERIAL_BRASS_NTC = 0;
    public static final int EXTRUDER_MATERIAL_BRASS_PT100 = 1;
    public static final int EXTRUDER_MATERIAL_HARDENED_STEEL_PT100 = 2;
    private final UInt8Prop indexProp = new UInt8Prop();

    public static final int EXTRUDER_LEFT = 0;
    public static final int EXTRUDER_RIGHT = 1;
    private final BoolProp filamentStatusProp = new BoolProp();
    private final UInt8Prop filamentDetectionStatus = new UInt8Prop();
    private final UInt8Prop stateProp = new UInt8Prop();
    private final UInt8Prop modelProp = new UInt8Prop();
    private final FloatProp diameterProp = new FloatProp();
    private final FloatProp temperatureProp = new FloatProp();
    private final FloatProp targetTemperatureProp = new FloatProp();


    public Extruder() {
    }

    public Extruder(int id, int state, float diameter, int temperature, int targetTemperature) {
        setId(id);
        setState(state);
        setDiameter(diameter);
        setTemperature(temperature);
        setTargetTemperature(targetTemperature);

    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(indexProp.toByteArray());
        buffer.write(filamentStatusProp.toByteArray());
        buffer.write(filamentDetectionStatus.toByteArray());
        buffer.write(stateProp.toByteArray());
        buffer.write(modelProp.toByteArray());
        buffer.write(diameterProp.toByteArray());
        buffer.write(temperatureProp.toByteArray());
        buffer.write(targetTemperatureProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        indexProp.readBuffer(buffer);
        filamentStatusProp.readBuffer(buffer);
        filamentDetectionStatus.readBuffer(buffer);
        stateProp.readBuffer(buffer);
        modelProp.readBuffer(buffer);
        diameterProp.readBuffer(buffer);
        temperatureProp.readBuffer(buffer);
        targetTemperatureProp.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "Extruder{" +
                "index =" + indexProp +
                "\n filamentStatus =" + filamentStatusProp +
                "\n filamentDetection =" + filamentDetectionStatus +
                ",\n state=" + stateProp +
                ",\n model=" + modelProp +
                ",\n diameter=" + diameterProp +
                ",\n temperature=" + temperatureProp +
                ",\n targetTemperature=" + targetTemperatureProp +
                '}';
    }

    public int getId() {
        return indexProp.getValue();
    }

    public void setId(int id) {
        this.indexProp.setValue(id);
    }

    public int getState() {
        return stateProp.getValue();
    }

    public void setState(int state) {
        this.stateProp.setValue(state);
    }

    public float getDiameter() {
        return diameterProp.getValue();
    }

    public void setDiameter(float diameter) {
        this.diameterProp.setValue(diameter);
    }

    public float getTemperature() {
        return temperatureProp.getValue();
    }

    public void setTemperature(float temperature) {
        this.temperatureProp.setValue(temperature);
    }

    public float getTargetTemperature() {
        return targetTemperatureProp.getValue();
    }

    public void setTargetTemperature(float targetTemperature) {
        this.targetTemperatureProp.setValue(targetTemperature);
    }

    public boolean getFilamentStatus() {
        return filamentStatusProp.getValue();
    }

    public void setFilamentStatus(boolean status) {
        this.filamentStatusProp.setValue(status);
    }

    public int getFilamentDetectionStatus() {
        return filamentDetectionStatus.getValue();
    }

    public void setFilamentDetectionStatus(int status) {
        this.filamentDetectionStatus.setValue(status);
    }

    public int getModel() {
        return modelProp.getValue();
    }
}
