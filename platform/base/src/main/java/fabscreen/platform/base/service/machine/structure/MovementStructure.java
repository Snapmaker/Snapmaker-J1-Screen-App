package fabscreen.platform.base.service.machine.structure;

import java.io.EOFException;
import java.io.IOException;

import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import okio.Buffer;

public class MovementStructure extends CoordinateStructure {
    private final UInt16Prop speedProp = new UInt16Prop();

    /**
     * Movement with default speed.
     */
    public MovementStructure(int direction, float coordinate) {
        // Machine will go with default speed while set 0. @xiehongyan
        this(direction, coordinate, 0);
    }

    public MovementStructure(int direction, float distance, int speed) {
        axisProp.setValue(direction);
        vectorProp.setValue(distance);
        speedProp.setValue(speed);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(axisProp.toByteArray());
        buffer.write(vectorProp.toByteArray());
        buffer.write(speedProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        axisProp.readBuffer(buffer);
        vectorProp.readBuffer(buffer);
        speedProp.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "MovementStructure{" +
                " \n axisProp=" + axisProp +
                ",\n vectorProp=" + vectorProp +
                ",\n speedProp=" + speedProp +
                '}';
    }

    @Override
    public MovementStructure readBufferToValue(Buffer buffer) throws EOFException {
        axisProp.readBuffer(buffer);
        vectorProp.readBuffer(buffer);
        speedProp.readBuffer(buffer);
        return this;
    }
}
