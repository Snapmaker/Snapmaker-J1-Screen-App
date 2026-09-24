package fabscreen.platform.base;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Route paths for ARouter.
 * <p>
 * ARouter requires at least two_level path. We use feature name(former "module" name) as the first
 * level, and activity name(with snake_case) as the second level.
 */
public class RoutePath {
    /*
     * Non-constant value like HOME + "/index" will break the StringDef lint feature.
     *
     * But we can still do this for convenience.
     */
    // Home
    public static final String FABSCREEN_INDEX = "/fabscreen/index";
    public static final String J1_INDEX = "/J1/index";
    public static final String S30_INDEX = "/s30/index";
    public static final String A400_INDEX = "/a400/index";
    private static final String HOME = "/home";
    public static final String HOME_MAIN = HOME + "/main";
    public static final String HOME_LAND = HOME + "/land";
    public static final String HOME_INDEX = HOME + "/index";

    // Welcome
    private static final String WELCOME = "/welcome";
    public static final String WELCOME_INDEX = WELCOME + "/index";
    public static final String WELCOME_J1 = WELCOME + "/j1_index";
    public static final String WELCOME_A400 = WELCOME + "/a400_index";

    // Remote
    private static final String REMOTE = "/remote";
    public static final String REMOTE_INDEX = REMOTE + "/home";
    public static final String S30_REMOTE_INDEX = REMOTE + "/home_s30";
    public static final String A400_REMOTE_INDEX = REMOTE + "/home_a400";
    public static final String J1_REMOTE_INDEX = REMOTE + "/home_j1";

    // Settings
    private static final String SETTINGS = "/settings";
    public static final String SETTINGS_INDEX = SETTINGS + "/index";
    public static final String SETTINGS_J1_INDEX = SETTINGS + "/j1_index";
    public static final String SETTINGS_A400_INDEX = SETTINGS + "/a400_index";
    public static final String SETTINGS_EXPERIMENT = SETTINGS + "/experiment";
    public static final String SETTINGS_ABOUT = SETTINGS + "/about_activity";
    public static final String SETTINGS_NAME = SETTINGS_ABOUT + "/name";
    public static final String SETTINGS_ABOUT_ = SETTINGS + "/about_fragment";
    public static final String SETTINGS_UPDATE = SETTINGS + "/update";
    public static final String SETTINGS_FIRMWARE = SETTINGS + "/firmware";
    public static final String SETTINGS_FIRMWARE_S30 = SETTINGS + "/firmware_s30";
    public static final String SETTINGS_FACTORY = SETTINGS + "/factory";
    public static final String A400_SETTINGS = SETTINGS + "/a400";
    public static final String A400_SETTINGS_REPLACE_MODULE = A400_SETTINGS + "/replace_module";
    public static final String A400_SETTINGS_REPLACE_HOTEND = A400_SETTINGS + "/replace_hotend";
    public static final String A400_GREEN_SCREEN = A400_SETTINGS + "/green_screen";
    public static final String A400_SETTINGS_UPDATE_SUCCESS = A400_SETTINGS + "/update_success";
    public static final String A400_SETTINGS_UPDATE_IN_PROGRESS = A400_SETTINGS + "/update_in_progress";
    public static final String A400_SETTINGS_RECOVERY_MODE = A400_SETTINGS + "/recovery_mode";
    public static final String A400_SETTINGS_UPDATE_MODULES = A400_SETTINGS + "/update_modules";
    public static final String J1_SETTINGS_UPDATE_SUCCESS = SETTINGS_J1_INDEX + "/update_success";
    public static final String J1_SETTINGS_UPDATE_IN_PROGRESS = SETTINGS_J1_INDEX + "/update_in_progress";
    public static final String J1_SETTINGS_RECOVERY_MODE = SETTINGS_J1_INDEX + "/recovery_mode";

    // Guide
    private static final String GUIDE = "/guide";
    private static final String GUIDE_A400 = GUIDE + "_a400";
    public static final String GUIDE_j1 = GUIDE + "/j1";
    public static final String GUIDE_3DP = GUIDE + "/3dp";
    public static final String GUIDE_A400_MILESTONE = GUIDE_A400 + "/3dp";
    public static final String GUIDE_LASER = GUIDE + "/laser";
    public static final String GUIDE_10W_LASER = GUIDE + "/10w_laser";
    public static final String GUIDE_ROTARY_LASER = GUIDE + "/rotary_laser";
    public static final String GUIDE_CNC = GUIDE + "/cnc";
    public static final String GUIDE_ROTARY_CNC = GUIDE + "/rotary_cnc";

