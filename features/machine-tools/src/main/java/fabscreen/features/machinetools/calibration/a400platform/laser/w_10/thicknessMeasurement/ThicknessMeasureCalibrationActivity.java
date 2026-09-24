package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.thicknessMeasurement;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.WarmTipDialog;
import fabscreen.platform.lib.LogHelper;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_LASER_THICKNESS_MEASURE_CALIBRATION)
public class ThicknessMeasureCalibrationActivity extends BaseActivity {

    public WarmTipDialog fabLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        fabLoading = WarmTipDialog.create(this)
                .setDialogWidthSize(WarmTipDialog.WarmTipDialogSize.SIZE_M)
                .setPic(R.drawable.ic_block_setup)
                .setTitle(R.string.move_show)
                .setContent(R.string.move_show_content);

        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, ThicknessMeasureCalibration11Fragment.newInstance());
    }

    public void gotToThicknessMeasureCalibration21() {
        addFragment(R.id.fragment_container, ThicknessMeasureCalibration21Fragment.newInstance());
    }

    public void gotToThicknessMeasureCalibration22() {
        addFragment(R.id.fragment_container, ThicknessMeasureCalibration22Fragment.newInstance());
    }

    public void showDialog() {
        if (fabLoading.isShowing()) {
            return;
        }
        fabLoading.show();
    }

    public void dismissDialog() {
        fabLoading.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().switchFocusAssistLight(0).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
