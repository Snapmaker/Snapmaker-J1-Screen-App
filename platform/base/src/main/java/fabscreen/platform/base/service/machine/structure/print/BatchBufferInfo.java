package fabscreen.platform.base.service.machine.structure.print;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import okio.Buffer;

public class BatchBufferInfo implements IStructure {
    private final UInt32Prop lineNoProp = new UInt32Prop();
    private final UInt16Prop batchBufferLength = new UInt16Prop();

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(lineNoProp.toByteArray());
        buffer.write(batchBufferLength.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        lineNoProp.readBuffer(buffer);
        batchBufferLength.readBuffer(buffer);
        return buffer;
    }

    public long getLineNo() {
        return lineNoProp.getValue();
    }

    public long getBatchBufferLength() {
        return batchBufferLength.getValue();
    }
}
