package fabscreen.platform.base.service;

import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fabscreen.platform.base.Constants;

public interface IPreferences {
    Helper getHelper();

    boolean getPref(String key, boolean defValue);

    String getPref(String key, String defValue);

    Set<String> getPref(String key, Set<String> defValue);

    float getPref(String key, float defValue);

    int getPref(String key, int defValue);

    void setPref(String key, boolean defValue);

    void setPref(String key, String defValue);

    void setPref(String key, float defValue);

    void setPref(String key, int defValue);

    void setPref(String key, Set<String> defValue);

    boolean reset(Map<String, Object> pairs);

    class Helper {
        // machine
        private static final String MACHINE_NAME = "MACHINE_NAME";
        private static final String MACHINE_MODEL = "MACHINE_MODEL";
        private static final String MACHINE_SETUP_FLAG = "MACHINE_SETUP_FLAG";
        private static final String MACHINE_UPDATE_FLAG = "MACHINE_UPDATE_FLAG";

        private static final String MACHINE_SETUP_LANGUAGE = "MACHINE_SETUP_LANGUAGE";

        private static final String MACHINE_SETUP_3DP = "MACHINE_SETUP_3DP";
        private static final String MACHINE_SETUP_LASER = "MACHINE_SETUP_LASER";
        private static final String MACHINE_SETUP_CNC = "MACHINE_SETUP_CNC";
        private static final String MACHINE_SETUP_10W_LASER = "MACHINE_SETUP_10W_LASER";
        private static final String MACHINE_SETUP_ROTARY_LASER = "MACHINE_SETUP_ROTARY_LASER";
        private static final String MACHINE_SETUP_ROTARY_CNC = "MACHINE_SETUP_ROTARY_CNC";
        private static final String MACHINE_SETUP_ROTARY_10W_LASER = "MACHINE_SETUP_ROTARY_10W_LASER";
        private static final String MACHINE_SETUP_DEVELOPER = "MACHINE_SETUP_DEVELOPER";
        private static final String NEED_QUERY_MACHINE_ERROR = "NEED_QUERY_MACHINE_ERROR";
        private static final String SCREEN_SAVER_ENABLED = "SCREEN_SAVER_ENABLED";
        private static final String SCREEN_SAVER_DELAY_TIME = "SCREEN_SAVER_DELAY_TIME";

        // ?
        private static final String API_HOST = "API_HOST";
        private static final String UPDATE_PACKAGE_VERSION = "UPDATE_PACKAGE_VERSION";
        private static final String CHECK_UPDATE_FLAG = "CHECK_UPDATE_FLAG";
        private static final String UPDATE_NOTIFICATION = "UPDATE_NOTIFICATION";
        private static final String UPDATE_LAST_CHECK_VERSION = "UPDATE_LAST_CHECK_VERSION";

        // laser
        private static final String LASER_MATERIAL_THICKNESS = "LASER_MATERIAL_THICKNESS";
        private static final String LASER_BOTTOM_Z = "LASER_BOTTOM_Z";
        private static final String LASER_CAMERA_CALIBRATION = "LASER_CAMERA_CALIBRATION";
        private static final String LASER_CONTROL_POWER = "LASER_CONTROL_POWER";

        // 10W Laser
        private static final String LASER_10W_CAMERA_CALIBRATION = "LASER_10W_CAMERA_CALIBRATION";

        // print
        private static final String FILE_LOC = "FILE_LOC";
        private static final String PRINT_SOURCE = "PRINT_SOURCE";
        private static final String FILE_NAME = "FILE_NAME";
        private static final String FILE_PATH = "FILE_PATH";
        private static final String FILE_ESTIMATED_TIME = "FILE_ESTIMATED_TIME";
        private static final String FILE_TOTAL_LINES = "FILE_TOTAL_LINES";
        private static final String PRINT_ELAPSED_TIME = "PRINT_ELAPSED_TIME";
        private static final String PRINT_SELECTED_MODE = "PRINT_SELECTED_MODE";
        private static final String PRINT_OFFSET_X = "PRINT_OFFSET_X";
        private static final String A400_CNC_SELECT_MODEL = "A400_CNC_SELECT_MODEL";

        // 3DP
        private static final String A3DP_CALIBRATION_MODE = "3DP_CALIBRATION_MODE";
        private static final String A3DP_FAST_CALIBRATION_ON = "3DP_FAST_CALIBRATION_ON";
        private static final String A3DP_CALIBRATION_GRID = "3DP_CALIBRATION_GRID";
        private static final String A3DP_CALIBRATION_HEATED_LEVELING_ON = "3DP_CALIBRATION_HEATED_LEVELING_ON";
        private static final String A3DP_CALIBRATION_HEATED_UP_TEMPERATURE = "3DP_CALIBRATION_HEATED_UP_TEMPERATURE";

        // Laser
        private static final String LASER_CALIBRATION_MODE = "LASER_CALIBRATION_MODE";
        private static final String LASER_CAMERA_LIGHT_ON = "LASER_CAMERA_LIGHT_ON";
        private static final String LASER_4AXIS_CALIBRATION_MODE = "LASER_4AXIS_CALIBRATION_MODE";
        private static final String LASER_PARAM_S1_PLUS_VALUE = "LASER_MEASURE_S1_PLUS_VALUE";
        private static final String LASER_PARAM_S2_PLUS_VALUE = "LASER_MEASURE_S2_PLUS_VALUE";
        private static final String LASER_PLATFORM_Z = "LASER_PLATFORM_Z";
        private static final String A400_LASER_ROTARY_AXIS_Z = "A400_LASER_ROTARY_AXIS_Z";

