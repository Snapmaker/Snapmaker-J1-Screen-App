package fabscreen.platform.base.service.machine.structure;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class CoordinateSystemInfo implements IStructure {
    private UInt8Prop homedProp = new UInt8Prop();
    private UInt8Prop coordinateSystemId = new UInt8Prop();
    private BoolProp offsetAligned = new BoolProp();
    private ArrayProp<CoordinateStructure> coordinates = new ArrayProp<>(new CoordinateStructure());
    private ArrayProp<CoordinateStructure> originOffsets = new ArrayProp<>(new CoordinateStructure());

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(homedProp.toByteArray());
        buffer.write(coordinateSystemId.toByteArray());
        buffer.write(offsetAligned.toByteArray());
        buffer.write(coordinates.toByteArray());
        buffer.write(originOffsets.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        homedProp.readBuffer(buffer);
        coordinateSystemId.readBuffer(buffer);
        offsetAligned.readBuffer(buffer);
        coordinates.readBuffer(buffer);
        originOffsets.readBuffer(buffer);
        return buffer;
    }

    public boolean getHomed() {
        return homedProp.getValue() == 0;
    }

    public int getCoordinateSystemId() {
        return coordinateSystemId.getValue();
    }

    public boolean getOffsetAligned() {
        return offsetAligned.getValue();
    }

    public List<CoordinateStructure> getCoordinates() {
        return coordinates.getValue();
    }

    public List<CoordinateStructure> getOffsets() {
        return originOffsets.getValue();
    }

    @NonNull
    @Override
    public String toString() {
        return "CoordinateSystemInfo{" +
                "homedProp=" + homedProp +
                ", coordinateSystemId=" + coordinateSystemId +
                ", offsetAligned=" + offsetAligned +
                ", coordinates=" + coordinates +
                ", originOffsets=" + originOffsets +
                '}';
    }
}
