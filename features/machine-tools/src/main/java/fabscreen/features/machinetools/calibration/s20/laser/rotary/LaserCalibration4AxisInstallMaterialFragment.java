package fabscreen.features.machinetools.calibration.s20.laser.rotary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibration4AxisInstallMaterialFragment extends BaseFragment {
    @BindView(R2.id.btn_preview_laser_install_material_next)
    Button mBtnNext;
    private LaserCalibrationViewModel mViewModel;
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);

    public static LaserCalibration4AxisInstallMaterialFragment newInstance() {
        return new LaserCalibration4AxisInstallMaterialFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.cnc_origin_assistant_install_material_title);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_install_material_land;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    private void initView() {
        // On moving event
        mIsMovingSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> mBtnNext.setEnabled(!isMoving));
    }

    private void finish() {
        mIsMovingSubject.onNext(false);

        if (getActivity() != null) {
            ((CalibrationLaserActivity) getActivity()).gotoLaser4AxisMeasureHeight();
        }
    }

    @OnClick(R2.id.btn_preview_laser_install_material_next)
    void onClickNext() {
        playNormalClickSound();
        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float materialLength = mViewModel.getWorkpieceLength();

        float initialY = Math.min(sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - (2 * 10 + 1), sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - materialLength);

        float z = MockConst.LASER_MOCK_ROTARY_WASTE_BOARD_HEIGHT
                + MockConst.LASER_MOCK_ROTARY_HEIGHT
                + (mViewModel.getWorkpieceDiameter() / 2f)
                + MockConst.LASER_HOOD_HEIGHT
                + 15;

        mIsMovingSubject.onNext(true);
        // Make sure on CS#0 and then go to center for measuring height.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f - 6, initialY)))
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mIsMovingSubject.onNext(false);
                    finish();
                }, e -> {
                    mIsMovingSubject.onNext(false);
                    LogHelper.log(e);
                    Logger.e("Failed to move.");
                });
    }
}
