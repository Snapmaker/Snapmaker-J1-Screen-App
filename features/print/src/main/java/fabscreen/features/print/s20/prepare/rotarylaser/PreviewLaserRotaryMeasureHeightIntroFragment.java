package fabscreen.features.print.s20.prepare.rotarylaser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.view.FabProgressDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PreviewLaserRotaryMeasureHeightIntroFragment extends BaseFragment {
    @BindView(R2.id.btn_preview_laser_4axis_measure_height_intro_next)
    Button mBtnNext;
    @BindView(R2.id.iv_prepare_install_material)
    ImageView mIvCover;
    private FabProgressDialog mProgressDialog;
    private LaserCalibrationViewModel mLaserCalibrationViewModel;

    public static PreviewLaserRotaryMeasureHeightIntroFragment newInstance() {
        return new PreviewLaserRotaryMeasureHeightIntroFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLaserCalibrationViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.preview_laser_prepare_4axis_measure_height);

        // init dialog.
        mProgressDialog = new FabProgressDialog(requireContext());
        mProgressDialog.setMessage(R.string.dialog_warning_moving);

        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W) {
            mIvCover.setImageResource(R.drawable.pic_10w_laser_measure_height_360x240);
        }

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_4axis_measure_height_intro;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        // Be aware we are using LaserCalibrationViewModel to get material diameter and length here.
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    @OnClick(R2.id.btn_preview_laser_4axis_measure_height_intro_next)
    void onClickNext() {
        playNormalClickSound();
        showDialog(mProgressDialog);
        mBtnNext.setEnabled(false);

        // Reuse laser rotary measure height moving logic. (where implemented in LaserCalibration4AxisInstallMaterialFragment)
        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float materialLength = mLaserCalibrationViewModel.getWorkpieceLength();

        float initialY = Math.min(sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - (2 * 10 + 1), sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - materialLength);

        float z = MockConst.LASER_MOCK_ROTARY_WASTE_BOARD_HEIGHT
                + MockConst.LASER_MOCK_ROTARY_HEIGHT
                + (mLaserCalibrationViewModel.getWorkpieceDiameter() / 2f)
                + MockConst.LASER_HOOD_HEIGHT
                + 15;

        // Make sure on CS#0 and then go to center for measuring height.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f - 6, initialY)))
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    dismissDialog(mProgressDialog);
                    mBtnNext.setEnabled(true);
                    if (getActivity() != null) {
                        ((PreviewActivity) getActivity()).gotoLaserRotaryMeasureHeight();
                    }
                }, e -> {
                    dismissDialog(mProgressDialog);
                    mBtnNext.setEnabled(true);
                    LogHelper.log(e);
                });
    }
}