    // Machine Tools
    private static final String TOOLS = "/machine_tools";
    public static final String TOOLS_CONTROL_J1 = TOOLS + "/control_j1";
    public static final String TOOLS_CONTROL_A400 = TOOLS + "/control_a400";
    public static final String TOOLS_CONTROL_A400_JOG = TOOLS_CONTROL_A400 + "/jog";

    public static final String SETTINGS_TOOL = TOOLS + "/index";

    public static final String TOOLS_BLOCK_SETUP = TOOLS + "/stopper_setup";
    public static final String TOOLS_LOAD_FILAMENT = TOOLS + "/load_filament";
    public static final String TOOLS_SETUP_CALIBRATION = TOOLS + "/setup_calibration";
    public static final String TOOLS_SETUP_XY_CALIBRATION = TOOLS + "/setup_xy_calibration";
    public static final String TOOLS_SETUP_SINGLE_SINGLE_BED_LEVELING = TOOLS + "/setup_single_single_bed_leveling";
    public static final String TOOLS_SETUP_SINGLE_SINGLE_FILAMENT = TOOLS + "/setup_single_single_filament";

    // calibration
    private static final String TOOLS_CALIBRATION = TOOLS + "/calibration";
    //  s20
    public static final String TOOLS_CALIBRATION_S20 = TOOLS_CALIBRATION + "/_s20";
    //      CNC
    private static final String TOOLS_CALIBRATION_S20_CNC = TOOLS_CALIBRATION_S20 + "/cnc";
    public static final String TOOLS_CALIBRATION_S20_CNC_BIT = TOOLS_CALIBRATION_S20_CNC + "/bit";
    //      3DP
    public static final String TOOLS_CALIBRATION_S20_3DP = TOOLS_CALIBRATION_S20 + "/3dp";
    //      Laser
    public static final String TOOLS_CALIBRATION_S20_LASER = TOOLS_CALIBRATION_S20 + "/laser";
    //          10W
    public static final String TOOLS_CALIBRATION_S20_10W_LASER_THICKNESS_MEASURE = TOOLS_CALIBRATION_S20_LASER + "/10w_laser_thickness_measure";
    public static final String TOOLS_CALIBRATION_S20_10W_CAMERA_CALIBRATION = TOOLS_CALIBRATION_S20_LASER + "/10w_laser_camera_calibration";
    //          1.6W
    public static final String TOOLS_CALIBRATION_S20_CNC_ORIGIN = TOOLS_CALIBRATION_S20_CNC + "/origin";

    //  A400
    public static final String TOOLS_CALIBRATION_A400 = TOOLS_CALIBRATION + "/_A400";
    public static final String TOOLS_CALIBRATION_A400_COMPLETE = TOOLS_CALIBRATION_A400 + "/complete";
    //      3DP
    public static final String TOOLS_CALIBRATION_A400_3DP = TOOLS_CALIBRATION_A400 + "/3dp";

