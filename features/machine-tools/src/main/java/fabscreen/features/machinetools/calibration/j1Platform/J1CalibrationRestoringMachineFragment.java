package fabscreen.features.machinetools.calibration.j1Platform;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.j1Platform.LevelingXY.LevelingXYAuxiliaryCalibrationActivity;
import fabscreen.features.machinetools.calibration.j1Platform.levelingBed.LevelingBedAuxiliaryCalibrationActivity;
import fabscreen.features.machinetools.calibration.j1Platform.levelingZ.LevelingZAuxiliaryCalibrationActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1CalibrationRestoringMachineFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.iv_show_gif)
    ImageView mIvShowImage;
    @BindView(R2.id.iv_calibration_high_temperature_warning_normal)
    ImageView mIvCalibrationTemperatureWarningNormal;
    @BindView(R2.id.iv_calibration_glass_plate_normal)
    ImageView mIvCalibrationGlassPlateNormal;
    @BindView(R2.id.tv_calibration_title)
    TextView mTvCalibrationTitle;
    @BindView(R2.id.tv_calibration_progress)
    TextView mTvCalibrationProgress;
    @BindView(R2.id.tv_calibration_content_1)
    TextView mTvCalibrationContent1;
    private boolean isGuide;

    public static Fragment newInstance() {
        return new J1CalibrationRestoringMachineFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        restoringMachine();
    }

    public void restoringMachine() {
        fabMoving.show();
        Vector vector = new Vector();
        vector.setZ(100);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController()
                .gotoAbsolutePosition(vector)
                .flatMap(responseStructure -> ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(true))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    fabMoving.dismiss();
                }, e -> {
                    fabMoving.dismiss();
                    LogHelper.log(e);
                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                    requireActivity().finish();
                });
    }

    private void initView() {
        if (getArguments() != null) {
            isGuide = getArguments().getBoolean("is_guide", false);
        }
        if (isGuide) {
            mTvCalibrationContent1.setText(R.string.j1_calibration_restore_extruder_content);
            Glide.with(this)
                    .load(R.drawable.gif_calibration_j1_z_offset_tighten_the_screws)
                    .into(mIvShowImage);
        } else {
            mTvCalibrationContent1.setText(requireActivity() instanceof LevelingZAuxiliaryCalibrationActivity ?
                    R.string.j1_calibration_restore_machine_glass_and_extruder_content :
                    R.string.j1_calibration_restore_machine_glass_content);
            Glide.with(this)
                    .load(requireActivity() instanceof LevelingZAuxiliaryCalibrationActivity ?
                            R.drawable.gif_calibration_j1_z_offset_tighten_the_screws_and_glass_plate
                            : R.drawable.gif_calibration_j1_xy_offset_restore_machine)
                    .into(mIvShowImage);
        }

        mTvCalibrationTitle.setText(R.string.j1_calibration_restore_machine_title);
        mTvCalibrationProgress.setText("6/6");

        mIvCalibrationTemperatureWarningNormal.setVisibility(View.VISIBLE);
        mIvCalibrationGlassPlateNormal.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_currency;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof LevelingBedAuxiliaryCalibrationActivity) {
            ((LevelingBedAuxiliaryCalibrationActivity) fragmentActivity).gotoCalibrationSuccess();
        } else if (fragmentActivity instanceof LevelingXYAuxiliaryCalibrationActivity) {
            ((LevelingXYAuxiliaryCalibrationActivity) fragmentActivity).gotoCalibrationSuccess();
        } else if (fragmentActivity instanceof LevelingZAuxiliaryCalibrationActivity) {
            ((LevelingZAuxiliaryCalibrationActivity) fragmentActivity).gotoCalibrationSuccess();
        }
    }

}
