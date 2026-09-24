package fabscreen.platform.base.service.machine.connection.mock;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_AIR_PURIFIER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_CNC_CALIBRATION;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_DRY_BOX;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_EMERGENCY_BUTTON;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_FAN;
//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_J1;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_S20;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_LEDS;
//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_POWER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_TOOLHEAD_CONVERTER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC;
//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_2;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_UNPLUGGED;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_2;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_2_5;
//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_3;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ROTARY_MODULE;

public class DebugModule {
    public int moduleId = -1;
    public int index = -1;

    public DebugModule(int moduleId, int index) {
        this.moduleId = moduleId;
        this.index = index;
    }

    public DebugModule(String s) {
        String[] split = s.split("=");
        moduleId = Integer.parseInt(split[1]);
        index = Integer.parseInt(split[3]);
    }

    @Override
    public String toString() {
        return "DebugModule{" +
                "moduleId=" + moduleId +
                "=, index=" + index +
                "=}";
    }

    public String getModuleName() {
        switch (moduleId) {
            case HEAD_UNPLUGGED:
                return "HEAD_UNPLUGGED";
            case ADDON_HEATED_BED_S20:
            case ADDON_HEATED_BED_J1:
            case ADDON_HEATED_BED_A400:
                return "ADDON_HEATED_BED";
            case HEAD_3DP:
                return "HEAD_3DP";
            case HEAD_CNC:
                return "HEAD_CNC";
            case HEAD_LASER:
                return "HEAD_LASER";
            case LINEAR_2:
                return "LINEAR_2";
            case ADDON_LEDS:
                return "ADDON_LEDS";
            case ADDON_ENCLOSURE:
                return "ADDON_ENCLOSURE";
            case ROTARY_MODULE:
                return "ROTARY_MODULE";
            case ADDON_AIR_PURIFIER:
                return "ADDON_AIR_PURIFIER";
            case ADDON_EMERGENCY_BUTTON:
                return "ADDON_EMERGENCY_BUTTON";
            case ADDON_CNC_CALIBRATION:
                return "ADDON_CNC_CALIBRATION";
            case ADDON_TOOLHEAD_CONVERTER:
                return "ADDON_TOOLHEAD_CONVERTER";
            case ADDON_FAN:
                return "ADDON_FAN";
            case LINEAR_2_5:
                return "LINEAR_2_5";
            case HEAD_3DP_DOUBLE_EXTRUDER:
                return "HEAD_3DP_DOUBLE_EXTRUDER";
            case HEAD_LASER_10W:
                return "HEAD_LASER_10W";
//            case ADDON_POWER:
//                return "ADDON_POWER";
            case HEAD_CNC_200W:
                return "HEAD_CNC_2";
            case LINEAR_A400:
                return "LINEAR_3";
            case ADDON_DRY_BOX:
                return "干燥箱";
            default:
                return "目前不支持mock";
        }
    }
}
