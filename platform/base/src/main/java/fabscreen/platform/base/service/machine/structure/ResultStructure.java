package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class ResultStructure implements IStructure {
    // Fixme: the default - 1
    private UInt8Prop statueProp = new UInt8Prop(0);
    private StringProp messageProp = new StringProp("");


    public ResultStructure(int statue, String message) {
        statueProp.setValue(statue);
        messageProp.setValue(message);
    }

    public ResultStructure() {
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(statueProp.toByteArray());
        buffer.write(messageProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        statueProp.readBuffer(buffer);
        messageProp.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "ResultStructure{" +
                "\nstatueProp=" + statueProp +
                ", \nmessageProp=" + messageProp +
                '}';
    }

    public boolean isSuccess() {
        return statueProp.getValue() == 0;
    }

    public int getStatus() {
        return statueProp.getValue();
    }

    public void setStatus(int statue) {
        statueProp.setValue(statue);
    }

    public String getMessage() {
        return messageProp.getValue();
    }

    public void setMessage(String message) {
        messageProp.setValue(message);
    }
}
