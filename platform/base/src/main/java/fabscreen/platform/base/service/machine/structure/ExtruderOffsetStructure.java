package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class ExtruderOffsetStructure implements IStructure {
    private UInt8Prop extruderIndexProp = new UInt8Prop();
    private UInt8Prop directionProp = new UInt8Prop();
    private FloatProp distanceProp = new FloatProp();

    public ExtruderOffsetStructure() {
    }

    public ExtruderOffsetStructure(int extruderIndex, int direction, float distance) {
        extruderIndexProp.setValue(extruderIndex);
        directionProp.setValue(direction);
        distanceProp.setValue(distance);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(extruderIndexProp.toByteArray());
        buffer.write(directionProp.toByteArray());
        buffer.write(distanceProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        extruderIndexProp.readBuffer(buffer);
        directionProp.readBuffer(buffer);
        distanceProp.readBuffer(buffer);
        return buffer;
    }

    public int getExtruderIndex() {
        return extruderIndexProp.getValue();
    }

    public void setExtruderIndex(int extruderIndex) {
        extruderIndexProp.setValue(extruderIndex);
    }

    public int getDirection() {
        return directionProp.getValue();
    }

    public void setDirection(int direction) {
        directionProp.setValue(direction);
    }

    public float getDistance() {
        return distanceProp.getValue();
    }

    public void setDistance(float distance) {
        distanceProp.setValue(distance);
    }
}
