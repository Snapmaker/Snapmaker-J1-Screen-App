package fabscreen.platform.base.helper;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.*;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.R;

public class ModuleIdNameMapper {
    public static List<String> convertIdsToNames(Context context, List<Integer> ids) {
        List<String> nameList = new ArrayList<>();
        for (int id : ids) {
            nameList.add(getModuleNameById(context, id));
        }
        return nameList;
    }

    public static String getModuleNameById(Context context, int id) {
        switch (id) {
            case HEAD_3DP:
                return context.getString(R.string.all_head_tool_3dp);
            case HEAD_3DP_DOUBLE_EXTRUDER:
                return context.getString(R.string.all_head_tool_double_extruder);
            case HEAD_LASER:
                return context.getString(R.string.all_head_tool_laser);
            case HEAD_LASER_10W:
                return context.getString(R.string.all_head_tool_laser_10w);
            case HEAD_CNC:
                return context.getString(R.string.all_head_tool_head_cnc);
            case HEAD_CNC_200W:
                return context.getString(R.string.all_head_tool_head_cnc_200w);
            case ADDON_ENCLOSURE:
            case ADDON_ENCLOSURE_A400:
                return context.getString(R.string.all_enclosure);
            case ROTARY_MODULE:
                return context.getString(R.string.all_rotary_module);
            case ADDON_AIR_PURIFIER:
                return context.getString(R.string.all_air_purifier);
            case EMERGENCY_BUTTON_A400:
                return context.getString(R.string.all_emergency_stop);
            case ADDON_HEATED_BED_A400:
                return context.getString(R.string.print_heated_bed);
            case LINEAR_A400:
                return context.getString(R.string.all_linear_module);
            default:
                return "Unknown Module";
        }
    }
}
