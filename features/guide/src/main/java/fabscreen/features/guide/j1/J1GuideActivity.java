package fabscreen.features.guide.j1;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.guide.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.GUIDE_j1)
public class J1GuideActivity extends BaseActivity {
    private static final String TAG = "J1GuideActivity";
    private static final int REQUEST_CALIBRATION_RESULT = 1;
    private IPreferences.Helper helper;
    private boolean mIsFirstWizard = false;
    private boolean mIsSafetyGlass = false;
    private boolean mIsConfirmFanInstruction = false;
    private boolean mShowClose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        mIsFirstWizard = true;
        mIsSafetyGlass = true;
        mIsConfirmFanInstruction = true;
        mShowClose = getIntent().getBooleanExtra("showClose", true);
        checkNext();
        if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getNeedQueryMachineError()) {
            ServiceContainer.getInstance().getService(IMachine.class).getErrorController().queryException().as(bindToLifecycle()).subscribe();
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setNeedQueryMachineError(false);
        }
    }


    public void initInfoFragment() {
        addFragment(R.id.fragment_container, GuideJ1InfoFragment.newInstance(mShowClose));
    }

    public void initSafetyGlassFragment() {
        addFragment(R.id.fragment_container, GuideJ1SafetyGlassFragment.newInstance());
    }

    public void initGuideJ1Success() {
        addFragment(R.id.fragment_container, GuideJ1SuccessfullyFragment.newInstance());
    }

    public void initGuideConfirmFanInstruction() {
        addFragment(R.id.fragment_container, GuideJ1InstallFanInfoFragment.newInstance());
    }

    public void initGuideInstallFanScrews() {
        addFragment(R.id.fragment_container, GuideJ1InstallFanScrewsFragment.newInstance());
    }

    public void initGuideInstallFanRecoverMachine() {
        addFragment(R.id.fragment_container, GuideJ1InstallFanRecoverMachineFragment.newInstance());
    }

    public void checkNext() {
        if (mIsFirstWizard) {
            mIsFirstWizard = false;
            initInfoFragment();
        } else if (mIsSafetyGlass) {
            mIsSafetyGlass = false;
            initSafetyGlassFragment();
        } else if(mIsConfirmFanInstruction) {
            mIsConfirmFanInstruction = false;
            initGuideConfirmFanInstruction();
        } else if (!helper.getGuideCalibration()) {
            ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage(true).startForResult(this, REQUEST_CALIBRATION_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CALIBRATION_RESULT && resultCode == RESULT_OK) {
            helper.setGuideTemperatureSelfCheck(false);
            helper.setGuideCalibration(false);
            helper.setMachineSetup3DP(true);
            initGuideJ1Success();
        }
    }
}
