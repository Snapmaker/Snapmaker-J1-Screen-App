package fabscreen.platform.core.router;

import android.content.Intent;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.service.BaseRouter;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;

public class A400Router extends BaseRouter implements IRouter, IServiceIdentifier {
    public A400Router() {
        super();
    }

    @Override
    public BaseRouter routeToCalibrationPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400);
        return this;
    }

    @Override
    public BaseRouter routeToControlPage() {
        mPostcard = createPostcard(RoutePath.TOOLS_CONTROL_A400);
        return this;
    }

    @Override
    public BaseRouter routeToHome() {
        mPostcard = createPostcard(RoutePath.A400_INDEX);
        return this;
    }

    @Override
    public IRouter routeToHome(int event) {
        mPostcard = createPostcard(RoutePath.A400_INDEX).withInt("event", event);
        return this;
    }

    @Override
    public BaseRouter routeToGuideCNC() {
        mPostcard = createPostcard(RoutePath.TOOLS_CALIBRATION_A400_CNC_SETUP);
        return this;
    }

    @Override
    public BaseRouter routeToFilesPage(int fileType) {
        mPostcard = createPostcard(RoutePath.FILE_BROWSE_A400).withInt("file_type", fileType);
        return this;
    }

    @Override
    public BaseRouter routeToPrintPage() {
        mPostcard = createPostcard(RoutePath.PRINT_PRINT_A400);
        return this;
    }

    @Override
    public BaseRouter routeToSettingsPage() {
        return routeToSettingsPage(SectionAndDetailContainerFragment.SETTINGS_WIFI);
    }

    @Override
    public BaseRouter routeToSettingsPage(int destination) {
        mPostcard = createPostcard(RoutePath.SETTINGS_A400_INDEX).withInt("destination", destination);
        return this;
    }


    @Override
    public BaseRouter routeToRemotePage() {
        return super.routeToRemotePage();
    }

    @Override
    public BaseRouter backHome() {
        mPostcard = createPostcard(RoutePath.A400_INDEX)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .withBoolean(Constants.KEY_IS_FORCE_BACK_HOME, true);
        return this;
    }

    @Override
    public BaseRouter routeToGreenScreen() {
        mPostcard = createPostcard(RoutePath.A400_GREEN_SCREEN);
        return this;
    }

    @Override
    public BaseRouter routeToSettingsFirmwarePage() {
        mPostcard = createPostcard(RoutePath.SETTINGS_FIRMWARE_S30);
        return this;
    }

    @Override
    public IRouter routeToUpdateSuccess(int event, String screenAPKPath) {
        mPostcard = createPostcard(RoutePath.A400_SETTINGS_UPDATE_SUCCESS)
                .withInt("updateEvent", event)
                .withString("apkPath", screenAPKPath);

        return this;
    }

    @Override
    public IRouter routeToRecoveryMode() {
        mPostcard = createPostcard(RoutePath.A400_SETTINGS_RECOVERY_MODE);
        return this;
    }

    @Override
    public IRouter routeToPrintSetting() {
        mPostcard = createPostcard(RoutePath.PRINT_SETTING);
        return this;
    }

    @Override
    public BaseRouter routeToWelcome() {
        mPostcard = createPostcard(RoutePath.WELCOME_A400);
        return this;
    }

    @Override
    public IRouter routeToUpdateModules() {
        mPostcard = createPostcard(RoutePath.A400_SETTINGS_UPDATE_MODULES);
        return this;
    }

    @Override
    public IRouter routeToUpdateInProgress(String updateFilePath, boolean isLocal) {
        mPostcard = createPostcard(RoutePath.A400_SETTINGS_UPDATE_IN_PROGRESS)
                .withString("filePath", updateFilePath)
                .withBoolean("isLocal", isLocal);

        return this;
    }
}
