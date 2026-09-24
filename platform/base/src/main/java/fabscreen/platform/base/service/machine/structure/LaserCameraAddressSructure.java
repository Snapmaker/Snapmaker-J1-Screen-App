package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class LaserCameraAddressSructure implements IStructure {
    public UInt8Prop keyProp = new UInt8Prop();
    public UInt8Prop statusProp = new UInt8Prop();
    public StringProp macAddressProp = new StringProp();

    public LaserCameraAddressSructure() {
    }

    public LaserCameraAddressSructure(int key, int status, String macAddress) {
        keyProp.setValue(key);
        statusProp.setValue(status);
        macAddressProp.setValue(macAddress);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(keyProp.toByteArray());
        buffer.write(statusProp.toByteArray());
        buffer.write(macAddressProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        keyProp.readBuffer(buffer);
        statusProp.readBuffer(buffer);
        macAddressProp.readBuffer(buffer);
        return buffer;
    }

    public int getKey() {
        return keyProp.getValue();
    }

    public int getStatus() {
        return statusProp.getValue();
    }

    public String getMacAddress() {
        return macAddressProp.getValue();
    }

}