    //          LEVELING_BED
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_CHECK_MODE = TOOLS_CALIBRATION_A400_3DP + "/leveling_bad_calibration_check_mode";
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_AUTO = TOOLS_CALIBRATION_A400_3DP + "/leveling_bad_automatic_calibration_auto";
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_MANUAL = TOOLS_CALIBRATION_A400_3DP + "/leveling_bad_automatic_calibration_manual";
    //          LEVELING_Z
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_CHECK_MODE = TOOLS_CALIBRATION_A400_3DP + "/leveling_z_calibration_check_mode";
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_AUTOMATIC = TOOLS_CALIBRATION_A400_3DP + "/leveling_z_calibration_auto";
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_MANUAL = TOOLS_CALIBRATION_A400_3DP + "/leveling_z_calibration_manual";
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_SENSOR = TOOLS_CALIBRATION_A400_3DP + "/leveling_z_calibration_sensor";
    //          LEVELING_XY
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_XY_CHECK_MODE = TOOLS_CALIBRATION_A400_3DP + "/leveling_xy_calibration_check_mode";
    public static final String TOOLS_CALIBRATION_A400_3DP_LEVELING_XY = TOOLS_CALIBRATION_A400_3DP + "/leveling_xy_automatic_calibration";
    //      Laser
    public static final String TOOLS_CALIBRATION_A400_LASER = TOOLS_CALIBRATION_A400 + "/laser";
    //          1.6W
    //          10W
    public static final String TOOLS_CALIBRATION_A400_LASER_10W = TOOLS_CALIBRATION_A400_LASER + "/10w";
    public static final String TOOLS_CALIBRATION_A400_LASER_10W_PLATFORM_HEIGHT_INFO = TOOLS_CALIBRATION_A400_LASER_10W + "/platform_height_info";
    public static final String TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION = TOOLS_CALIBRATION_A400_LASER_10W + "/camera";
    public static final String TOOLS_CALIBRATION_A400_LASER_FOCUS_CALIBRATION = TOOLS_CALIBRATION_A400_LASER + "/focus_calibration";
    public static final String TOOLS_CALIBRATION_A400_LASER_THICKNESS_MEASURE_CALIBRATION = TOOLS_CALIBRATION_A400_LASER + "/thickness_measure_calibration";
    public static final String TOOLS_CALIBRATION_A400_LASER_4_AXIS = TOOLS_CALIBRATION_A400_LASER + "_4axis";
    public static final String TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS = TOOLS_CALIBRATION_A400_LASER_4_AXIS + "/central_axis";
    public static final String TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS_SELECT_MATERIAL = TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS + "/select_material";

    //      CNC
    private static final String TOOLS_CALIBRATION_A400_CNC = TOOLS_CALIBRATION_A400 + "/cnc";
    public static final String TOOLS_CALIBRATION_A400_CNC_SET_ORIGIN = TOOLS_CALIBRATION_A400_CNC + "/set_origin";
    public static final String TOOLS_CALIBRATION_A400_CNC_CHANGE_ASSISTANT = TOOLS_CALIBRATION_A400_CNC + "/change_assistant";
    public static final String TOOLS_CALIBRATION_A400_CNC_ORIGIN_ASSISTANT = TOOLS_CALIBRATION_A400_CNC + "/origin_assistant";
    public static final String TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_CHECK_MODE = TOOLS_CALIBRATION_A400_CNC + "/manual_tool_check_mode";
    public static final String TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_BASIC = TOOLS_CALIBRATION_A400_CNC + "/manual_tool_basic";
    public static final String TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_ADVANCED = TOOLS_CALIBRATION_A400_CNC + "/manual_tool_advanced";
    public static final String TOOLS_CALIBRATION_A400_CNC_SETUP = TOOLS_CALIBRATION_A400_CNC + "/setup";
    //  J1
    public static final String TOOLS_CALIBRATION_J1 = TOOLS_CALIBRATION + "/_J1";

    public static final String TOOLS_SETUP_COMMON_INTRO = TOOLS_CALIBRATION_S20_LASER + "/10w_laser_thickness_measure_intro";
    public static final String TOOLS_CALIBRATION_J1_3DP = TOOLS_CALIBRATION_J1 + "/3dp";
    public static final String TOOLS_CALIBRATION_J1_3DP_LEVELING_BED_AUXILIARY = TOOLS_CALIBRATION_J1_3DP + "/leveling_bad_auxiliary_calibration";
    public static final String TOOLS_CALIBRATION_J1_3DP_LEVELING_BED = TOOLS_CALIBRATION_J1_3DP + "/leveling_bad_calibration";
    public static final String TOOLS_CALIBRATION_J1_3DP_LEVELING_Z_AUXILIARY = TOOLS_CALIBRATION_J1_3DP + "/leveling_z_auxiliary_calibration";
    public static final String TOOLS_CALIBRATION_J1_3DP_LEVELING_Z = TOOLS_CALIBRATION_J1_3DP + "/leveling_z_calibration";
    public static final String TOOLS_CALIBRATION_J1_3DP_LEVELING_XY_AUXILIARY = TOOLS_CALIBRATION_J1_3DP + "/leveling_xy_auxiliary_calibration";
    public static final String TOOLS_CALIBRATION_J1_3DP_LEVELING_XY = TOOLS_CALIBRATION_J1_3DP + "/leveling_xy_calibration";
    public static final String TOOLS_CALIBRATION_J1_3DP_CALIBRATION_CHECK = TOOLS_CALIBRATION_J1_3DP + "/leveling_calibration_check";
    public static final String TOOLS_CALIBRATION_J1_3DP_CALIBRATION_VIBRATION = TOOLS_CALIBRATION_J1_3DP + "/calibration_vibration";

