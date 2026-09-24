package fabscreen.platform.base.service.machine.structure.print;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import okio.Buffer;

public class BatchGcodeRequest implements IStructure {
    private UInt32Prop requestLineNo;

    public BatchGcodeRequest() {
        this(new UInt32Prop(0));
    }

    public BatchGcodeRequest(UInt32Prop lineNo) {
        requestLineNo = lineNo;
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(requestLineNo.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        requestLineNo.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "BatchGcodeRequest{" +
                "requestLineNo=" + requestLineNo +
                '}';
    }

    public long getRequestLineNo() {
        return requestLineNo.getValue();
    }

    public void setRequestLineNo(long requestLineNo) {
        this.requestLineNo = new UInt32Prop(requestLineNo);
    }
}