        // Addon
        private static final String ADD_ON_AIR_PURIFIER_3DP_AUTO_ON_FLAG = "AIR_PURIFIER_3DP_AUTO_ON_FLAG";
        private static final String ADD_ON_AIR_PURIFIER_LASER_AUTO_ON_FLAG = "AIR_PURIFIER_LASER_AUTO_ON_FLAG";
        private static final String ADD_ON_AIR_PURIFIER_CNC_AUTO_ON_FLAG = "AIR_PURIFIER_CNC_AUTO_ON_FLAG";
        private static final String ADD_ON_AIR_PURIFIER_AUTO_OFF_FLAG = "AIR_PURIFIER_AUTO_OFF_FLAG";
        private static final String ADD_ON_ENCLOSURE_AUTO_LIGHTING_ON_FLAG = "ENCLOSURE_AUTO_LIGHTING_ON_FLAG";

        // Settings

        private static final String SETTINGS_FIREBASE_ANALYTICS_FLAG = "SETTINGS_FIREBASE_ANALYTICS_FLAG";
        private static final String SETTINGS_USER_SELECTED_LANGUAGE = "SETTINGS_USER_SELECTED_LANGUAGE";

        private static final String HEADER_ONLINE_SYNC_ID = "HEADER_ONLINE_SYNC_ID";
        private static final String DEBUG_FLAG = "DEBUG_FLAG";
        private static final String DEBUG_APP_MACHINE_PROTOCOL = "APP_MACHINE_PROTOCOL";

        private static final String DEBUG_MOCK_ENABLED = "DEBUG_MOCK_ENABLED";
        private static final String DEBUG_MACHINE_SERIES = "DEBUG_MACHINE_SERIES";
        private static final String DEBUG_MACHINE_MODEL = "DEBUG_MACHINE_MODEL";
        private static final String DEBUG_MODULE_LIST = "DEBUG_MODULE_LIST";
        private static final String CONTROLLER_MODEL = "CONTROLLER_MODEL";
        private static final String LAST_CONNECTED_AP_SSID = "LAST_CONNECTED_AP_SSID";
        private static final String LAST_CONNECTED_AP_PWD = "LAST_CONNECTED_AP_PWD";
        private static final String SHOW_UPDATE_RESULT_ON_NEXT_STARTUP = "SHOW_UPDATE_RESULT_ON_NEXT_STARTUP";

        // Calibration
        private static final String J1_HEATED_BED_LEVELING_IS_AUXILIARY = "J1_HEATED_BED_LEVELING_IS_AUXILIARY";
        private static final String J1_Z_OFFSET_CALIBRATION_IS_AUXILIARY = "J1_Z_OFFSET_CALIBRATION_IS_AUXILIARY";
        private static final String J1_XY_OFFSET_CALIBRATION_IS_AUXILIARY = "J1_XY_OFFSET_CALIBRATION_IS_AUXILIARY";
        private static final String A400_LEVELING_BED_CALIBRATION_MODE = "A400_LEVELING_BED_CALIBRATION_MODE";
        private static final String A400_CENTRAL_AXIS_CALIBRATION_MATERIAL_TYPE = "A400_CENTRAL_AXIS_CALIBRATION_MATERIAL_TYPE";
        private static final String A400_LEVELING_BED_CALIBRATION_GRID = "A400_LEVELING_BED_CALIBRATION_GRID";
        private static final String A400_LEVELING_BED_CALIBRATION_BED_TEMPERATURE = "A400_LEVELING_BED_CALIBRATION_BED_TEMPERATURE";
        private static final String A400_LEVELING_Z_CALIBRATION_MODE = "A400_LEVELING_Z_CALIBRATION_MODE";
        private static final String A400_LEVELING_XY_CALIBRATION_MATERIAL_SELECTION = "A400_LEVELING_XY_CALIBRATION_MATERIAL_SELECTION";

        private static final String A400_LEVELING_XY_CALIBRATION_LEFT_PRINT_TEMPERATURE = "A400_LEVELING_XY_CALIBRATION_LEFT_PRINT_TEMPERATURE";
        private static final String A400_LEVELING_XY_CALIBRATION_RIGHT_PRINT_TEMPERATURE = "A400_LEVELING_XY_CALIBRATION_RIGHT_PRINT_TEMPERATURE";
        private static final String A400_LEVELING_XY_CALIBRATION_LEFT_STANDBY_TEMPERATURE = "A400_LEVELING_XY_CALIBRATION_LEFT_STANDBY_TEMPERATURE";
        private static final String A400_LEVELING_XY_CALIBRATION_RIGHT_STANDBY_TEMPERATURE = "A400_LEVELING_XY_CALIBRATION_RIGHT_STANDBY_TEMPERATURE";
        private static final String A400_LEVELING_XY_CALIBRATION_BED_PRINT_TEMPERATURE = "A400_LEVELING_XY_CALIBRATION_BED_PRINT_TEMPERATURE";
        private static final String A400_CNC_MANUAL_TOOL_CALIBRATION = "A400_CNC_MANUAL_TOOL_CALIBRATION";
        private static final String A400_LASER_PRINT_XY_ORIGIN_MODE = "A400_LASER_PRINT_XY_ORIGIN_MODEL";


