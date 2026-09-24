package fabscreen.platform.base.service.machine.entity;

import android.content.Context;

import java.io.IOException;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import io.reactivex.Observable;
import okio.Buffer;

public abstract class Module {
    protected IMachine mMachine;
    protected ModuleInfo mModuleInfo;
    protected MachineConnectionController mConnectionController;

    public Module(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        mModuleInfo = info;
        mConnectionController = cc;
        mMachine = mc;
    }

    protected abstract void init();

    public abstract <T extends IStructure> Observable<ResponseStructure<T>> requestInfo();

    public ModuleInfo getModuleInfo() {
        return mModuleInfo;
    }

    public static Boolean isModule(Object o) {
        return o instanceof Module;
    }

    public void setModuleInfo(ModuleInfo moduleInfo) {
        mModuleInfo = moduleInfo;
    }

    @Override
    public String toString() {
        return "Module{" +
                "mMachine=" + mMachine +
                ", mModuleInfo=" + mModuleInfo.toString() +
                ", mConnectionController=" + mConnectionController +
                '}';
    }

    public abstract String getDisplayName();

    /**
     * moduleId:
     */
    public static class ModuleInfo implements IStructure {
        public UInt8Prop keyProp = new UInt8Prop();
        public UInt16Prop moduleIdProp = new UInt16Prop();
        public UInt8Prop moduleIndexProp = new UInt8Prop();
        public UInt8Prop moduleStateProp = new UInt8Prop();
        public UInt32Prop snProp = new UInt32Prop();
        public UInt8Prop hardwareVersionProp = new UInt8Prop();
        public StringProp moduleFirmwareVersionProp = new StringProp();

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(moduleIdProp.toByteArray());
            buffer.write(moduleIndexProp.toByteArray());
            buffer.write(moduleStateProp.toByteArray());
            buffer.write(snProp.toByteArray());
            buffer.write(hardwareVersionProp.toByteArray());
            buffer.write(moduleFirmwareVersionProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            moduleIdProp.readBuffer(buffer);
            moduleIndexProp.readBuffer(buffer);
            moduleStateProp.readBuffer(buffer);
            snProp.readBuffer(buffer);
            hardwareVersionProp.readBuffer(buffer);
            moduleFirmwareVersionProp.readBuffer(buffer);
            return buffer;
        }

        @Override
        public String toString() {
            return "ModuleInfo{" +
                    "\nidProp=" + keyProp +
                    ",\n moduleIdProp=" + moduleIdProp +
                    ",\n moduleIndexProp=" + moduleIndexProp +
                    '}';
        }

        public int getKey() {
            return keyProp.getValue();
        }

        public void setKey(int key) {
            keyProp.setValue(key);
        }

        public int getModuleId() {
            return moduleIdProp.getValue();
        }

        public void setModuleId(int moduleId) {
            moduleIdProp.setValue(moduleId);
        }

        public int getModuleIndex() {
            return moduleIndexProp.getValue();
        }

        public void setModuleIndex(int moduleIndex) {
            moduleIndexProp.setValue(moduleIndex);
        }

        public int getModuleState() {
            return moduleStateProp.getValue();
        }

        public void setModuleState(int moduleState) {
            moduleStateProp.setValue(moduleState);
        }

        public IStructure getModuleIndexProp() {
            return moduleIndexProp;
        }

        public long getSn() {
            return snProp.getValue();
        }

        public String getFirmwareVersion() {
            return moduleFirmwareVersionProp.getValue();
        }
    }

    public static class ModuleType {
        public static final int HEAD_UNPLUGGED = -1;
        //        public static final int ADDON_HEATED_BED = 0x12;
        // head type
        public static final int HEAD_3DP = 0;
        public static final int HEAD_CNC = 1;
        public static final int HEAD_LASER = 2;
        public static final int LINEAR_2 = 3;
        public static final int ADDON_LEDS = 4;
        public static final int ADDON_ENCLOSURE = 5;
        public static final int ROTARY_MODULE = 6;

        public static final int ADDON_AIR_PURIFIER = 7;
        public static final int ADDON_EMERGENCY_BUTTON = 8;
        public static final int ADDON_CNC_CALIBRATION = 9;  //对刀模块
        public static final int ADDON_TOOLHEAD_CONVERTER = 10;
        public static final int ADDON_FAN = 11;
        public static final int LINEAR_2_5 = 12;
        public static final int HEAD_3DP_DOUBLE_EXTRUDER = 13;
        public static final int HEAD_LASER_10W = 14;
        public static final int HEAD_CNC_200W = 15;
        public static final int ADDON_ENCLOSURE_A400 = 16;
        public static final int ADDON_DRY_BOX = 17;
        public static final int ADDON_HEATED_BED_S20 = 512;
        public static final int ADDON_HEATED_BED_J1 = 513;
        public static final int LINEAR_J1 = 514;
        public static final int ADDON_HEATED_BED_A400 = 515;
        public static final int LINEAR_A400 = 516;
        public static final int EMERGENCY_BUTTON_A400 = 517;

        public static final int SNAPMAKER2_CONTROL = 2048;
        public static final int J1_CONTROL = 2049;
        public static final int A400_CONTROL = 2050;

//        public static final int ADDON_POWER = 15;
//        public static final int HEAD_CNC_2 = 16;
//        public static final int LINEAR_3 = 17;
//        public static final int ADDON_HEATED_BED = 18;


    }

    protected Context getAppContext() {
        return ServiceContainer.getInstance().getService(IAppService.class).getAppContext();
    }

}
