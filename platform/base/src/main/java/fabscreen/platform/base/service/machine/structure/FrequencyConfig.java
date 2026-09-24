package fabscreen.platform.base.service.machine.structure;

import androidx.annotation.NonNull;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class FrequencyConfig implements IStructure {
    private final UInt8Prop typeProp = new UInt8Prop();
    private final FloatProp frequencyProp = new FloatProp();

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(typeProp.toByteArray());
        buffer.write(frequencyProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        typeProp.readBuffer(buffer);
        frequencyProp.readBuffer(buffer);
        return buffer;
    }

    public int getType() {
        return typeProp.getValue();
    }

    public float getFrequency() {
        return frequencyProp.getValue();
    }

    @NonNull
    @Override
    public String toString() {
        return "VibrationCompensationConfig{" +
                "typeProp=" + typeProp +
                ", frequencyProp=" + frequencyProp +
                '}';
    }
}