        private static final String GUIDE_TEMPERATURE_SELF_CHECK_STATE = "GUIDE_TEMPERATURE_SELF_CHECK_STATE";
        private static final String GUIDE_CALIBRATION = "GUIDE_CALIBRATION";
        private static final String GUIDE_LEVELING_BED = "GUIDE_LEVELING_BED";
        private static final String GUIDE_LEVELING_XY = "GUIDE_LEVELING_XY";
        private static final String GUIDE_LEVELING_Z = "GUIDE_LEVELING_Z";
        private static final String GUIDE_CHECK_PRINT = "GUIDE_CHECK_PRINT";
        private static final String GUIDE_VIBRATION_COMPENSATION = "GUIDE_VIBRATION_COMPENSATION";
        private static final String CAMERA_CALIBRATION_TAKE_PHOTO_VECTOR = "CAMERA_CALIBRATION_TAKE_PHOTO_VECTOR";

        private static final String J1_EXTRUDER_TARGET_TEMP_L = "J1_EXTRUDER_TARGET_TEMP_L";
        private static final String J1_EXTRUDER_TARGET_TEMP_R = "J1_EXTRUDER_TARGET_TEMP_R";

        private static final String SETTING_REMOTE_ALLOW_CONNECTION = "SETTING_REMOTE_ALLOW_CONNECTION";
        private static final String SETTING_REMOTE_SAFE_MODE = "SETTING_REMOTE_SAFE_MODE";
        private static final String SETTING_REMOTE_CONNECTION_VERIFICATION = "SETTING_REMOTE_CONNECTION_VERIFICATION";
        private static final String REMOTE_TOKEN = "REMOTE_TOKEN";

        // temporary pref
        private static final String FACTORY_USB_OFF = "FACTORY_USB_OFF";

        private final IPreferences service;

        Helper(IPreferences preferences) {
            this.service = preferences;
        }


        public boolean reset() {
            Map<String, Object> resetKVs = new HashMap<>();
            resetKVs.put(MACHINE_NAME, "");
            resetKVs.put(MACHINE_MODEL, "");
            resetKVs.put(SETTINGS_USER_SELECTED_LANGUAGE, MultiLanguageManager.LANGUAGE_DEFAULT);
            resetKVs.put(MACHINE_SETUP_FLAG, false);
            resetKVs.put(MACHINE_SETUP_3DP, false);
            resetKVs.put(MACHINE_SETUP_LASER, false);
            resetKVs.put(MACHINE_SETUP_10W_LASER, false);
            resetKVs.put(MACHINE_SETUP_CNC, false);
            resetKVs.put(MACHINE_SETUP_ROTARY_LASER, false);
            resetKVs.put(MACHINE_SETUP_ROTARY_10W_LASER, false);
            resetKVs.put(MACHINE_SETUP_ROTARY_CNC, false);
            resetKVs.put(MACHINE_UPDATE_FLAG, false);
            resetKVs.put(MACHINE_SETUP_LANGUAGE, false);
            resetKVs.put(CHECK_UPDATE_FLAG, true);
            resetKVs.put(UPDATE_LAST_CHECK_VERSION, "0.0.0.0");
            resetKVs.put(UPDATE_NOTIFICATION, false);
            resetKVs.put(LASER_MATERIAL_THICKNESS, 1.5f);
            resetKVs.put(LASER_CONTROL_POWER, 100f);
            resetKVs.put(FILE_LOC, true);
            resetKVs.put(PRINT_SOURCE, 0);
            resetKVs.put(FILE_PATH, "");
            resetKVs.put(PRINT_OFFSET_X, 0f);
            resetKVs.put(PRINT_SELECTED_MODE, 0);
            resetKVs.put(A3DP_CALIBRATION_MODE, 0);
            resetKVs.put(A3DP_FAST_CALIBRATION_ON, true);
            resetKVs.put(A3DP_CALIBRATION_HEATED_LEVELING_ON, false);
            resetKVs.put(A3DP_CALIBRATION_HEATED_UP_TEMPERATURE, 70f);
            resetKVs.put(LASER_CALIBRATION_MODE, 0);
            resetKVs.put(LASER_4AXIS_CALIBRATION_MODE, 1);
            resetKVs.put(ADD_ON_ENCLOSURE_AUTO_LIGHTING_ON_FLAG, true);
            resetKVs.put(ADD_ON_AIR_PURIFIER_3DP_AUTO_ON_FLAG, false);
            resetKVs.put(ADD_ON_AIR_PURIFIER_LASER_AUTO_ON_FLAG, true);
            resetKVs.put(ADD_ON_AIR_PURIFIER_CNC_AUTO_ON_FLAG, false);
            resetKVs.put(ADD_ON_AIR_PURIFIER_AUTO_OFF_FLAG, true);
            resetKVs.put(DEBUG_FLAG, false);
            resetKVs.put(FACTORY_USB_OFF, false);
            resetKVs.put(MACHINE_SETUP_DEVELOPER, false);
            resetKVs.put(GUIDE_LEVELING_BED, false);
            resetKVs.put(GUIDE_LEVELING_Z, false);
            resetKVs.put(GUIDE_LEVELING_XY, false);
            resetKVs.put(GUIDE_CHECK_PRINT, false);
            resetKVs.put(GUIDE_CALIBRATION, false);
            return service.reset(resetKVs);
        }


        // -- Machine

        public String getMachineName() {
            return service.getPref(MACHINE_NAME, "");
        }

        public void setMachineName(String name) {
            service.setPref(MACHINE_NAME, name);
        }

        public String getMachineModel() {
            return service.getPref(MACHINE_MODEL, Constants.MACHINE_TYPE_A150);
        }

        public void setMachineModel(String machineModel) {
            service.setPref(MACHINE_MODEL, machineModel);
        }

        /**
         * Indicates if screen has been setup.
         */
        public boolean getMachineSetupFlag() {
            return service.getPref(MACHINE_SETUP_FLAG, false);
        }

        public void setMachineSetupFlag(boolean flag) {
            service.setPref(MACHINE_SETUP_FLAG, flag);
        }

