package fabscreen.platform.base.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;

import fabscreen.platform.base.BuildConfig;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;

/**
 * An ARouter wrap.
 * <p>
 * Use like this: ServiceContainer.getInstance().getService(IRouter.class).routeToExperimentPage();
 */
public class BaseRouter implements IRouter, IServiceIdentifier {

    protected Postcard mPostcard;

    /**
     * Make constructor private to avoid calling directly.
     */
    @Keep
    protected BaseRouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(ServiceContainer.getInstance().getService(IAppService.class).getApp());
    }

//    private static class RouterHolder {
//        private static final Router INSTANCE = new Router();
//    }
//
//    @Deprecated
//    private Router() {
//
//    }

    // TODO: 2021/10/11 check if path added
    @Override
    public BaseRouter routeWithClassPath(@RoutePath.Path String path) {
        mPostcard = createPostcard(path);
        return this;
    }

    @Override
    public BaseRouter routeWithClassPath(@RoutePath.Path String path, boolean b) {
        mPostcard = createPostcard(path).withBoolean("bool", b);
        return this;
    }

//    public static Router getInstance() {
//        return RouterHolder.INSTANCE;
//    }

    @Override
    public IRouter routeToWelcome() {
        mPostcard = createPostcard(RoutePath.WELCOME_INDEX);
        return this;
    }

    @Override
    public IRouter routeToWelcome(boolean showGuideClose) {
        return null;
    }

    @Override
    public BaseRouter routeToMain() {
        mPostcard = createPostcard(RoutePath.HOME_MAIN);
        return this;
    }

    @Override
    public BaseRouter routeToHome() {
        mPostcard = createPostcard(RoutePath.HOME_INDEX);
        return this;
    }

    /**
     * @param event 1: change language
     */
    @Override
    public IRouter routeToHome(int event) {
        mPostcard = createPostcard(RoutePath.HOME_INDEX).withInt("event", event);
        return this;
    }

    @Override
    public IRouter routeToGuideMilestone() {
        mPostcard = createPostcard(RoutePath.GUIDE_A400_MILESTONE);
        return this;
    }

    @Override
    public IRouter routeToGuide3DP() {
        return routeToGuide3DP(true);
    }

    @Override
    public IRouter routeToGuide3DP(boolean showClose) {
        mPostcard = createPostcard(RoutePath.GUIDE_j1).withBoolean("showClose", showClose);
        return this;
    }

    @Override
    public BaseRouter routeToGuideLaser() {
        mPostcard = createPostcard(RoutePath.GUIDE_LASER);
        return this;
    }

    @Override
    public BaseRouter routeToGuide10WLaser() {
        mPostcard = createPostcard(RoutePath.GUIDE_10W_LASER);
        return this;
    }

    @Override
    public BaseRouter routeToGuideCNC() {
        mPostcard = createPostcard(RoutePath.GUIDE_CNC);
        return this;
    }

    @Override
    public BaseRouter routeToGuideRotaryLaser() {
        mPostcard = createPostcard(RoutePath.GUIDE_ROTARY_LASER);
        return this;
    }

    @Override
    public BaseRouter routeToGuideRotaryCNC() {
        mPostcard = createPostcard(RoutePath.GUIDE_ROTARY_CNC);
        return this;
    }

    @Override
    public BaseRouter routeToControlPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CONTROL_J1);
        return this;
    }

    // BrowseActivity
    @Override
    public BaseRouter routeToFilesPage(int fileType) {
        mPostcard = createPostcard(RoutePath.FILE_BROWSER).withInt("file_type", fileType);
        return this;
    }

    // TODO: 2021/10/11 test params
    @Override
    public BaseRouter routeToPreviewPage(boolean isLocal, String filePath) {
        mPostcard = createPostcard(RoutePath.PRINT_PREVIEW)
                .withBoolean(IntentKeys.IS_LOCAL, isLocal)
                .withString(IntentKeys.FILE_PATH, filePath);
        return this;
    }

    @Override
    public BaseRouter routeToPrintSettingsPage() {
        mPostcard = createPostcard(RoutePath.PRINT_PREVIEW_SETTINGS);
        return this;
    }

    @Override
    public BaseRouter routeToPrintPage() {
        mPostcard = createPostcard(RoutePath.PRINT_PRINT);
        return this;
    }

    @Override
    public BaseRouter routeToPrepareLaserPage() {
        mPostcard = createPostcard(RoutePath.PREPARE_LASER);
        return this;
    }

    @Override
    public BaseRouter routeToPrepareCNCPage() {
        mPostcard = createPostcard(RoutePath.PREPARE_CNC);
        return this;
    }

    /**
     * Compose params for going to CalibrationActivity.
     *
     * @param needBackHome Whether we need to go back home(or directly finish) after diving into
     *                     CalibrationActivity.
     */
    @Override
    public BaseRouter routeTo3DPCalibrationPage(boolean needBackHome) {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_3DP)
                .withBoolean(IntentKeys.NEED_BACK_HOME, needBackHome);
        return this;
    }

    @Override
    public BaseRouter routeToCalibrationPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_J1);
        return this;
    }

    @Override
    public BaseRouter routeToCalibrationPage(boolean isGuide) {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_J1)
                .withBoolean("is_guide", isGuide);
        return this;
    }

    @Override
    public BaseRouter routeToLaserCalibrationPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_LASER);
        return this;
    }

    @Override
    public BaseRouter routeTo10wThicknessCalibrationPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_10W_LASER_THICKNESS_MEASURE);
        return this;
    }

    @Override
    public BaseRouter routeToCameraCalibration() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_10W_CAMERA_CALIBRATION);
        return this;
    }

    @Override
    public BaseRouter routeToCNCOriginAssistantPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_CNC_ORIGIN);
        return this;
    }

    @Override
    public BaseRouter routeToCNCBitAssistantPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_CNC_BIT);
        return this;
    }

    @Override
    public BaseRouter routeToSettingsPage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_J1_INDEX);
        return this;
    }

    @Override
    public IRouter routeToSettingsPage(int destination) {
        mPostcard = createPostcard(RoutePath.SETTINGS_J1_INDEX).withInt("destination", destination);
        return this;
    }

    @Override
    public BaseRouter routeToAboutPage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_ABOUT);
        return this;
    }

    @Override
    public BaseRouter routeToSettingsFirmwarePage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_FIRMWARE_S30);
        return this;
    }

    @Override
    public BaseRouter routeToRemotePage() {
        mPostcard = createPostcard(RoutePath.S30_REMOTE_INDEX);
        return this;
    }

    @Override
    public BaseRouter routeToEmergencyStopPage(boolean isTriggeredOnPowerUp) {
        mPostcard = createPostcard(RoutePath.ADDONS_EMERGENCY_STOP)
                .withBoolean(IntentKeys.IS_TRIGGERED_ON_POWER_UP, isTriggeredOnPowerUp);
        return this;
    }

    @Override
    public BaseRouter routeToFactoryActivity() {
        mPostcard = createPostcard(RoutePath.SETTINGS_FACTORY);
        return this;
    }

    @Override
    public BaseRouter routeToUpdateActivity(String filePath, Boolean isLocal) {
        mPostcard = createPostcard(RoutePath.SETTINGS_UPDATE)
                .withString(IntentKeys.FILE_PATH, filePath)
                .withBoolean(IntentKeys.IS_LOCAL, isLocal);
        return this;
    }

    @Override
    public IRouter routeToBlockSetup() {
        mPostcard = createPostcard(RoutePath.TOOLS_BLOCK_SETUP);
        return this;
    }

    @Override
    public IRouter routeToFilamentSetup() {
        mPostcard = createPostcard(RoutePath.TOOLS_LOAD_FILAMENT);
        return this;
    }

    @Override
    public IRouter routeToZCalibrationSetup() {
        mPostcard = createPostcard(RoutePath.TOOLS_SETUP_CALIBRATION);
        return this;
    }

    public IRouter routeToZCalibration() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_AUTOMATIC);
        return this;
    }

    @Override
    public IRouter routeToXYCalibrationSetup() {
        mPostcard = createPostcard(RoutePath.TOOLS_SETUP_XY_CALIBRATION);
        return this;
    }

    @Override
    public IRouter routeToXYCalibration() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_XY);
        return this;
    }

    @Override
    public IRouter routeToHeatedBedLevelingSetup() {
        mPostcard = createPostcard(RoutePath.TOOLS_SETUP_SINGLE_SINGLE_BED_LEVELING);
        return this;
    }

    @Override
    public IRouter routeToHeatedBedLeveling() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_AUTO);
        return this;
    }

    @Override
    public IRouter routeToSingleSingleFilamentSetup() {
        mPostcard = createPostcard(RoutePath.TOOLS_SETUP_SINGLE_SINGLE_FILAMENT);
        return this;
    }

    @Override
    public IRouter routeToAutoMeasureCalibration() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_LASER_THICKNESS_MEASURE_CALIBRATION);
        return this;
    }

    @Override
    public IRouter routeTo10WCameraCaptureCalibration(Bundle bundle) {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_S20_10W_CAMERA_CALIBRATION).withBundle("page_data", bundle);
        return this;
    }

    @Override
    public BaseRouter routeToSetupIntro(Bundle bundle) {
        mPostcard = createPostcard(RoutePath.TOOLS_SETUP_COMMON_INTRO).withBundle("page_data", bundle);
        return this;
    }

    @Override
    public BaseRouter routeToExperimentPage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_EXPERIMENT);
        return this;
    }

    @Override
    public IRouter routeToManualFocusCalibration() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_LASER_FOCUS_CALIBRATION);
        return this;
    }

    @Override
    public IRouter routeToCentralAxisCalibration() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS);
        return this;
    }

    @Override
    public IRouter routeToCalibrationComplete(int caliType) {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_COMPLETE).withInt("calibration_type", caliType);
        return this;
    }

    @Override
    public IRouter routeToReplaceModules() {
        mPostcard = createPostcard(RoutePath.A400_SETTINGS_REPLACE_MODULE);
        return this;
    }

    @Override
    public IRouter routeToReplaceHotend() {
        mPostcard = createPostcard(RoutePath.A400_SETTINGS_REPLACE_HOTEND);
        return this;
    }

    @Override
    public IRouter routeToGreenScreen() {
        mPostcard = createPostcard(RoutePath.A400_GREEN_SCREEN);
        return this;
    }

    @Override
    public IRouter routeToUpdateSuccess(int event, String screenAPKPath) {
        return null;
    }

    @Override
    public IRouter routeToUpdateModules() {
        return null;
    }

    @Override
    public IRouter routeToUpdateInProgress(String updateFilePath, boolean isLocal) {
        return null;
    }

    @Override
    public IRouter routeToRecoveryMode() {
        return null;
    }

    @Override
    public IRouter routeToPrintSetting() {
        mPostcard = createPostcard(RoutePath.PRINT_SETTING);
        return this;
    }

    @Override
    public Fragment getFragmentInstance(String fragmentPath) {
        return (Fragment) ARouter.getInstance().build(fragmentPath).navigation();
    }

    //
