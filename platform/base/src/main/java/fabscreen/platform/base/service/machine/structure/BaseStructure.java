package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.BasicProp;
import okio.Buffer;

/**
 * Base Structure can add props to generate a Structure
 * extended class may disable addProp to keep safe
 */
public abstract class BaseStructure implements IStructure {

    // Don't use mPropMap.entrySet().forEach(...), it may break the order. Need further investigation.
    // https://stackoverflow.com/questions/42711433/linkedhashmap-entrysets-order-not-being-preserved-in-a-stream-android/42714165
    protected Map<String, IStructure> mProps = new LinkedHashMap<>();

    public BaseStructure() {
        init();
    }

    protected abstract void init();

    @Override
    public byte[] toByteArray() {
        // iter the map and set
        Buffer buffer = new Buffer();
        mProps.forEach((s, iStructure) -> buffer.write(iStructure.toByteArray()));
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        for (Map.Entry<String, IStructure> entry : mProps.entrySet()) {
            entry.getValue().readBuffer(buffer);
        }
        return buffer;
    }

    protected void addProp(String key, IStructure prop) {
        // add a prop to mProps
        mProps.put(key, prop);
    }

    public BasicProp getProp(String key) {
        return (BasicProp) mProps.get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        mProps.forEach((s, struct) -> {
            sb.append(s).append(":").append(struct.toString()).append("\n");
        });
        return sb.toString();
    }
}
