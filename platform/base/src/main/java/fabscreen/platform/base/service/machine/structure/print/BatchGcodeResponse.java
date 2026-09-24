package fabscreen.platform.base.service.machine.structure.print;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import okio.Buffer;

public class BatchGcodeResponse implements IStructure {
    private UInt32Prop startLineNo;
    private UInt32Prop endLineNo;
    private StringProp batchGcode;

    public BatchGcodeResponse() {
        this(new UInt32Prop(0), new UInt32Prop(0), new StringProp(""));
    }

    public BatchGcodeResponse(UInt32Prop startLineNo, UInt32Prop endLineNo, StringProp batchGcode) {
        this.startLineNo = startLineNo;
        this.endLineNo = endLineNo;
        this.batchGcode = batchGcode;
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(startLineNo.toByteArray());
        buffer.write(endLineNo.toByteArray());
        buffer.write(batchGcode.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        startLineNo.readBuffer(buffer);
        endLineNo.readBuffer(buffer);
        batchGcode.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "BatchGcodeResponse{" +
                "\nstartLineNo=" + startLineNo +
                ",\n endLineNo=" + endLineNo +
                ",\n batchGcode=" + batchGcode +
                '}';
    }

    public long getStartLineNo() {
        return startLineNo.getValue();
    }

    public long getEndLineNo() {
        return endLineNo.getValue();
    }

    public String getBatchGcode() {
        return batchGcode.getValue();
    }

    public void setStartLineNo(long startLineNo) {
        this.startLineNo = new UInt32Prop(startLineNo);
    }

    public void setEndLineNo(long endLineNo) {
        this.endLineNo = new UInt32Prop(endLineNo);
    }

    public void setBatchGcode(String batchGcode) {
        this.batchGcode = new StringProp(batchGcode);
    }
}
