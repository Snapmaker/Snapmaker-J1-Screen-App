package fabscreen.features.guide.s20.laser.autofocus;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.core.ui.view.RulerView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class GuideLaserAutoFocusPickFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.btn_laser_calibration_save)
    Button mBtnLaserCalibrationSave;
    @BindView(R2.id.tv_laser_calibration_pick_offset)
    TextView mTvFineTuneValue;
    @BindView(R2.id.rv_laser_calibration_pick_ruler)
    RulerView mRvFineTuneRuler;
    private BehaviorSubject<Float> mZOffsetValueSubject = BehaviorSubject.createDefault(0f);
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);

    public static GuideLaserAutoFocusPickFragment newInstance() {
        return new GuideLaserAutoFocusPickFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_fine_tune_pick;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.laser_calibration_auto_focus);

        // Display
        mZOffsetValueSubject
                .as(bindToLifecycle())
                .subscribe(value -> {
                    mTvFineTuneValue.setText(String.format(Locale.getDefault(), "%.1f", value));
                });

        // On value changes
        mRvFineTuneRuler.setOnValueChangedListener(value -> {
            // final float newValue = ((int) (value * 10) / 10f - 10) * mZStep;
            if (mZOffsetValueSubject.getValue() != value) {
                mZOffsetValueSubject.onNext(value);
            }
        });

        mIsMovingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtnBack.setEnabled(!isMoving);
                    mBtnLaserCalibrationSave.setEnabled(!isMoving);
                });
    }

    private void finish() {
        mIsMovingSubject.onNext(false);
        if (getActivity() != null) {
            ((GuideLaserActivity) getActivity()).startCameraCalibrationIntroFragment();
        }
    }

    @OnClick(R2.id.btn_laser_calibration_save)
    void onClickSave() {
        playNormalClickSound();
        final float initialZ = (float) -ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
        float thickness = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();

        float offset = mZOffsetValueSubject.getValue();
        float focalLength = initialZ + offset - thickness;
        Logger.i("mFocalLength is " + focalLength);

        mIsMovingSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController()
                .setFocalLength(focalLength)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
                                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z" + offset + " F1800"))
                                .flatMap(res -> {
                                            Vector vector = new Vector();
                                            vector.setZ(0);
                                            return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
                                        }
                                )
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(response -> finish());


                    } else {
                        FabAlert.alert(getContext(), R.string.laser_alert_failed_to_get_focal_length);
                        finish();
                    }
                }, e -> {
                    LogHelper.log(e);
                    FabAlert.alert(getContext(), R.string.laser_alert_failed_to_get_focal_length);
                    finish();
                });
    }
}