//    public void startUpdatingApp(Context context) {
//        if (context != null) {
//            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.snapmaker.updating");
//            context.startActivity(intent);
//        }
//    }

    @Override
    public BaseRouter backHome() {
        mPostcard = createPostcard(RoutePath.HOME_INDEX)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .withBoolean(Constants.KEY_IS_FORCE_BACK_HOME, true);
        return this;
    }

    @Override
    public BaseRouter backLandHome() {
        mPostcard = createPostcard(RoutePath.HOME_LAND)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .withBoolean(Constants.KEY_IS_FORCE_BACK_HOME, true);
        return this;
    }

    @Override
    public void start(Context context) {
        mPostcard.navigation(context);
    }

    @Override
    public void start(Context context, int flags) {
        mPostcard.addFlags(flags).navigation(context);
    }

    @Override
    public void start(Context context, Intent intent) {
        if (context == null) return;
        context.startActivity(intent);
    }

    @Override
    public void startAndClear(Context context) {
        mPostcard.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).navigation(context);
    }

    /**
     * startForResult() for Fragment.
     * <p>
     * ARouter doesn't support fragment's startActivityForResult() method, so we retrieve the Intent
     * and call fragment.startActivityForResult() by ourself.
     * <p>
     * Refer to <a href="https://github.com/alibaba/ARouter/issues/397#issuecomment-399655843">this issue comment</a>.
     */
    @Override
    public void startForResult(Fragment fragment, int requestCode) {
        LogisticsCenter.completion(mPostcard);
        Intent intent = new Intent(fragment.getActivity(), mPostcard.getDestination());
        intent.putExtras(mPostcard.getExtras());
        fragment.startActivityForResult(intent, requestCode);
    }

    public void startForResult(Activity activity, ActivityResultLauncher<Intent> launcher) {
        LogisticsCenter.completion(mPostcard);
        Intent intent = new Intent(activity, mPostcard.getDestination());
        intent.putExtras(mPostcard.getExtras());
        launcher.launch(intent);
    }

    @Override
    public void startForResult(Activity activity, int requestCode) {
        mPostcard.navigation(activity, requestCode);
    }

    protected Postcard createPostcard(@RoutePath.Path String destinationPath) {
        return ARouter.getInstance().build(destinationPath);
    }
}
