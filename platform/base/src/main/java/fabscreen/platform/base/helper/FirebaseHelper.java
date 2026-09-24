package fabscreen.platform.base.helper;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.service.machine.entity.Module;

public class FirebaseHelper {
    private final static String EVENT_START_UP = "start";

    private final static String PARAM_HEAD_TYPE = "head_type";
    private final static String PARAM_MODEL = "model";

    public static void logStartUpEvent(FirebaseAnalytics firebaseAnalytics, int headType, int machineModel) {
        if (firebaseAnalytics == null) return;

        Bundle params = new Bundle();

        switch (headType) {
            case Module.ModuleType.HEAD_3DP:
                params.putString(PARAM_HEAD_TYPE, "3DP");
                break;
            case Module.ModuleType.HEAD_LASER:
                params.putString(PARAM_HEAD_TYPE, "Laser");
                break;
            case Module.ModuleType.HEAD_LASER_10W:
                params.putString(PARAM_HEAD_TYPE, "Laser 10W");
                break;
            case Module.ModuleType.HEAD_CNC:
                params.putString(PARAM_HEAD_TYPE, "CNC");
                break;
            case Module.ModuleType.HEAD_UNPLUGGED:
                params.putString(PARAM_HEAD_TYPE, "Unplugged");
                break;
            default:
                params.putString(PARAM_HEAD_TYPE, "Unknown");
                break;
        }

        switch (machineModel) {
            case Constants.MACHINE_MODEL_SNAPMAKER_A150:
                params.putString(PARAM_MODEL, Constants.MACHINE_TYPE_A150);
                break;
            case Constants.MACHINE_MODEL_SNAPMAKER_A250:
                params.putString(PARAM_MODEL, Constants.MACHINE_TYPE_A250);
                break;
            case Constants.MACHINE_MODEL_SNAPMAKER_A350:
                params.putString(PARAM_MODEL, Constants.MACHINE_TYPE_A350);
                break;
            default:
                params.putString(PARAM_MODEL, "Unknown");
                break;
        }

        firebaseAnalytics.logEvent(EVENT_START_UP, params);
    }
}
