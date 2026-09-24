package fabscreen.platform.base.service.machine.structure.update;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import okio.Buffer;

public class RequestPackageParam implements IStructure {
    private final UInt32Prop byteIndexProp = new UInt32Prop();
    private final UInt16Prop maxBufSpaceProp = new UInt16Prop();


    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(byteIndexProp.toByteArray());
        buffer.write(maxBufSpaceProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        byteIndexProp.readBuffer(buffer);
        maxBufSpaceProp.readBuffer(buffer);
        return buffer;
    }

    public long getIndex() {
        return byteIndexProp.getValue();
    }

    public int getMaxSpace() {
        return maxBufSpaceProp.getValue();
    }
}