        public boolean getMachineSetupLanguage() {
            return service.getPref(MACHINE_SETUP_LANGUAGE, false);
        }

        public void setMachineSetupLanguage(boolean flag) {
            service.setPref(MACHINE_SETUP_LANGUAGE, flag);
        }

        public boolean getMachineSetup3DP() {
            return service.getPref(MACHINE_SETUP_3DP, false);
        }

        public void setMachineSetup3DP(boolean flag) {
            service.setPref(MACHINE_SETUP_3DP, flag);
        }

        public boolean getMachineSetupLaser() {
            return service.getPref(MACHINE_SETUP_LASER, false);
        }

        public void setMachineSetupLaser(boolean flag) {
            service.setPref(MACHINE_SETUP_LASER, flag);
        }

        public boolean getMachineSetup10WLaser() {
            return service.getPref(MACHINE_SETUP_10W_LASER, false);
        }

        public void setMachineSetup10WLaser(boolean flag) {
            service.setPref(MACHINE_SETUP_10W_LASER, flag);
        }

        public boolean getMachineSetupCNC() {
            return service.getPref(MACHINE_SETUP_CNC, false);
        }

        public void setMachineSetupCNC(boolean flag) {
            service.setPref(MACHINE_SETUP_CNC, flag);
        }

        public boolean getMachineSetupRotaryLaser() {
            return service.getPref(MACHINE_SETUP_ROTARY_LASER, false);
        }

        public void setMachineSetupRotaryLaser(boolean flag) {
            service.setPref(MACHINE_SETUP_ROTARY_LASER, flag);
        }

        public boolean getMachineSetupRotary10WLaser() {
            return service.getPref(MACHINE_SETUP_ROTARY_10W_LASER, false);
        }

        public void setMachineSetupRotary10WLaser(boolean flag) {
            service.setPref(MACHINE_SETUP_ROTARY_10W_LASER, flag);
        }

        public boolean getMachineSetupRotaryCNC() {
            return service.getPref(MACHINE_SETUP_ROTARY_CNC, false);
        }

        public void setMachineSetupRotaryCNC(boolean flag) {
            service.setPref(MACHINE_SETUP_ROTARY_CNC, flag);
        }

        /**
         * Indicates if screen/main controller is just updated.
         */
        public boolean getMachineUpdatedFlag() {
            return service.getPref(MACHINE_UPDATE_FLAG, false);
        }

        public void setMachineUpdatedFlag(boolean flag) {
            service.setPref(MACHINE_UPDATE_FLAG, flag);
        }

        // -- ?

        public String getApiHost() {
            return service.getPref(API_HOST, "https://api.snapmaker.com/");
        }

        public void setApiHost(String apiHost) {
            service.setPref(API_HOST, apiHost);
        }

        public String getLastUpdatePackageVersion() {
            return service.getPref(UPDATE_PACKAGE_VERSION, "");
        }

        public void setLastUpdatePackageVersion(String version) {
            service.setPref(UPDATE_PACKAGE_VERSION, version);
        }

        public boolean getCheckUpdateFlag() {
            return service.getPref(CHECK_UPDATE_FLAG, true);
        }

        public void setCheckUpdateFlag(boolean updateFlag) {
            service.setPref(CHECK_UPDATE_FLAG, updateFlag);
        }

        public boolean getUpdateNotification() {
            return service.getPref(UPDATE_NOTIFICATION, false);
        }

        public void setUpdateNotification(boolean notification) {
            service.setPref(UPDATE_NOTIFICATION, notification);
        }

        public String getLastCheckVersion() {
            return service.getPref(UPDATE_LAST_CHECK_VERSION, "0.0.0.0");
        }

        public void setLastCheckVersion(String version) {
            service.setPref(UPDATE_LAST_CHECK_VERSION, version);
        }

        // -- Laser

        public float getLaserMaterialThickness() {
            return service.getPref(LASER_MATERIAL_THICKNESS, 0f);
        }

        public void setLaserMaterialThickness(float thickness) {
            service.setPref(LASER_MATERIAL_THICKNESS, thickness);
        }

        public float getLaserBottomZ() {
            return service.getPref(LASER_BOTTOM_Z, 0f);
        }

        public void setLaserBottomZ(float z) {
            service.setPref(LASER_BOTTOM_Z, z);
        }

        public float getLaserPlatformZ() {
            return service.getPref(LASER_PLATFORM_Z, 0f);
        }

        public void setLaserPlatformZ(float z) {
            service.setPref(LASER_PLATFORM_Z, z);
        }

        public String getCameraCalibration() {
            return service.getPref(LASER_CAMERA_CALIBRATION, (String) null);
        }

        public void setCameraCalibration(String result) {
            service.setPref(LASER_CAMERA_CALIBRATION, result);
        }

        public float getLaserControlPower() {
            return service.getPref(LASER_CONTROL_POWER, 15f);
        }

        public void setLaserControlPower(float power) {
            service.setPref(LASER_CONTROL_POWER, power);
        }

        // -- 10W Laser
        public String get10WLaserCameraCalibration() {
            return service.getPref(LASER_10W_CAMERA_CALIBRATION, (String) null);
        }

        public void set10WLaserCameraCalibration(String result) {
            service.setPref(LASER_10W_CAMERA_CALIBRATION, result);
        }

        // -- File / Print

        public boolean getPrintLoc() {
            return service.getPref(FILE_LOC, false); // local: true, USB: false
        }

        public void setPrintLoc(boolean isLocal) {
            service.setPref(FILE_LOC, isLocal);
        }

        public int getPrintSource() {
            return service.getPref(PRINT_SOURCE, 0);
        }

