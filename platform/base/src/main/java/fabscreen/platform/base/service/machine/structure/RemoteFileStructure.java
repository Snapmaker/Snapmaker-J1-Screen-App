package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import okio.Buffer;

public class RemoteFileStructure implements IStructure {
    StringProp fileNameProp = new StringProp();
    UInt32Prop fileSizeProp = new UInt32Prop();
    UInt16Prop packageNumProp = new UInt16Prop();
    StringProp md5Prop = new StringProp();

    public RemoteFileStructure() {
    }

    public RemoteFileStructure(String fileName, long fileSize, int packageNum, String md5) {
        fileNameProp.setValue(fileName);
        fileSizeProp.setValue(fileSize);
        packageNumProp.setValue(packageNum);
        md5Prop.setValue(md5);
    }

    public String getFileName() {
        return fileNameProp.getValue();
    }

    public void setFileName(String fileName) {
        fileNameProp.setValue(fileName);
    }

    public long getFileLength() {
        return fileSizeProp.getValue();
    }

    public void setFileSize(long fileSize) {
        fileSizeProp.setValue(fileSize);
    }

    public int getPackageNum() {
        return packageNumProp.getValue();
    }

    public void setPackageNum(int packageNum) {
        packageNumProp.setValue(packageNum);
    }

    public String getMd5() {
        return md5Prop.getValue();
    }

    public void setMd5(String md5) {
        md5Prop.setValue(md5);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(fileNameProp.toByteArray());
        buffer.write(fileSizeProp.toByteArray());
        buffer.write(packageNumProp.toByteArray());
        buffer.write(md5Prop.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        fileNameProp.readBuffer(buffer);
        fileSizeProp.readBuffer(buffer);
        packageNumProp.readBuffer(buffer);
        md5Prop.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "RemoteFileStructure{" +
                "fileNameProp=" + fileNameProp +
                ", fileSizeProp=" + fileSizeProp +
                ", packageNumProp=" + packageNumProp +
                ", md5Prop=" + md5Prop +
                '}';
    }
}
