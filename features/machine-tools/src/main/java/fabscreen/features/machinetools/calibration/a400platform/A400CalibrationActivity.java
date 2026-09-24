package fabscreen.features.machinetools.calibration.a400platform;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400)
public class A400CalibrationActivity extends BaseActivity {
    private static int STATUS_IDLE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        showPrintState();
        Fragment fragment = A400CalibrationFragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public static class CalibrationType {
        // 3dp
        public static final int Z_CALI_MANUAL = 0x01;
        public static final int Z_CALI_AUTO = 0x02;
        public static final int Z_CALI_AUTO_SENSOR = 0x03;
        public static final int BED_LEVELING_MANUAL = 0x04;
        public static final int BED_LEVELING_AUTO = 0x05;
        public static final int DUAL_EXTRUDER_XY = 0x06;

        // laser-10w-3axis
        public static final int THK_MEASURE = 0x07;
        public static final int PLATFORM_HEIGHT_CALI = 0x08;
        public static final int CAMERA_CALI = 0x09;

        // laser-10w-4axis
        public static final int AXIS_CENTRAL_CALI = 0x0a;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CalibrationType.Z_CALI_AUTO:
                case CalibrationType.BED_LEVELING_AUTO:
                case CalibrationType.DUAL_EXTRUDER_XY:
                case CalibrationType.THK_MEASURE:
                case CalibrationType.CAMERA_CALI:
                case CalibrationType.AXIS_CENTRAL_CALI:
                    mRouter.routeToCalibrationComplete(requestCode).start(this);
                    break;
            }
        }
    }

    public void showPrintState() {
        ViewGroup rootView = (ViewGroup) this.findViewById(android.R.id.content).getRootView();
        View floatView = LayoutInflater.from(this).inflate(fabscreen.platform.base.R.layout.view_a400_top_icon_toast, rootView, false);
        rootView.addView(floatView);
        ImageView ivTopToast = floatView.findViewById(R.id.iv_top_toast);
        TextView tvTopToast = floatView.findViewById(R.id.tv_top_toast);
        ivTopToast.setImageResource(R.drawable.pic_dialog_warning_72x72);
        tvTopToast.setText(R.string.printing_toast);
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineStatusSubjectHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    boolean isIdle = status.status == PrintController.STATE_IDLE;
                    boolean isPrint = status.status <= 10;
                    boolean is3DP = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.FDM;
                    tvTopToast.setText(getString(R.string.printing_toast, getString(isPrint && is3DP ? R.string.printing_toast_3dp_print : R.string.printing_toast_other)));
                    floatView.setVisibility(isIdle ? View.INVISIBLE : View.VISIBLE);
                }, LogHelper::log);


    }
}