        public void setPrintSource(int source) {
            service.setPref(PRINT_SOURCE, source);
        }

        public String getPrintFilePath() {
            return service.getPref(FILE_PATH, "");
        }

        public void setPrintFilePath(String filePath) {
            service.setPref(FILE_PATH, filePath);
        }

        public int getPrintFileTotalLines() {
            return service.getPref(FILE_TOTAL_LINES, 0);
        }

        public void setPrintFileTotalLines(int linesCount) {
            service.setPref(FILE_TOTAL_LINES, linesCount);
        }

        public float getPrintFileEstimatedTime() {
            return service.getPref(FILE_ESTIMATED_TIME, 0f);
        }

        public void setPrintFileEstimatedTime(float estimatedTime) {
            service.setPref(FILE_ESTIMATED_TIME, estimatedTime);
        }

        public int getPrintElapsedTime() {
            return service.getPref(PRINT_ELAPSED_TIME, 0);
        }

        public void setPrintElapsedTime(int time) {
            service.setPref(PRINT_ELAPSED_TIME, time);
        }

        public int getPrintSelectedMode() {
            return service.getPref(PRINT_SELECTED_MODE, 0);
        }

        public void setPrintSelectedMode(int mode) {
            service.setPref(PRINT_SELECTED_MODE, mode);
        }

        public float getPrintOffsetX() {
            return service.getPref(PRINT_OFFSET_X, 0f);
        }

        public void setPrintOffsetX(float offsetX) {
            service.setPref(PRINT_OFFSET_X, offsetX);
        }

        // -- 3DP

        /**
         * Calibration Mode
         * <p>
         * 0 - Auto Mode
         * 1 - Manual Mode
         */
        public int get3DPCalibrationMode() {
            return service.getPref(A3DP_CALIBRATION_MODE, 0);
        }

        public void set3DPCalibrationMode(int mode) {
            service.setPref(A3DP_CALIBRATION_MODE, mode);
        }

        public boolean get3DPFastCalibrationOn() {
            return service.getPref(A3DP_FAST_CALIBRATION_ON, true);
        }

        public void set3DPFastCalibrationOn(boolean on) {
            service.setPref(A3DP_FAST_CALIBRATION_ON, on);
        }

        public int get3DPCalibrationGrid() {
            return service.getPref(A3DP_CALIBRATION_GRID, 3);
        }

        public void set3DPCalibrationGrid(int grid) {
            service.setPref(A3DP_CALIBRATION_GRID, grid);
        }

        public boolean get3DPCalibrationHeatedLevelingOn() {
            return service.getPref(A3DP_CALIBRATION_HEATED_LEVELING_ON, false);
        }

        public void set3DPCalibrationHeatedLevelingOn(boolean on) {
            service.setPref(A3DP_CALIBRATION_HEATED_LEVELING_ON, on);
        }

        public float get3DPCalibrationHeatedUpTemperature() {
            return service.getPref(A3DP_CALIBRATION_HEATED_UP_TEMPERATURE, 70.0f);
        }

        public void set3DPCalibrationHeatedUpTemperature(float temperature) {
            service.setPref(A3DP_CALIBRATION_HEATED_UP_TEMPERATURE, temperature);
        }

        // -- Laser

        public int getLaserCalibrationMode() {
            return service.getPref(LASER_CALIBRATION_MODE, 0);
        }

        public void setLaserCalibrationMode(int mode) {
            service.setPref(LASER_CALIBRATION_MODE, mode);
        }

        public int getLaser4AxisCalibrationMode() {
            return service.getPref(LASER_4AXIS_CALIBRATION_MODE, 1);
        }

        public void setLaser4AxisCalibrationMode(int mode) {
            service.setPref(LASER_4AXIS_CALIBRATION_MODE, mode);
        }


        public boolean getLaserCameraLightOn() {
            return service.getPref(LASER_CAMERA_LIGHT_ON, true);
        }

        public void setLaserCameraLightOn(boolean cameraLightOn) {
            service.setPref(LASER_CAMERA_LIGHT_ON, cameraLightOn);
        }

        // -- ADD ONS
        public boolean getAirPurifier3DPAutoFlag() {
            return service.getPref(ADD_ON_AIR_PURIFIER_3DP_AUTO_ON_FLAG, false);
        }

        public void setAirPurifier3DPAutoFlag(boolean flag) {
            service.setPref(ADD_ON_AIR_PURIFIER_3DP_AUTO_ON_FLAG, flag);
        }

        public boolean getAirPurifierLaserAutoFlag() {
            return service.getPref(ADD_ON_AIR_PURIFIER_LASER_AUTO_ON_FLAG, true);
        }

        public void setAirPurifierLaserAutoFlag(boolean flag) {
            service.setPref(ADD_ON_AIR_PURIFIER_LASER_AUTO_ON_FLAG, flag);
        }

        public boolean getAirPurifierCNCAutoTurnOnFlag() {
            return service.getPref(ADD_ON_AIR_PURIFIER_CNC_AUTO_ON_FLAG, false);
        }

        public void setAirPurifierCNCAutoTurnOnFlag(boolean flag) {
            service.setPref(ADD_ON_AIR_PURIFIER_CNC_AUTO_ON_FLAG, flag);
        }

        public boolean getAirPurifierAutoTurnOffFlag() {
            return service.getPref(ADD_ON_AIR_PURIFIER_AUTO_OFF_FLAG, true);
        }

        public void setAirPurifierAutoTurnOffFlag(boolean flag) {
            service.setPref(ADD_ON_AIR_PURIFIER_AUTO_OFF_FLAG, flag);
        }

