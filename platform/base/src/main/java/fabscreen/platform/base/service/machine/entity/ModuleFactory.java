package fabscreen.platform.base.service.machine.entity;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module.ModuleType;
import fabscreen.platform.base.service.machine.entity.module.AirPurifier;
import fabscreen.platform.base.service.machine.entity.module.DryBox;
import fabscreen.platform.base.service.machine.entity.module.EmergencyButton;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.module.LinearModule;
import fabscreen.platform.base.service.machine.entity.module.RotaryModule;
import fabscreen.platform.base.service.machine.entity.module.UnknownModule;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;

public class ModuleFactory {
    public static Module createModule(Module.ModuleInfo info, IMachine mc, MachineConnectionController cc, IAppService appService) throws Exception {
        Module module;
        switch (info.getModuleId()) {
            case ModuleType.HEAD_3DP:
            case ModuleType.HEAD_3DP_DOUBLE_EXTRUDER:
                module = new FdmToolhead(info, mc, cc);
                break;
            case ModuleType.HEAD_LASER:
            case ModuleType.HEAD_LASER_10W:
                module = new LaserToolhead(info, mc, cc, appService);
                break;
            case ModuleType.HEAD_CNC:
//            case ModuleType.HEAD_CNC_2:
            case ModuleType.HEAD_CNC_200W:
                module = new CNCToolhead(info, mc, cc);
                break;
//            case ModuleType.ADDON_HEATED_BED:
            case ModuleType.ADDON_HEATED_BED_S20:
            case ModuleType.ADDON_HEATED_BED_A400:
            case ModuleType.ADDON_HEATED_BED_J1:
                module = new HeatedBed(info, mc, cc);
                break;
            case ModuleType.LINEAR_2:
            case ModuleType.LINEAR_2_5:
//            case ModuleType.LINEAR_3:
            case ModuleType.LINEAR_A400:
            case ModuleType.LINEAR_J1:
                module = new LinearModule(info, mc, cc);
                break;
            case ModuleType.ADDON_ENCLOSURE:
            case ModuleType.ADDON_ENCLOSURE_A400:
                Logger.d("enclosure init!1");
                module = new Enclosure(info, mc, cc, appService);
                break;
            case ModuleType.ADDON_AIR_PURIFIER:
                module = new AirPurifier(info, mc, cc, appService);
                break;
            case ModuleType.ADDON_EMERGENCY_BUTTON:
            case ModuleType.EMERGENCY_BUTTON_A400:
                module = new EmergencyButton(info, mc, cc);
                break;
            case ModuleType.ROTARY_MODULE:
                module = new RotaryModule(info, mc, cc);
                break;
            case ModuleType.ADDON_DRY_BOX:
                module = new DryBox(info, mc, cc);
                break;
            default:
                module = new UnknownModule(info, mc, cc);
                break;
        }
        module.init();
        return module;
    }
}
