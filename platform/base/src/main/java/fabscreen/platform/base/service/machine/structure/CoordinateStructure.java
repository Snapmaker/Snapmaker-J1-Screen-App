package fabscreen.platform.base.service.machine.structure;

import java.io.EOFException;
import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class CoordinateStructure implements IStructure {
    // X/Y/Z...
    protected final UInt8Prop axisProp = new UInt8Prop();
    // A pos/neg value represent distance and direction.
    protected final FloatProp vectorProp = new FloatProp();


    public CoordinateStructure() {
    }

    public CoordinateStructure(int axis, float distance) {
        axisProp.setValue(axis);
        vectorProp.setValue(distance);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(axisProp.toByteArray());
        buffer.write(vectorProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        axisProp.readBuffer(buffer);
        vectorProp.readBuffer(buffer);
        return buffer;
    }

    public int getAxis() {
        return axisProp.getValue();
    }

    public void setAxis(int direction) {
        axisProp.setValue(direction);
    }

    public float getVector() {
        return vectorProp.getValue();
    }

    public void setVector(float coordinate) {
        vectorProp.setValue(coordinate);
    }

    @Override
    public String toString() {
        return "VectorInformation{" +
                "\n directionProp=" + axisProp +
                ",\n vectorProp=" + vectorProp +
                '}';
    }

    public CoordinateStructure readBufferToValue(Buffer buffer) throws EOFException {
        axisProp.readBuffer(buffer);
        vectorProp.readBuffer(buffer);
        return this;
    }
}
