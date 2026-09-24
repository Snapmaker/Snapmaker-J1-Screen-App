package fabscreen.features.machinetools.calibration.s20.laser.common;

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
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.RulerView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibrationSetMaterialThicknessFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.rv_laser_calibration_material_thickness_ruler)
    RulerView mRvMaterialThickness;
    @BindView(R2.id.tv_laser_calibration_material_offset_value)
    TextView mTvMaterialThickness;
    @BindView(R2.id.btn_laser_calibration_next)
    Button mBtnNext;
    private CoordinateSystemPresenter mCoordinateSystemPresenter;
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Float> mThicknessSubject = BehaviorSubject.createDefault(0f);

    public static LaserCalibrationSetMaterialThicknessFragment newInstance() {
        return new LaserCalibrationSetMaterialThicknessFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_set_material_thickness;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        int calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCalibrationMode();
        if (calibrationMode == 0) {
            setTitle(R.string.laser_calibration_auto_focus);
        } else {
            setTitle(R.string.laser_calibration_manual_focus);
        }
        Logger.d("Enter %s laser focus.", calibrationMode == 0 ? "auto" : "manual");

        // Initial value
        final float thickness0 = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();
        mThicknessSubject.onNext(thickness0);
        mRvMaterialThickness.setCurrentValue(thickness0);

        // On value changes
        mRvMaterialThickness.setOnValueChangedListener(value -> {
            final float newValue = (int) (value * 10) / 10f;
            if (newValue != mThicknessSubject.getValue()) {
                mThicknessSubject.onNext(newValue);
            }
        });

        // Display
        mThicknessSubject
                .as(bindToLifecycle())
                .subscribe(thickness ->
                        mTvMaterialThickness.setText(String.format(Locale.getDefault(), getString(R.string.all_format_float), thickness)));

        // On moving event
        mIsMovingSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtnBack.setVisibility(isMoving ? View.INVISIBLE : View.VISIBLE);
                    mBtnNext.setEnabled(!isMoving);
                    mRvMaterialThickness.setEnabled(!isMoving);
                });

        // CS#0, where offset x, y, z = 0
        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(0);
    }

    private void finish() {
        mIsMovingSubject.onNext(false);

        if (getActivity() != null) {
            ((CalibrationLaserActivity) getActivity()).gotoMeasureHeightFragment();
        }
    }

    @OnClick(R2.id.btn_laser_calibration_next)
    void onClickNext() {
        playNormalClickSound();
        final float thickness = mThicknessSubject.getValue();
        Logger.d("Set material %.1f mm.", thickness);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserMaterialThickness(thickness);

        mIsMovingSubject.onNext(true);

        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();

        float z = MockConst.LASER_PLATE_SAFETY_HEIGHT + thickness + MockConst.LASER_HOOD_HEIGHT + 10;

        // Make sure on CS#0 and then go to center for measuring height.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> {
                    Vector vector = new Vector();
                    vector.setX(sizeX / 2f);
                    vector.setY(sizeY / 2f);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, 3000);
                })//.sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f, sizeY / 2f)))
                .flatMap(res -> {
                    Vector vector = new Vector();
                    vector.setZ(z);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, 1800);
                })//.sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    finish();
                }, e -> {
                    LogHelper.log(e);
                    Logger.e("Failed to move.");
                });
    }
}
