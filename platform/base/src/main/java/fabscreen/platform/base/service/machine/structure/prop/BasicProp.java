package fabscreen.platform.base.service.machine.structure.prop;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import okio.Buffer;

public abstract class BasicProp<T> implements IStructure {
    protected T mValue;

    public BasicProp() {
    }

    public T getValue() {
        return mValue;
    }

    public void setValue(T value) {
        mValue = value;
    }

    public abstract T readBufferToValue(Buffer buffer) throws IOException;
}
