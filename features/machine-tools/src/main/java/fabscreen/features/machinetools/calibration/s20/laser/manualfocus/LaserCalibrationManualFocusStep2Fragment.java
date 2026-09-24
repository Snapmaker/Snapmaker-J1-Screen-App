package fabscreen.features.machinetools.calibration.s20.laser.manualfocus;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibrationManualFocusStep2Fragment extends BaseFragment {
    private final static String TAG = LaserCalibrationManualFocusStep2Fragment.class.getSimpleName();

    private final static int STATUS_IDLE = 0;
    private final static int STATUS_LASER_TEST = 1;
    private final static int STATUS_COMPLETE = 2;
    private final static int STATUS_ERROR = 3;

    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.tv_guide_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_content)
    TextView mTvContent;
    @BindView(R2.id.btn_guide_next)
    Button mBtnComplete;

    private BehaviorSubject<Integer> mCalibrationStatusSubject = BehaviorSubject.createDefault(STATUS_IDLE);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_auto_focus_step2;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.laser_calibration_manual_focus);

        mTvTitle.setText(R.string.laser_calibration_engraving);
        mTvContent.setText(R.string.laser_calibration_manual_focus_notice);
        mBtnComplete.setVisibility(Button.GONE);

        mCalibrationStatusSubject
                .as(bindToLifecycle())
                .subscribe(status -> {
                    switch (status) {
                        case STATUS_IDLE:
                        case STATUS_LASER_TEST:
                            mBtnBack.setVisibility(View.GONE);
                            mBtnComplete.setVisibility(View.VISIBLE);
                            mBtnComplete.setEnabled(false);
                            mBtnComplete.setText(R.string.laser_calibration_processing);
                            break;
                        case STATUS_ERROR:
                            mBtnBack.setVisibility(Button.VISIBLE);
                            break;
                        default:
                            break;
                    }
                });

        startCalibration();
    }

    private void startCalibration() {
        mCalibrationStatusSubject.onNext(STATUS_LASER_TEST);
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().startLaserFineTune(Constants.LASER_TEST_PATTERN_Z_DIFF)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    if (result == 0) {
                        CalibrationLaserActivity activity = (CalibrationLaserActivity) getContext();
                        if (activity != null) {
                            activity.gotoLaserCalibrationManualFineTunePick();
                        }
                    } else {
                        mCalibrationStatusSubject.onNext(STATUS_ERROR);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mCalibrationStatusSubject.onNext(STATUS_ERROR);
                });
    }
}
