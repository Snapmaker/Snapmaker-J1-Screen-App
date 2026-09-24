package fabscreen.features.machinetools.calibration.s20.laser.rotary;

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
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.model.LaserFineTuneExecutor;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibration4AxisManualFocusStep2Fragment extends BaseFragment {
    private final static String TAG = LaserCalibration4AxisManualFocusStep2Fragment.class.getSimpleName();

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

    private LaserCalibrationViewModel mViewModel;

    private BehaviorSubject<Integer> mCalibrationStatusSubject = BehaviorSubject.createDefault(STATUS_IDLE);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

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
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    private void initView() {
        setTitle(R.string.laser_calibration_manual_focus);

        mTvTitle.setText(R.string.laser_calibration_manual_focus);
        mTvContent.setText(R.string.laser_calibration_engraving_notice);
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

        LaserFineTuneExecutor fineTuneExecutor = new LaserFineTuneExecutor(disposables);
        fineTuneExecutor.setLaserPattern(mViewModel.getLaserPattern());
        fineTuneExecutor.startFineTune()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {
                        CalibrationLaserActivity activity = (CalibrationLaserActivity) getContext();
                        if (activity != null) {
                            activity.goto4AxisManualFineTunePick();
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