        public boolean getEnclosureAutoLightingOn() {
            return service.getPref(ADD_ON_ENCLOSURE_AUTO_LIGHTING_ON_FLAG, true);
        }

        public void setEnclosureAutoLightingOn(boolean flag) {
            service.setPref(ADD_ON_ENCLOSURE_AUTO_LIGHTING_ON_FLAG, flag);
        }

        // -- Settings
        public boolean getFirebaseAnalyticsFlag() {
            return service.getPref(SETTINGS_FIREBASE_ANALYTICS_FLAG, true);
        }

        public void setFirebaseAnalyticsFlag(boolean enabled) {
            service.setPref(SETTINGS_FIREBASE_ANALYTICS_FLAG, enabled);
        }

        public int getUserSelectedLanguage() {
            return service.getPref(SETTINGS_USER_SELECTED_LANGUAGE, MultiLanguageManager.LANGUAGE_DEFAULT);
        }

        public void setUserSelectedLanguage(int selectedLanguage) {
            service.setPref(SETTINGS_USER_SELECTED_LANGUAGE, selectedLanguage);
        }

        public int getHeaderOnlineSyncID() {
            return service.getPref(HEADER_ONLINE_SYNC_ID, -1);
        }

        public void setHeaderOnlineSyncID(int leaserHeaderID) {
            service.setPref(HEADER_ONLINE_SYNC_ID, leaserHeaderID);
        }

        // -- Debug
        public boolean getDebugFlag() {
            return service.getPref(DEBUG_FLAG, false);
        }

        public void setDebugFlag(boolean flag) {
            service.setPref(DEBUG_FLAG, flag);
        }

        public float getLaserThicknessS1Plus() {
            return service.getPref(LASER_PARAM_S1_PLUS_VALUE, -1f);
        }

        public float getLaserThicknessS2Plus() {
            return service.getPref(LASER_PARAM_S2_PLUS_VALUE, -1f);
        }

        public void setLaserParamS1Plus(float s1Plus) {
            service.setPref(LASER_PARAM_S1_PLUS_VALUE, s1Plus);
        }

        public void setLaserParamS2Plus(float s2Plus) {
            service.setPref(LASER_PARAM_S2_PLUS_VALUE, s2Plus);
        }

        public int getAppMachineProtocol() {
            return service.getPref(DEBUG_APP_MACHINE_PROTOCOL, 0);
        }

        public void setAppMachineProtocol(int protocol) {
            service.setPref(DEBUG_APP_MACHINE_PROTOCOL, protocol);
        }

        public void setMockEnabled(boolean enabled) {
            service.setPref(DEBUG_MOCK_ENABLED, enabled);
        }

        // Mock can only be enabled when BuildConfig.DEBUG is true.
        public boolean getMockEnabled() {
            return service.getPref(DEBUG_MOCK_ENABLED, false);
        }

        public int getControllerModel() {
            return service.getPref(CONTROLLER_MODEL, 0);
        }

        public void setControllerModel(int controllerModel) {
            service.setPref(CONTROLLER_MODEL, controllerModel);
        }

        public void debugSetMachineSeries(int series) {
            service.setPref(DEBUG_MACHINE_SERIES, series);
        }

        public void debugSetMachineModel(int model) {
            service.setPref(DEBUG_MACHINE_MODEL, model);
        }

        public int getDebugMachineSeries() {
            return service.getPref(DEBUG_MACHINE_SERIES, IMachine.MachineSeries.J);
        }

        public int getDebugMachineModel() {
            return service.getPref(DEBUG_MACHINE_MODEL, IMachine.MachineModel.A150);
        }

        public Set<String> getDebugModuleList() {
            return service.getPref(DEBUG_MODULE_LIST, (Set<String>) null);
        }

        public void setDebugModuleList(Set<String> debugModuleList) {
            service.setPref(DEBUG_MODULE_LIST, debugModuleList);
        }

        public boolean getHeatedBedLeveLingIsAuxiliary() {
            return service.getPref(J1_HEATED_BED_LEVELING_IS_AUXILIARY, true);
        }

        public void setHeatedBedLeveLingIsAuxiliary(boolean isAuxiliary) {
            service.setPref(J1_HEATED_BED_LEVELING_IS_AUXILIARY, isAuxiliary);
        }

        public boolean getZOffsetCalibrationIsAuxiliary() {
            return service.getPref(J1_Z_OFFSET_CALIBRATION_IS_AUXILIARY, true);
        }

        public void setZOffsetCalibrationIsAuxiliary(boolean isAuxiliary) {
            service.setPref(J1_Z_OFFSET_CALIBRATION_IS_AUXILIARY, isAuxiliary);
        }

        public boolean getXYOffsetCalibrationIsAuxiliary() {
            return service.getPref(J1_XY_OFFSET_CALIBRATION_IS_AUXILIARY, true);
        }

        public void setXYOffsetCalibrationIsAuxiliary(boolean isAuxiliary) {
            service.setPref(J1_XY_OFFSET_CALIBRATION_IS_AUXILIARY, isAuxiliary);
        }


        public int getA400LevelingBedCalibrationMode() {
            return service.getPref(A400_LEVELING_BED_CALIBRATION_MODE, 0);
        }

        public void setA400LevelingBedCalibrationMode(int mode) {
            service.setPref(A400_LEVELING_BED_CALIBRATION_MODE, mode);
        }

        public int getA400CentralAxisCalibrationMaterialType() {
            return service.getPref(A400_CENTRAL_AXIS_CALIBRATION_MATERIAL_TYPE, 0);
        }

        public void setA400CentralAxisCalibrationMaterialType(int type) {
            service.setPref(A400_CENTRAL_AXIS_CALIBRATION_MATERIAL_TYPE, type);
        }