    private static final String FILE = "/file";
    public static final String FILE_BROWSER = FILE + "/browser";
    public static final String FILE_BROWSE_J1 = FILE_BROWSER + "/J1";
    public static final String FILE_BROWSE_A400 = FILE_BROWSER + "/A400";

    // Print
    private static final String PRINT = "/print";
    public static final String PRINT_PREVIEW = PRINT + "/preview";
    public static final String PRINT_PREVIEW_SETTINGS = PRINT_PREVIEW + "/settings";
    public static final String PRINT_PRINT = PRINT + "/print";
    public static final String PRINT_PRINT_J1 = PRINT_PRINT + "/j1platform";
    public static final String PRINT_PRINT_J1_AJUSTMENT_MENT_CONTAINER = PRINT_PRINT_J1 + "/J1AdjustmentContainer";
    public static final String PRINT_PRINT_A400 = PRINT_PRINT + "/a400platform";
    public static final String PRINT_LASER_SET_Z_SELECT = PRINT_PRINT_A400 + "/laserSetZSelect";
    public static final String PRINT_SETTING = PRINT_PRINT_A400 + "/printSetting";
    public static final String PREPARE_LASER = PRINT + "/prepare/laser";
    public static final String PREPARE_CNC = PRINT + "/prepare/cnc";
    public static final String PREPARE_PRINT_JOG_CONTROL = PRINT + "/prepare/jog";

    // Add-ons
    private static final String ADDONS = "/add_ons";
    public static final String ADDONS_EMERGENCY_STOP = ADDONS + "/emergency_stop";
    public static final String ADDONS_AIR_PURIFIER = ADDONS + "/airpurifier";
    public static final String ADDONS_ENCLOSURE = ADDONS + "/enclosure";

    // Debug
    public static final String DEBUG = "/debug";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            HOME_MAIN,
            HOME_INDEX,
            WELCOME_J1,
            WELCOME_INDEX,
            REMOTE_INDEX,
            S30_REMOTE_INDEX,
            A400_REMOTE_INDEX,
            J1_REMOTE_INDEX,
            SETTINGS_J1_INDEX,
            SETTINGS_A400_INDEX,
            SETTINGS_EXPERIMENT,
            SETTINGS_ABOUT,
            SETTINGS_FACTORY,
            SETTINGS_UPDATE,
            SETTINGS_FIRMWARE,
            SETTINGS_FIRMWARE_S30,
            SETTINGS_NAME,
            A400_SETTINGS_REPLACE_MODULE,
            GUIDE_j1,
            GUIDE_3DP,
            GUIDE_LASER,
            GUIDE_10W_LASER,
            GUIDE_ROTARY_LASER,
            GUIDE_CNC,
            GUIDE_ROTARY_CNC,
            TOOLS_CONTROL_J1,
            TOOLS_CONTROL_A400,
            TOOLS_CONTROL_A400_JOG,
            TOOLS_CALIBRATION_S20_3DP,
            TOOLS_CALIBRATION_S20_LASER,
            TOOLS_CALIBRATION_S20_10W_LASER_THICKNESS_MEASURE,
            TOOLS_CALIBRATION_S20_10W_CAMERA_CALIBRATION,
            TOOLS_CALIBRATION_S20_CNC_ORIGIN,
            TOOLS_CALIBRATION_S20_CNC_BIT,
            TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS,
            FILE_BROWSER,
            PRINT_PREVIEW,
            PRINT_PREVIEW_SETTINGS,
            PRINT_PRINT,
            PRINT_PRINT_A400,
            PRINT_PRINT_J1,
            ADDONS_EMERGENCY_STOP,
            ADDONS_AIR_PURIFIER,
            ADDONS_ENCLOSURE,
            PREPARE_LASER,
            PREPARE_CNC,
            FABSCREEN_INDEX,
            J1_INDEX,
            S30_INDEX,
            A400_INDEX,
            DEBUG,
            WELCOME_A400
    })
    public @interface Path {
    }
}
