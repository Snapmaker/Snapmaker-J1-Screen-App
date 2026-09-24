package fabscreen.features.machinetools.calibration.s20._3dp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.CalibrationViewModel;
import fabscreen.platform.core.ui.presenter.HeatedBedWidgetPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class CalibrationPreHeatedBedFragment extends BaseFragment {
    @BindView(R2.id.view_calibration_pre_heated_bed_control)
    View mViewPreHeatedBedControl;
    @BindView(R2.id.btn_calibration_pre_heated_bed_calibrate)
    Button mBtnCalibrate;
    private CalibrationViewModel mViewModel;
    private HeatedBedWidgetPresenter mHeatedBedWidgetPresenter;
    private BehaviorSubject<Boolean> mIsBedPreHeatedReadySubject = BehaviorSubject.createDefault(false);

    public static CalibrationPreHeatedBedFragment newInstance() {
        return new CalibrationPreHeatedBedFragment();
    }

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
        return R.layout.fragment_calibration_pre_heated_bed;
    }

    @Override
    protected void back() {
        // Turn off bed before leaving the page.
        mViewModel.turnOffBed();
        super.back();
    }

    private void initView() {
        setTitle(R.string.calibration_pre_heated_bed_title);

        initPreHeatedBedControl();

        bindEvent();
    }

    private void initPreHeatedBedControl() {
        mHeatedBedWidgetPresenter = new HeatedBedWidgetPresenter(disposables);
        mHeatedBedWidgetPresenter.bind(mViewPreHeatedBedControl);
        mHeatedBedWidgetPresenter.connectMachineStatus();

        // Initialize heated bed temperature.
        float initHeatBedTemp = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationHeatedUpTemperature();
        mHeatedBedWidgetPresenter.setTargetValue(initHeatBedTemp);
    }

    private void bindEvent() {
        // Check if heated bed is already heated up with target temperature.
        // Skip first event to wait initialize temperature set up.
        MachineStatusManager.getMachineInfoHolder().getObservable()
                .skip(1)
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    boolean ok = (machineStatus.bedTemperature + 1 >= machineStatus.bedTargetTemperature);
                    mIsBedPreHeatedReadySubject.onNext(ok);
                });

        mIsBedPreHeatedReadySubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ready -> mBtnCalibrate.setEnabled(ready));
    }

    @Override
    protected CalibrationViewModel getViewModel() {
        return getViewModelProvider().get(CalibrationViewModel.class);
    }

    @OnClick(R2.id.btn_calibration_pre_heated_bed_calibrate)
    void onClickCalibrate() {
        playNormalClickSound();
        // Set target heated bed temperature in ViewModel. Value will be set into preferences when saving calibration.
        float heatedBedTemp = mHeatedBedWidgetPresenter.getTargetValue();
        mViewModel.setHeatedLevelingTemperature(heatedBedTemp);

        if (getActivity() != null) {
            ((Calibration3DPActivity) getActivity()).startCalibrationFragment();
        }
    }
}
