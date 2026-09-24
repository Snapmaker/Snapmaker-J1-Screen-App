package fabscreen.platform.base.service.machine;

import java.util.List;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.Module;
import io.reactivex.Observable;

//all machineInfo here, may properties
public class MachineInfo {
    public static final MachineInfo initialValue = new MachineInfo(new Vector(), null);
    public Vector size;
    public String controllerFWVersion;
    // series MachineType  //A150&A250 is a series
    public int seriesId;
    // model under the series 150,250 etc
    public int modelId;
    public int productId;
    // SN machine code
    public String serialNo;
    //mOutdatedVersionModuleListSubject
    public List<Module> moduleList;

    //virtual attributes, analysed from moduleList
    public boolean isRotaryAvailable;
    public boolean isEnclosureAvailable;
    public boolean isAirPurifierAvailable;
    public boolean isEmergencyStopAvailable;
    public boolean isHeatedBedAvailable;
    public boolean isDryBoxAvailable;

    // enum: fdm laser cnc
    public IMachine.WorkType workType = IMachine.WorkType.NONE;
    public int headType = Module.ModuleType.HEAD_UNPLUGGED;

    private MachineInfo mLastInfo;


    private MachineInfo(Vector s, MachineInfo last) {
        size = s;
        if (mLastInfo != null) {
            mLastInfo.mLastInfo = null;
        }
    }


    public List<Module> getModuleList() {
        return moduleList;
    }

    public void setMachineType(int position) {
    }

    public void setRotaryAvailable(int position) {
    }

    public String getMachineModelSeries() {
        switch (modelId) {
            case Constants.MACHINE_MODEL_SNAPMAKER_A150:
                return "Snapmaker 2.0 A150";
            case Constants.MACHINE_MODEL_SNAPMAKER_A250:
                return "Snapmaker 2.0 A250";
            case Constants.MACHINE_MODEL_SNAPMAKER_A350:
                return "Snapmaker 2.0 A350";
            default:
                return "Unknown";
        }
    }

    public Observable<Boolean> setAirPurifierEnabled(boolean enabled) {
        return null;
    }

    @Override
    public String toString() {
        return "MachineInfo{" +
                "size=" + size +
                ", controllerVersion='" + controllerFWVersion + '\'' +
                ", seriesId=" + seriesId +
                ", modelId=" + modelId +
                ", productId=" + productId +
                ", serialNo='" + serialNo + '\'' +
                ", moduleList=" + moduleList +
                ", isRotaryAvailable=" + isRotaryAvailable +
                ", isEnclosureAvailable=" + isEnclosureAvailable +
                ", isAirPurifierAvailable=" + isAirPurifierAvailable +
                ", isEmergencyStopAvailable=" + isEmergencyStopAvailable +
                ", workType=" + workType +
                ", mLastInfo=" + mLastInfo +
                '}';
    }

    public void reset() {
        moduleList = null;
        // TODO: 2022/5/13 reset all values
    }

    public String getModelName() {
        switch (productId) {
            case IMachine.Product.A150:
                return "A150";
            case IMachine.Product.A250:
                return "A250";
            case IMachine.Product.A350:
                return "A350";
            case IMachine.Product.A400:
                return "Artisan";
            case IMachine.Product.J1:
                return "Snapmaker J1";
            default:
                return "Unknown";
        }
    }
}
