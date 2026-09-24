package fabscreen.platform.core.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.launcher.ARouter;

import fabscreen.platform.base.BuildConfig;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.BaseRouter;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;

public class J1Router extends BaseRouter implements IRouter, IServiceIdentifier {
    @Keep
    public J1Router() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(ServiceContainer.getInstance().getService(IAppService.class).getApp());
    }

    @Override
    public J1Router routeWithClassPath(String path) {
        mPostcard = createPostcard(path);
        return this;
    }

    @Override
    public J1Router routeWithClassPath(@RoutePath.Path String path, boolean b) {
        mPostcard = createPostcard(path).withBoolean("bool", b);
        return this;
    }

    @Override
    public IRouter routeToWelcome() {
        return routeToWelcome(true);
    }

    @Override
    public IRouter routeToWelcome(boolean showGuideClose) {
        mPostcard = createPostcard(RoutePath.WELCOME_J1).withBoolean("showGuideClose", showGuideClose);
        return this;
    }

    @Override
    public J1Router routeToMain() {
        return null;
    }

    @Override
    public J1Router routeToHome() {
        mPostcard = createPostcard(RoutePath.J1_INDEX);
        return this;
    }

    @Override
    public IRouter routeToHome(int event) {
        mPostcard = createPostcard(RoutePath.J1_INDEX).withInt("event", event);
        return this;
    }

    @Override
    public J1Router routeToGuideMilestone() {
        return null;
    }

    @Override
    public IRouter routeToGuide3DP() {
        return routeToGuide3DP(true);
    }

    @Override
    public IRouter routeToGuide3DP(boolean showClose) {
        mPostcard = createPostcard(RoutePath.GUIDE_j1)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .withBoolean("showClose", showClose);
        return this;
    }

    @Override
    public J1Router routeToGuideLaser() {
        return null;
    }

    @Override
    public J1Router routeToGuide10WLaser() {
        return null;
    }

    @Override
    public J1Router routeToGuideCNC() {
        return null;
    }

    @Override
    public J1Router routeToGuideRotaryLaser() {
        return null;
    }

    @Override
    public J1Router routeToGuideRotaryCNC() {
        return null;
    }

    @Override
    public J1Router routeToControlPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CONTROL_J1);
        return this;
    }

    @Override
    public J1Router routeToFilesPage(int fileType) {
        mPostcard = createPostcard(RoutePath.FILE_BROWSE_J1).withInt("file_type", fileType);
        return this;
    }

    @Override
    public J1Router routeToPreviewPage(boolean isLocal, String filePath) {
        return null;
    }

    @Override
    public J1Router routeToPrintSettingsPage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_J1_INDEX);
        return this;
    }

    @Override
    public J1Router routeToPrintPage() {
        mPostcard = createPostcard(RoutePath.PRINT_PRINT_J1);
        return this;
    }

    @Override
    public J1Router routeToPrepareLaserPage() {
        return null;
    }

    @Override
    public J1Router routeToPrepareCNCPage() {
        return null;
    }

    @Override
    public J1Router routeTo3DPCalibrationPage(boolean needBackHome) {
        return null;
    }

    @Override
    public J1Router routeToCalibrationPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_J1);
        return this;
    }

    @Override
    public J1Router routeToCalibrationPage(boolean isGuide) {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_J1)
                .withBoolean("is_guide", isGuide);
        return this;
    }

    @Override
    public J1Router routeToLaserCalibrationPage() {
        return null;
    }

    @Override
    public J1Router routeTo10wThicknessCalibrationPage() {
        return null;
    }

    @Override
    public J1Router routeToCameraCalibration() {
        return null;
    }

    @Override
    public J1Router routeToCNCOriginAssistantPage() {
        return null;
    }

    @Override
    public J1Router routeToCNCBitAssistantPage() {
        return null;
    }

    @Override
    public J1Router routeToSettingsPage() {
        return routeToSettingsPage(SectionAndDetailContainerFragment.SETTINGS_FIRMWARE);
    }

    @Override
    public J1Router routeToSettingsPage(int destination) {
        mPostcard = createPostcard(RoutePath.SETTINGS_J1_INDEX).withInt("destination", destination);
        return this;
    }

    @Override
    public J1Router routeToAboutPage() {
        return null;
    }

    @Override
    public J1Router routeToRemotePage() {
        return null;
    }

    @Override
    public J1Router routeToEmergencyStopPage(boolean isTriggeredOnPowerUp) {
        return null;
    }

    @Override
    public J1Router routeToFactoryActivity() {
        return null;
    }

    @Override
    public J1Router routeToUpdateActivity(String filePath, Boolean isLocal) {
        return null;
    }

    @Override
    public J1Router routeToBlockSetup() {
        return null;
    }

    @Override
    public J1Router routeToExperimentPage() {
        return null;
    }

    @Override
    public J1Router backHome() {
        mPostcard = createPostcard(RoutePath.J1_INDEX)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .withBoolean(Constants.KEY_IS_FORCE_BACK_HOME, true);
        return this;
    }

    @Override
    public J1Router backLandHome() {
        return null;
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
     * and cal fragment.startActivityForResult() by ourself.
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

    @Override
    public J1Router routeToFilamentSetup() {
        return null;
    }

    @Override
    public J1Router routeToZCalibrationSetup() {
        return null;
    }

    @Override
    public J1Router routeToZCalibration() {
        return null;
    }

    @Override
    public J1Router routeToXYCalibrationSetup() {
        return null;
    }

    @Override
    public J1Router routeToXYCalibration() {
        return null;
    }

    @Override
    public J1Router routeToHeatedBedLevelingSetup() {
        return null;
    }

    @Override
    public J1Router routeToHeatedBedLeveling() {
        return null;
    }

    @Override
    public J1Router routeToSingleSingleFilamentSetup() {
        return null;
    }

    @Override
    public J1Router routeToAutoMeasureCalibration() {
        return null;
    }

    @Override
    public J1Router routeTo10WCameraCaptureCalibration(Bundle bundle) {
        return null;
    }

    @Override
    public BaseRouter routeToSetupIntro(Bundle bundle) {
        return null;
    }

    @Override
    public Fragment getFragmentInstance(String fragmentPath) {
        return (Fragment) ARouter.getInstance().build(fragmentPath).navigation();
    }

    @Override
    public J1Router routeToManualFocusCalibration() {
        return null;
    }

    @Override
    public J1Router routeToCentralAxisCalibration() {
        return null;
    }

    @Override
    public J1Router routeToCalibrationComplete(int caliType) {
        return null;
    }

    @Override
    public J1Router routeToReplaceModules() {
        return null;
    }

    @Override
    public IRouter routeToRecoveryMode() {
        mPostcard = createPostcard(RoutePath.J1_SETTINGS_RECOVERY_MODE);
        return this;
    }

    @Override
    public BaseRouter routeToSettingsFirmwarePage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_J1_INDEX);
        return this;
    }

    @Override
    public IRouter routeToGreenScreen() {
        return super.routeToGreenScreen();
    }

    @Override
    public IRouter routeToUpdateSuccess(int event, String screenAPKPath) {
        mPostcard = createPostcard(RoutePath.J1_SETTINGS_UPDATE_SUCCESS)
                .withInt("updateEvent", event)
                .withString("apkPath", screenAPKPath);
        return this;
    }

    @Override
    public IRouter routeToUpdateInProgress(String updateFilePath, boolean isLocal) {
        mPostcard = createPostcard(RoutePath.J1_SETTINGS_UPDATE_IN_PROGRESS)
                .withString("filePath", updateFilePath)
                .withBoolean("isLocal", isLocal);

        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return mPostcard.toString();
    }
}
