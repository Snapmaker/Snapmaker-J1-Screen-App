package fabscreen.platform.base.service.machine.entity.parts;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.Int16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class DryBoxStatus implements IStructure {
    private final UInt8Prop dryStateProp = new UInt8Prop();
    private final Int16Prop tempCurrentChamberProp = new Int16Prop();
    private final Int16Prop tempTargetChamberProp = new Int16Prop();
    private final Int16Prop tempWindHoleProp = new Int16Prop();
    private final UInt16Prop currentHumidityProp = new UInt16Prop();
    private final UInt16Prop targetHumidityProp = new UInt16Prop();
    private final UInt32Prop residualHeatingTimeProp = new UInt32Prop();
    private final UInt32Prop targetHeatingTimeProp = new UInt32Prop();
    private final UInt32Prop cumulativeHeatingTimeProp = new UInt32Prop();
    private final UInt8Prop lidStateProp = new UInt8Prop();
    private final UInt8Prop heaterBlockStateProp = new UInt8Prop();

    public DryBoxStatus() {
    }

    public DryBoxStatus(int dryState,
                        int tempCurrentChamber,
                        int tempTargetChamber,
                        int tempWindHole,
                        int currentHumidity,
                        int targetHumidity,
                        long residualHeatingTime,
                        long targetHeatingTime,
                        long cumulativeHeatingTime,
                        int lidState,
                        int heaterBlockState
    ) {
        dryStateProp.setValue(dryState);
        tempCurrentChamberProp.setValue(tempCurrentChamber);
        tempTargetChamberProp.setValue(tempTargetChamber);
        tempWindHoleProp.setValue(tempWindHole);
        currentHumidityProp.setValue(currentHumidity);
        targetHumidityProp.setValue(targetHumidity);
        residualHeatingTimeProp.setValue(residualHeatingTime);
        targetHeatingTimeProp.setValue(targetHeatingTime);
        cumulativeHeatingTimeProp.setValue(cumulativeHeatingTime);
        lidStateProp.setValue(lidState);
        heaterBlockStateProp.setValue(heaterBlockState);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(dryStateProp.toByteArray());
        buffer.write(tempCurrentChamberProp.toByteArray());
        buffer.write(tempTargetChamberProp.toByteArray());
        buffer.write(tempWindHoleProp.toByteArray());
        buffer.write(currentHumidityProp.toByteArray());
        buffer.write(targetHumidityProp.toByteArray());
        buffer.write(residualHeatingTimeProp.toByteArray());
        buffer.write(targetHeatingTimeProp.toByteArray());
        buffer.write(cumulativeHeatingTimeProp.toByteArray());
        buffer.write(lidStateProp.toByteArray());
        buffer.write(heaterBlockStateProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        dryStateProp.readBuffer(buffer);
        tempCurrentChamberProp.readBuffer(buffer);
        tempTargetChamberProp.readBuffer(buffer);
        tempWindHoleProp.readBuffer(buffer);
        currentHumidityProp.readBuffer(buffer);
        targetHumidityProp.readBuffer(buffer);
        residualHeatingTimeProp.readBuffer(buffer);
        targetHeatingTimeProp.readBuffer(buffer);
        cumulativeHeatingTimeProp.readBuffer(buffer);
        lidStateProp.readBuffer(buffer);
        heaterBlockStateProp.readBuffer(buffer);
        return buffer;
    }

    public float getTempCurrentChamber() {
        return tempCurrentChamberProp.getValue();
    }

    public void setTempCurrentChamber(int tempCurrentChamber) {
        tempCurrentChamberProp.setValue(tempCurrentChamber);
    }

    public int getDryState() {
        return dryStateProp.getValue();
    }

    public void setDryState(int dryState) {
        dryStateProp.setValue(dryState);
    }


    public int getTempTargetChamber() {
        return tempTargetChamberProp.getValue();
    }

    public void setTempTargetChamber(int tempTargetChamber) {
        tempTargetChamberProp.setValue(tempTargetChamber);
    }

    public int getCurrentHumidity() {
        return currentHumidityProp.getValue();
    }

    public void setCurrentHumidity(int currentHumidity) {
        currentHumidityProp.setValue(currentHumidity);
    }

    public int getTargetHumidity() {
        return targetHumidityProp.getValue();
    }

    public void setTargetHumidity(int targetHumidity) {
        targetHumidityProp.setValue(targetHumidity);
    }

    public long getCumulativeHeatingTime() {
        return cumulativeHeatingTimeProp.getValue();
    }

    public void setCumulativeHeatingTime(long heatingTime) {
        cumulativeHeatingTimeProp.setValue(heatingTime);
    }

    public int getLidState() {
        return lidStateProp.getValue();
    }

    public void setLidState(int lidState) {
        lidStateProp.setValue(lidState);
    }

    public int getTempWindHole() {
        return tempWindHoleProp.getValue();
    }

    public void setTempWindHole(int tempWindHole) {
        tempWindHoleProp.setValue(tempWindHole);
    }

    public long getResidualHeatingTime() {
        return residualHeatingTimeProp.getValue();
    }

    public void setResidualHeatingTime(long residualHeatingTime) {
        residualHeatingTimeProp.setValue(residualHeatingTime);
    }

    public long getTargetHeatingTime() {
        return targetHeatingTimeProp.getValue();
    }

    public void setTargetHeatingTime(long targetHeatingTime) {
        residualHeatingTimeProp.setValue(targetHeatingTime);
    }

    public int getHeaterBlockState() {
        return heaterBlockStateProp.getValue();
    }

    public void setHeaterBlockState(int heaterBlockState) {
        heaterBlockStateProp.setValue(heaterBlockState);
    }

    @Override
    public String toString() {
        return "DryBoxStatus{" +
                "dryStat=" + dryStateProp.getValue() +
                ", tempCurrentChamber=" + tempCurrentChamberProp.getValue() +
                ", tempTargetChamber=" + tempTargetChamberProp.getValue() +
                ", tempWindHole=" + tempWindHoleProp.getValue() +
                ", currentHumidity=" + currentHumidityProp.getValue() +
                ", targetHumidity=" + targetHumidityProp.getValue() +
                ", residualHeatingTime=" + residualHeatingTimeProp.getValue() +
                ", targetHeatingTime=" + targetHeatingTimeProp.getValue() +
                ", cumulativeHeatingTime=" + cumulativeHeatingTimeProp.getValue() +
                ", lidState=" + lidStateProp.getValue() +
                ", heaterBlockState=" + heaterBlockStateProp.getValue() +
                '}';
    }
}
