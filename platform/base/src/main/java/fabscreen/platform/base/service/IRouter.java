package fabscreen.platform.base.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.platform.base.RoutePath;

public interface IRouter {
    IRouter routeWithClassPath(String path);

    IRouter routeWithClassPath(String path, boolean b);

    IRouter routeToWelcome();

    IRouter routeToWelcome(boolean showGuideClose);

    IRouter routeToMain();

    IRouter routeToHome();

    /**
     * route home with event
     *
     * @param event 0: default, do nothing 1: update complete
     */
    IRouter routeToHome(int event);

    IRouter routeToGuideMilestone();

    IRouter routeToGuide3DP();

    IRouter routeToGuide3DP(boolean showClose);

    IRouter routeToGuideLaser();

    IRouter routeToGuide10WLaser();

    IRouter routeToGuideCNC();

    IRouter routeToGuideRotaryLaser();

    IRouter routeToGuideRotaryCNC();

    IRouter routeToControlPage();

    // TODO: Update to file manager type
    IRouter routeToFilesPage(int fileType);

    IRouter routeToPreviewPage(boolean isLocal, String filePath);

    IRouter routeToPrintSettingsPage();

    IRouter routeToPrintPage();

    IRouter routeToPrepareLaserPage();

    IRouter routeToPrepareCNCPage();

    IRouter routeTo3DPCalibrationPage(boolean needBackHome);

    IRouter routeToCalibrationPage();

    IRouter routeToCalibrationPage(boolean isGuide);

    IRouter routeToLaserCalibrationPage();

    IRouter routeTo10wThicknessCalibrationPage();

    IRouter routeToCameraCalibration();

    IRouter routeToCNCOriginAssistantPage();

    IRouter routeToCNCBitAssistantPage();

    IRouter routeToSettingsPage();

    IRouter routeToSettingsPage(int destination);

    IRouter routeToAboutPage();

    IRouter routeToSettingsFirmwarePage();

    IRouter routeToRemotePage();

    IRouter routeToEmergencyStopPage(boolean isTriggeredOnPowerUp);

    IRouter routeToFactoryActivity();

    @Deprecated
    IRouter routeToUpdateActivity(String filePath, Boolean isLocal);

    IRouter routeToBlockSetup();

    IRouter routeToExperimentPage();

    IRouter backHome();

    IRouter backLandHome();

    void start(Context context);

    void start(Context context, int flags);

    void start(Context context, Intent intent);

    void startAndClear(Context context);

    void startForResult(Fragment fragment, int requestCode);

    void startForResult(Activity activity, int requestCode);

    IRouter routeToFilamentSetup();

    IRouter routeToZCalibrationSetup();

    IRouter routeToZCalibration();

    IRouter routeToXYCalibrationSetup();

    IRouter routeToXYCalibration();

    IRouter routeToHeatedBedLevelingSetup();

    IRouter routeToHeatedBedLeveling();

    IRouter routeToSingleSingleFilamentSetup();

    IRouter routeToAutoMeasureCalibration();

    IRouter routeTo10WCameraCaptureCalibration(Bundle bundle);

    BaseRouter routeToSetupIntro(Bundle bundle);

    Fragment getFragmentInstance(@RoutePath.Path String fragmentPath);

    IRouter routeToManualFocusCalibration();

    IRouter routeToCentralAxisCalibration();

    IRouter routeToCalibrationComplete(int caliType);

    IRouter routeToReplaceModules();

    IRouter routeToReplaceHotend();

    IRouter routeToGreenScreen();

    /**
     * @param event         0:bigbin, 1:embin, 2:startup
     * @param screenAPKPath the apk to install for screen updating
     */
    IRouter routeToUpdateSuccess(int event, @Nullable String screenAPKPath);

    IRouter routeToUpdateModules();

    /**
     * @param updateFilePath big bin file absolute path.
     * @param isLocal        is in local(true) or external(false).
     */
    IRouter routeToUpdateInProgress(String updateFilePath, boolean isLocal);

    IRouter routeToRecoveryMode();

    IRouter routeToPrintSetting();

    class IntentKeys {
        public static final String IS_LOCAL = "is_local";
        public static final String FILE_PATH = "file_path";
        public static final String NEED_BACK_HOME = "need_back_home";
        public static final String IS_TRIGGERED_ON_POWER_UP = "is_triggered_on_power_up";
    }
}
