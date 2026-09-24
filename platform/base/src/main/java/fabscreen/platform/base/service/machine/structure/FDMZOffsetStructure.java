package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class FDMZOffsetStructure implements IStructure {
    private UInt8Prop keyProp = new UInt8Prop();
    private ArrayProp<ZOffsetInfo> zOffsetInfoList = new ArrayProp<>(new ZOffsetInfo());

    public FDMZOffsetStructure() {

    }

    public FDMZOffsetStructure(int key, List<ZOffsetInfo> zOffsetInfoList) {
        keyProp.setValue(key);
        this.zOffsetInfoList.setValue(zOffsetInfoList);
    }


    public int getKey() {
        return keyProp.getValue();
    }

    public List<ZOffsetInfo> getZOffsetInfoList() {
        return zOffsetInfoList.getValue();
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(keyProp.toByteArray());
        buffer.write(zOffsetInfoList.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        keyProp.readBufferToValue(buffer);
        zOffsetInfoList.readBufferToValue(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "FDMZOffsetStructure{" +
                "keyProp=" + keyProp +
                ", zOffsetInfoList=" + zOffsetInfoList +
                '}';
    }
}
