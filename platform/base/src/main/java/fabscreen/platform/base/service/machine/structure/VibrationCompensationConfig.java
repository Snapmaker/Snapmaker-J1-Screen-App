package fabscreen.platform.base.service.machine.structure;

import androidx.annotation.NonNull;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import okio.Buffer;

public class VibrationCompensationConfig implements IStructure {
    private final BoolProp enabledProp = new BoolProp();
    private final FrequencyConfig frequencyConfig = new FrequencyConfig();

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(enabledProp.toByteArray());
        buffer.write(frequencyConfig.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        enabledProp.readBuffer(buffer);
        frequencyConfig.readBuffer(buffer);
        return buffer;
    }

    public boolean getEnabled() {
        return enabledProp.getValue();
    }

    public FrequencyConfig getFrequencyConfig() {
        return frequencyConfig;
    }

    @NonNull
    @Override
    public String toString() {
        return "VibrationCompensationConfig{" +
                "enabledProp=" + enabledProp +
                ", frequencyProp=" + frequencyConfig +
                '}';
    }
}
