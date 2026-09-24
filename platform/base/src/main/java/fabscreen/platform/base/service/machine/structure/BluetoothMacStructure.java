package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class BluetoothMacStructure implements IStructure {
    private UInt8Prop keyProp = new UInt8Prop();
    private UInt8Prop statusProp = new UInt8Prop();
    private ArrayProp<UInt8Prop> macProp = new ArrayProp<>(new UInt8Prop());


    public int getKey() {
        return keyProp.getValue();
    }

    public int getStatus() {
        return statusProp.getValue();
    }

    public String getMac() {
        List<UInt8Prop> uInt8Props = macProp.getValue();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uInt8Props.size(); i++) {
            if (i == uInt8Props.size() - 1)
                sb.append(Integer.toHexString(uInt8Props.get(i).getValue()));
            else
                sb.append(Integer.toHexString(uInt8Props.get(i).getValue())).append(":");
        }
        return sb.toString().toUpperCase();
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(keyProp.toByteArray());
        buffer.write(statusProp.toByteArray());
        buffer.write(macProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        keyProp.readBuffer(buffer);
        statusProp.readBuffer(buffer);
        macProp.readBuffer(buffer);
        return buffer;
    }
}
