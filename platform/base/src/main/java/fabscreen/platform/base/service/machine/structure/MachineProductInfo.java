package fabscreen.platform.base.service.machine.structure;

import java.io.IOException;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import okio.Buffer;

public class MachineProductInfo implements IStructure {
    private int mMachineSeries = IMachine.MachineSeries.UNDEFINED;

    private UInt8Prop modelProp = new UInt8Prop();
    private UInt8Prop controllerHWVersionProp = new UInt8Prop();
    private UInt32Prop snProp = new UInt32Prop();
    private StringProp controllerFWVersionProp = new StringProp();

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(modelProp.toByteArray());
        buffer.write(controllerHWVersionProp.toByteArray());
        buffer.write(snProp.toByteArray());
        buffer.write(controllerFWVersionProp.toByteArray());
        return buffer.readByteArray();
    }

    @Override
    public Buffer readBuffer(Buffer buffer) throws IOException {
        modelProp.readBuffer(buffer);
        controllerHWVersionProp.readBuffer(buffer);
        snProp.readBuffer(buffer);
        controllerFWVersionProp.readBuffer(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "MachineProductInfo{" +
                "\nmodelProp=" + modelProp +
                ",\n controllerHWVersionProp=" + controllerHWVersionProp +
                ",\n snProp=" + snProp +
                ",\n controllerFWVersionProp=" + controllerFWVersionProp +
                '}';
    }

    public int getBrand() {
        int brand = IMachine.MachineSeries.UNDEFINED;
        switch (modelProp.getValue()) {
            case 0:
            case 1:
            case 2:
            case 3:
                return IMachine.MachineSeries.A;
            case 4:
                return IMachine.MachineSeries.J;
        }
        return brand;
    }

    public void setSeries(int brand) {
        mMachineSeries = brand;
    }

    public void setModel(int model) {
        int fullModel = -1;
        switch (mMachineSeries) {
            case IMachine.MachineSeries.A:
                switch (model) {
                    case IMachine.MachineModel.A150:
                        fullModel = 0;
                        break;
                    case IMachine.MachineModel.A250:
                        fullModel = 1;
                        break;
                    case IMachine.MachineModel.A350:
                        fullModel = 2;
                        break;
                    case IMachine.MachineModel.A400:
                        fullModel = 3;
                }
                break;

            case IMachine.MachineSeries.J:
                fullModel = 4;
                break;
        }

        modelProp.setValue(fullModel);
    }

    public int getModel() {
        switch (modelProp.getValue()) {
            case 0:
                return IMachine.MachineModel.A150;
            case 1:
                return IMachine.MachineModel.A250;
            case 2:
                return IMachine.MachineModel.A350;
            case 3:
                return IMachine.MachineModel.A400;
            case 4:
                return IMachine.MachineModel.J1;
            default:
                return IMachine.MachineModel.UNDEFINED;
        }
    }

    public int getProductId() {
        return modelProp.getValue();
    }

    public String getControllerFWVersion() {
        return controllerFWVersionProp.getValue();
    }

    public void setControllerFWVersion(String controllerVersion) {
        controllerFWVersionProp.setValue(controllerVersion);
    }

    public String getSn() {
        return snProp.getValue().toString();
    }

    public void setSn(String sn) {
        snProp.setValue(Long.valueOf(sn));
    }
}