        public int getA400LevelingBedCalibrationGrid() {
            return service.getPref(A400_LEVELING_BED_CALIBRATION_GRID, 5);
        }

        public void setA400LevelingBedCalibrationGrid(int grid) {
            service.setPref(A400_LEVELING_BED_CALIBRATION_GRID, grid);
        }

        public int getA400LevelingBedCalibrationBedTemperature() {
            return service.getPref(A400_LEVELING_BED_CALIBRATION_BED_TEMPERATURE, 70);
        }

        public void setA400LevelingBedCalibrationBedTemperature(int temperature) {
            service.setPref(A400_LEVELING_BED_CALIBRATION_BED_TEMPERATURE, temperature);
        }

        public int getA400LevelingZCalibrationMode() {
            return service.getPref(A400_LEVELING_Z_CALIBRATION_MODE, 2);
        }

        public void setA400LevelingZCalibrationMode(int mode) {
            service.setPref(A400_LEVELING_Z_CALIBRATION_MODE, mode);
        }

        public int getA400BevelingXYMaterialSelection() {
            return service.getPref(A400_LEVELING_XY_CALIBRATION_MATERIAL_SELECTION, 0);
        }

        public void setA400BevelingXYMaterialSelection(int materialSelection) {
            service.setPref(A400_LEVELING_XY_CALIBRATION_MATERIAL_SELECTION, materialSelection);
        }

        public int getA400LevelingXYCalibrationLeftPrintingTemperature() {
            return service.getPref(A400_LEVELING_XY_CALIBRATION_LEFT_PRINT_TEMPERATURE, 0);
        }

        public void setA400LevelingXYCalibrationLeftPrintingTemperature(int leftPrintingTemperature) {
            service.setPref(A400_LEVELING_XY_CALIBRATION_LEFT_PRINT_TEMPERATURE, leftPrintingTemperature);
        }

        public int getA400LevelingXYCalibrationRightPrintingTemperature() {
            return service.getPref(A400_LEVELING_XY_CALIBRATION_RIGHT_PRINT_TEMPERATURE, 0);
        }

        public void setA400LevelingXYCalibrationRightPrintingTemperature(int rightPrintingTemperature) {
            service.setPref(A400_LEVELING_XY_CALIBRATION_RIGHT_PRINT_TEMPERATURE, rightPrintingTemperature);
        }

        public int getA400LevelingXYCalibrationLeftStandbyTemperature() {
            return service.getPref(A400_LEVELING_XY_CALIBRATION_LEFT_STANDBY_TEMPERATURE, 0);
        }

        public void setA400LevelingXYCalibrationLeftStandbyTemperature(int leftStandbyTemperature) {
            service.setPref(A400_LEVELING_XY_CALIBRATION_LEFT_STANDBY_TEMPERATURE, leftStandbyTemperature);
        }

        public int getA400LevelingXYCalibrationRightStandbyTemperature() {
            return service.getPref(A400_LEVELING_XY_CALIBRATION_RIGHT_STANDBY_TEMPERATURE, 0);
        }

        public void setA400LevelingXYCalibrationRightStandbyTemperature(int rightStandbyTemperature) {
            service.setPref(A400_LEVELING_XY_CALIBRATION_RIGHT_STANDBY_TEMPERATURE, rightStandbyTemperature);
        }

        public int getA400LevelingXYCalibrationBedPrintingTemperature() {
            return service.getPref(A400_LEVELING_XY_CALIBRATION_BED_PRINT_TEMPERATURE, 0);
        }

        public void setA400LevelingXYCalibrationBedPrintingTemperature(int bedPrintingTemperature) {
            service.setPref(A400_LEVELING_XY_CALIBRATION_BED_PRINT_TEMPERATURE, bedPrintingTemperature);
        }

        public int getA400ManualToolCalibrationMode() {
            return service.getPref(A400_CNC_MANUAL_TOOL_CALIBRATION, 0);
        }

        public void setA400ManualToolCalibrationMode(int manualToolCalibrationMode) {
            service.setPref(A400_CNC_MANUAL_TOOL_CALIBRATION, manualToolCalibrationMode);
        }

        public int getLaserPrintXYOriginModel() {
            return service.getPref(A400_LASER_PRINT_XY_ORIGIN_MODE, 0);
        }

        public void setLaserPrintXYOriginModel(int model) {
            service.setPref(A400_LASER_PRINT_XY_ORIGIN_MODE, model);
        }

        public int getLaserPrintZOriginModel() {
            return service.getPref(A400_LASER_PRINT_XY_ORIGIN_MODE, 2);
        }

        public void setLaserPrintZOriginMode(int mode) {
            service.setPref(A400_LASER_PRINT_XY_ORIGIN_MODE, mode);
        }

        public boolean getGuideTemperatureSelfCheck() {
            return service.getPref(GUIDE_TEMPERATURE_SELF_CHECK_STATE, false);
        }

        public void setGuideTemperatureSelfCheck(boolean state) {
            service.setPref(GUIDE_TEMPERATURE_SELF_CHECK_STATE, state);
        }

        public boolean getGuideCalibration() {
            return service.getPref(GUIDE_CALIBRATION, false);
        }

        public void setGuideCalibration(boolean state) {
            service.setPref(GUIDE_CALIBRATION, state);
        }

        public boolean getGuideLevelingBed() {
            return service.getPref(GUIDE_LEVELING_BED, false);
        }

        public void setGuideLevelingBed(boolean state) {
            service.setPref(GUIDE_LEVELING_BED, state);
        }

        public boolean getGuideLevelingXY() {
            return service.getPref(GUIDE_LEVELING_XY, false);
        }

