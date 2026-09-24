package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationBaseInfoFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class A400LevelingBedCalibrationInfoFragment extends A400CalibrationBaseInfoFragment {
    public static final int A400_LEVELING_BED_CALIBRATION_AUTO = 0;
    public static final int A400_LEVELING_BED_CALIBRATION_MANUAL = 1;
    protected IPreferences.Helper helper;
    int mCalibrationMode;
    String mCalibrationGrid;
    int mCalibrationBedTemperature;
    private int mHeadType;

    public static Fragment newInstance() {
        return new A400LevelingBedCalibrationInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
        mHeadType = getServiceContainer().getService(IMachine.class).getFDMController().getHeadType();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        getData();

        mIvInfoShow.setImageResource(R.drawable.pic_a400_leveling_bed_calibration_info);
        mTvInfoShowTitle.setText(R.string.calibration_heated_bed_leveing_title);

        String checkMode;
        if (mCalibrationMode == A400_LEVELING_BED_CALIBRATION_AUTO) {
            checkMode = getString(R.string.calibration_auto_mode);
            mTvCheckMode.setText(String.format(Locale.US, "%s %s %d ℃", checkMode, mCalibrationGrid, mCalibrationBedTemperature));
            mTvInfoShowContent.setText(String.format(Locale.getDefault(), getString(R.string.calibration_auto_content), mCalibrationGrid));

        } else {
            checkMode = getString(R.string.calibration_manual_mode);
            mTvCheckMode.setText(String.format(Locale.US, "%s %s", checkMode, mCalibrationGrid));
            mTvInfoShowContent.setText(String.format(Locale.getDefault(), getString(R.string.calibration_manual_content), mCalibrationGrid));
        }
    }

    private void getData() {
        if (mHeadType == Module.ModuleType.HEAD_3DP) {
            helper.setA400LevelingBedCalibrationMode(A400_LEVELING_BED_CALIBRATION_MANUAL);
        }
        mCalibrationMode = helper.getA400LevelingBedCalibrationMode();
        mCalibrationGrid = getResources().getStringArray(R.array.grid_types)[gridGeoIndex(helper.getA400LevelingBedCalibrationGrid())];
        mCalibrationBedTemperature = helper.getA400LevelingBedCalibrationBedTemperature();
    }

    private int gridGeoIndex(int a400LevelingBedCalibrationGrid) {
        switch (a400LevelingBedCalibrationGrid) {
            case 3:
                return 0;
            case 9:
                return 2;
            default:
                return 1;
        }

    }

    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setPic(R.drawable.ic_a400_clean_up_112x112)
                .setType(DecisionDialog.TIP_TYPE)
                .setTitle(getString(R.string.calibraiton_a400_procedure_start_confirm_dialog_title,
                        getString(R.string.calibration_heated_bed_leveing_title)))
                .setContent(R.string.calibraiton_a400_procedure_start_confirm_dialog_content_3dp)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_next, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    ServiceContainer.getInstance().getService(IMachine.class)
                            .getFDMController()
                            .setCalibrationMode(mCalibrationMode == A400_LEVELING_BED_CALIBRATION_AUTO ? 2 : 3)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(responseStructure -> {
                                if (responseStructure.isSuccess()) {
                                    String path;
                                    int caliType;
                                    if (mCalibrationMode == A400_LEVELING_BED_CALIBRATION_AUTO) {
                                        path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_AUTO;
                                        caliType = A400CalibrationActivity.CalibrationType.BED_LEVELING_AUTO;
                                    } else {
                                        path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_MANUAL;
                                        caliType = A400CalibrationActivity.CalibrationType.BED_LEVELING_MANUAL;
                                    }
                                    mRouter.routeWithClassPath(path).startForResult(requireActivity(), caliType);
                                } else {
                                    Logger.d("---FDT--- error: " + responseStructure);
                                }
                            }, LogHelper::log);
                })
                .show();
    }

    @OnClick(R2.id.cl_calibration_check_mode)
    public void onClickCheckMode() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_CHECK_MODE)
                .start(getContext());
    }

}
