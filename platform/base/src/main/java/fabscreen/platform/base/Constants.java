package fabscreen.platform.base;

import java.util.concurrent.TimeUnit;

/**
 * Entity or Interface or Manager provide constants
 */

@Deprecated
public class Constants {
    public static final String SP_DEFAULT = "com.snapmaker.fabscreen.PREFERENCE_DEFAULT";

    public static final int MACHINE_MODEL_UNKNOWN = 0;
    public static final int MACHINE_MODEL_SNAPMAKER_A150 = 1;
    public static final int MACHINE_MODEL_SNAPMAKER_A250 = 2;
    public static final int MACHINE_MODEL_SNAPMAKER_A350 = 3;
    public static final int MACHINE_MODEL_SNAPMAKER_A400 = 4;

    // machine type
    public static final int MACHINE_UNKNOWN = 0;
    public static final int MACHINE_A_0 = 1;
    public static final int MACHINE_J_1 = 2;
    public static final int MACHINE_A_400 = 3;

    // file type
    public static final int FILE_TYPE_UNKNOWN = 0;
    public static final int FILE_TYPE_3DP = 1;
    public static final int FILE_TYPE_CNC = 2;
    public static final int FILE_TYPE_LASER = 3;// laser and laser-10w
    public static final int FILE_TYPE_UPDATE = 4;
    public static final int FILE_TYPE_LOG = 5;

    // print source
    public static final int PRINT_SOURCE_SCREEN = 0;
    public static final int PRINT_SOURCE_LUBAN = 1;

    // heartbeat
    public static final int HEARTBEAT_INTERVAL = 5000;

    // polling
    public static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final int POLLING_INTERVAL = 1000;

    public static final int THROTTLE_DURATION = 800;
    public static final TimeUnit THROTTLE_TIME_UNIT = TimeUnit.MILLISECONDS;

    public static final int FIVE_MINUTES_DELAY_DURATION = 300000;

    // Machine
    public static final String MACHINE_TYPE_A150 = "Snapmaker 2 Model A150";
    public static final String MACHINE_TYPE_A250 = "Snapmaker 2 Model A250";
    public static final String MACHINE_TYPE_A350 = "Snapmaker 2 Model A350";
    public static final String MACHINE_TYPE_A400 = "Snapmaker 2 Model A400";


    public final static float MIN_LIVE_Z_OFFSET = -0.5f;
    public final static float MAX_LIVE_Z_OFFSET = 2.0f;

    public static final int LASER_CAMERA_OFFSET_X = -20;
    public static final int LASER_CAMERA_OFFSET_Y = 8;
    public static final int LASER_10W_CAMERA_OFFSET_X = -60;
    public static final int LASER_10W_CAMERA_OFFSET_Y = 0;
    public static final float LASER_TEST_PATTERN_X_DIFF = 0.2f;
    public static final float LASER_TEST_PATTERN_Z_DIFF = 0.5f;
    public static final float LASER_10W_CAMERA_FOCAL_LENGTH = 10f;
    public static final float LASER_MEASURE_OFFSET_X = 50f;


    // intent
    public static final String KEY_IS_FORCE_BACK_HOME = "isBackToHome";


    public static final float HIGH_LASER_LOW_POWER_LASER_PERCENT = 1f;
}
