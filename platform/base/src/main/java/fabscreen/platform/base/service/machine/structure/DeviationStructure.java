package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class DeviationStructure implements IStructure {
    public UInt8Prop mExtruderIndexProp = new UInt8Prop();
    public UInt8Prop mAxisProp = new UInt8Prop();
    public FloatProp mValueProp = new FloatProp();

    public DeviationStructure() {
    }

    public DeviationStructure(int extruder, int axis, float value) {
        mExtruderIndexProp.setValue(extruder);
        mAxisProp.setValue(axis);
        mValueProp.setValue(value);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(mExtruderIndexProp.toByteArray());
        buffer.write(mAxisProp.toByteArray());
        buffer.write(mValueProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        mExtruderIndexProp.readBuffer(buffer);
        mAxisProp.readBuffer(buffer);
        mValueProp.readBuffer(buffer);
        return buffer;
    }

    public int getAxis() {
        return mAxisProp.getValue();
    }

    public void setAxis(int axis) {
        mAxisProp.setValue(axis);
    }

    public float getValue() {
        return mValueProp.getValue();
    }

    public void setValue(float value) {
        mValueProp.setValue(value);
    }

    @Override
    public String toString() {
        return "DeviationStructure{" +
                "ExtruderIndex=" + mExtruderIndexProp.getValue() +
                ", Axis=" + mAxisProp.getValue() +
                ", Value=" + mValueProp.getValue() +
                '}';
    }
}