        public void setGuideLevelingXY(boolean state) {
            service.setPref(GUIDE_LEVELING_XY, state);
        }

        public boolean getGuideLevelingZ() {
            return service.getPref(GUIDE_LEVELING_Z, false);
        }

        public void setGuideLevelingZ(boolean state) {
            service.setPref(GUIDE_LEVELING_Z, state);
        }

        public boolean getGuideCheckPrint() {
            return service.getPref(GUIDE_CHECK_PRINT, false);
        }

        public void setGuideCheckPrint(boolean state) {
            service.setPref(GUIDE_CHECK_PRINT, state);
        }

        public boolean getGuideVibrationCompensation() {
            return service.getPref(GUIDE_VIBRATION_COMPENSATION, false);
        }

        public void setGuideVibrationCompensation(boolean state) {
            service.setPref(GUIDE_VIBRATION_COMPENSATION, state);
        }

        public float getJ1LeftTargetTemp() {
            return service.getPref(J1_EXTRUDER_TARGET_TEMP_L, 180f);
        }

        public void setJ1LeftTargetTemp(float temp) {
            service.setPref(J1_EXTRUDER_TARGET_TEMP_L, temp);
        }

        public float getJ1RightTargetTemp() {
            return service.getPref(J1_EXTRUDER_TARGET_TEMP_R, 180f);
        }

        public void setJ1RightTargetTemp(float temp) {
            service.setPref(J1_EXTRUDER_TARGET_TEMP_R, temp);
        }

        public String getCameraCalibrationTakePhotoVector() {
            return service.getPref(CAMERA_CALIBRATION_TAKE_PHOTO_VECTOR, (String) null);
        }

        public void setCameraCalibrationTakePhotoVector(String vectorStr) {
            service.setPref(CAMERA_CALIBRATION_TAKE_PHOTO_VECTOR, vectorStr);
        }

        public void setLastConnectedAPSSID(String connectedSSID) {
            Logger.d("Wifi: saving connected ssid: %s", connectedSSID);
            service.setPref(LAST_CONNECTED_AP_SSID, connectedSSID);
        }

        public void setLastConnectedAPPwd(String password) {
            Logger.d("Wifi: saving connected pwd");
            service.setPref(LAST_CONNECTED_AP_PWD, password);
        }

        public String getLastConnectedApSSID() {
            return service.getPref(LAST_CONNECTED_AP_SSID, "");
        }

        public String getLastConnectedApPwd() {
            return service.getPref(LAST_CONNECTED_AP_PWD, "");
        }

        public boolean getRemoteAllowConnection() {
            return service.getPref(SETTING_REMOTE_ALLOW_CONNECTION, true);
        }

        public void setRemoteAllowConnection(boolean isAllow) {
            service.setPref(SETTING_REMOTE_ALLOW_CONNECTION, isAllow);
        }

        public boolean getRemoteSafeMode() {
            return service.getPref(SETTING_REMOTE_SAFE_MODE, true);
        }

        public void setRemoteSafeMode(boolean isAllow) {
            service.setPref(SETTING_REMOTE_SAFE_MODE, isAllow);
        }

        public int getConnectionVerification() {
            return service.getPref(SETTING_REMOTE_CONNECTION_VERIFICATION, 0);
        }

        public void setConnectionVerification(int item) {
            service.setPref(SETTING_REMOTE_CONNECTION_VERIFICATION, item);

        }

        public int getCncSelectModel() {
            return service.getPref(A400_CNC_SELECT_MODEL, 0);
        }

        public void setCncSelectModel(int item) {
            service.setPref(A400_CNC_SELECT_MODEL, item);

        }

        public Set<String> getRemoteTokens() {
            HashSet<String> hashSet = new HashSet<String>();
            return service.getPref(REMOTE_TOKEN, hashSet);
        }

        public void setRemoteTokens(Set<String> tokens) {
            service.setPref(REMOTE_TOKEN, tokens);
        }

        public boolean getUSBFactoryModeOn() {
            return service.getPref(FACTORY_USB_OFF, false);
        }

        public void setUSBFactoryModeOn(boolean isOn) {
            service.setPref(FACTORY_USB_OFF, isOn);
        }

        public boolean getMachineDeveloper() {
            return service.getPref(MACHINE_SETUP_DEVELOPER, false);
        }

        public void setMachineDeveloper(boolean isOn) {
            service.setPref(MACHINE_SETUP_DEVELOPER, isOn);
        }

        public void setShowUpdateResultWhenHome(boolean show) {
            service.setPref(SHOW_UPDATE_RESULT_ON_NEXT_STARTUP, show);
        }

        public boolean getShowUpdateResultWhenHome() {
            return service.getPref(SHOW_UPDATE_RESULT_ON_NEXT_STARTUP, false);
        }

        public void setNeedQueryMachineError(boolean need) {
            service.setPref(NEED_QUERY_MACHINE_ERROR, need);
        }

        public boolean getNeedQueryMachineError() {
            return service.getPref(NEED_QUERY_MACHINE_ERROR, true);
        }

        public void setScreenSaverEnabled(boolean enabled) {
            service.setPref(SCREEN_SAVER_ENABLED, enabled);
        }

        public boolean getScreenSaverEnabled() {
            return service.getPref(SCREEN_SAVER_ENABLED, true);
        }

        public void setScreenSaverDelayTime(int seconds) {
            service.setPref(SCREEN_SAVER_DELAY_TIME, seconds);
        }

        /**
         * @return time in seconds
         */
        public int getScreenSaverDelayTime() {
            return service.getPref(SCREEN_SAVER_DELAY_TIME, 900);
        }
    }
}
