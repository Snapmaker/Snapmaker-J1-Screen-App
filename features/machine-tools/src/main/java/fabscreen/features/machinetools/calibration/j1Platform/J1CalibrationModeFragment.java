package fabscreen.features.machinetools.calibration.j1Platform;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1CalibrationModeFragment extends BaseFragment {
    @BindView(R2.id.iv_show_image)
    ImageView mIvShowImage;
    @BindView(R2.id.tv_calibration_mode_content)
    TextView mTvCalibrationModeContent;
    @BindView(R2.id.rl_calibration_switch_mode)
    RelativeLayout mRlCalibrationMode;
    @BindView(R2.id.sw_calibration_switch_mode)
    Switch mSwCalibrationMode;
    IPreferences.Helper mHelper;
    private J1CalibrationMode.J1CalibrationModeIndex mCalibrationModeIndex;
    private boolean mIsAuxiliary;
    private J1CalibrationMode mJ1CalibrationMode;
    private boolean mIsGuide;

    public static Fragment newInstance(J1CalibrationMode.J1CalibrationModeIndex calibrationModeIndex, boolean isGuide) {
        Fragment fragment = new J1CalibrationModeFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("modeIndex", calibrationModeIndex);
        bundle.putBoolean("isGuide", isGuide);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHelper = getServiceContainer().getService(IPreferences.class).getHelper();
        mCalibrationModeIndex = (J1CalibrationMode.J1CalibrationModeIndex) requireArguments().getSerializable("modeIndex");
        mIsGuide = requireArguments().getBoolean("isGuide");

        getIsAuxiliary();
        if (mIsGuide) {
            mIsAuxiliary = true;
        }
        getCalibrationMode();
        initView();
    }

    private void initView() {
        Glide.with(this).load(mJ1CalibrationMode.getCalibrationModeImageId()).into(mIvShowImage);
        mTvCalibrationModeContent.setText(mJ1CalibrationMode.getCalibrationModeContentId());
        mSwCalibrationMode.setChecked(mJ1CalibrationMode.isAuxiliary());
//        if (mCalibrationModeIndex == CALIBRATION_CHECK || mIsGuide) {
//            mRlCalibrationMode.setVisibility(View.INVISIBLE);
//        } else {
//            mRlCalibrationMode.setVisibility(View.VISIBLE);
//        }
    }

    private void getCalibrationMode() {
        switch (mCalibrationModeIndex) {
            case HEATED_BED_LEVELING:
                mJ1CalibrationMode = mIsAuxiliary ?
                        new J1CalibrationMode(
                                mCalibrationModeIndex,
                                R.drawable.gif_calibration_j1_heated_bed_leveing_auxiliary_content,
                                R.string.calibration_J1_heated_bed_leveing_auxiliary_content,
                                RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_BED_AUXILIARY,
                                mIsAuxiliary)
                        :
                        new J1CalibrationMode(
                                mCalibrationModeIndex,
                                R.drawable.ic_j1_calibration,
                                R.string.calibration_J1_heated_bed_leveing_auxiliary_content,
                                RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_BED,
                                mIsAuxiliary);
                break;
            case Z_OFFSET_CALIBRATION:
                mJ1CalibrationMode = mIsAuxiliary ?
                        new J1CalibrationMode(
                                mCalibrationModeIndex,
                                R.drawable.gif_calibration_j1_z_offset_calibration_content,
                                R.string.calibration_J1_Z_offset_calibration_content,
                                RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_Z_AUXILIARY,
                                mIsAuxiliary)
                        :
                        new J1CalibrationMode(
                                mCalibrationModeIndex,
                                R.drawable.ic_j1_calibration,
                                R.string.calibration_J1_heated_bed_leveing_auxiliary_content,
                                RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_Z,
                                mIsAuxiliary);
                break;
            case XY_OFFSET_CALIBRATION:
                mJ1CalibrationMode = mIsAuxiliary ?
                        new J1CalibrationMode(
                                mCalibrationModeIndex,
                                R.drawable.gif_calibration_j1_xy_offset_auxiliary_calibration_content,
                                R.string.calibration_J1_XY_offset_auxiliary_calibration_content,
                                RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_XY_AUXILIARY,
                                mIsAuxiliary)
                        :
                        new J1CalibrationMode(
                                mCalibrationModeIndex,
                                R.drawable.ic_j1_calibration,
                                R.string.calibration_J1_XY_offset_calibration_content,
                                RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_XY,
                                mIsAuxiliary);
                break;
            case CALIBRATION_CHECK:
                mJ1CalibrationMode = new J1CalibrationMode(
                        mCalibrationModeIndex,
                        R.drawable.pic_model_check_360x408,
                        R.string.calibration_J1_calibration_check_content,
                        RoutePath.TOOLS_CALIBRATION_J1_3DP_CALIBRATION_CHECK,
                        mIsAuxiliary);
                break;
            case CALIBRATION_VIBRATION:
                mJ1CalibrationMode = new J1CalibrationMode(
                        mCalibrationModeIndex,
                        R.drawable.pic_calibration_j1_clibration_vibration_content,
                        R.string.j1_calibration_vibration_intro_desc,
                        RoutePath.TOOLS_CALIBRATION_J1_3DP_CALIBRATION_VIBRATION,
                        mIsAuxiliary);
                break;
            default:
                mJ1CalibrationMode = null;
                break;
        }
    }

    private void getIsAuxiliary() {
        switch (mCalibrationModeIndex) {
            case HEATED_BED_LEVELING:
                mIsAuxiliary = mHelper.getHeatedBedLeveLingIsAuxiliary();
                break;
            case Z_OFFSET_CALIBRATION:
                mIsAuxiliary = mHelper.getZOffsetCalibrationIsAuxiliary();
                break;
            case XY_OFFSET_CALIBRATION:
                mIsAuxiliary = mHelper.getXYOffsetCalibrationIsAuxiliary();
                break;
            default:
                mIsAuxiliary = false;
        }
    }

    @OnCheckedChanged(R2.id.sw_calibration_switch_mode)
    public void SwitchChange(CompoundButton view, boolean isCheck) {
        if (isCheck != mIsAuxiliary) {
            mIsAuxiliary = isCheck;
            switch (mCalibrationModeIndex) {
                case HEATED_BED_LEVELING:
                    mHelper.setHeatedBedLeveLingIsAuxiliary(mIsAuxiliary);
                    break;
                case Z_OFFSET_CALIBRATION:
                    mHelper.setZOffsetCalibrationIsAuxiliary(mIsAuxiliary);
                    break;
                case XY_OFFSET_CALIBRATION:
                    mHelper.setXYOffsetCalibrationIsAuxiliary(mIsAuxiliary);
                    break;
                default:
            }
            getCalibrationMode();
            initView();
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_mode;
    }

    @OnClick(R2.id.btn_calibration_start)
    void onCalibrationStartClicked(View view) {
        playNormalClickSound();
        if (needNoMoreConsideration(view)) {
            startProcedure();
        }
    }

    private boolean needNoMoreConsideration(View view) {
        if (mJ1CalibrationMode.getCalibrationModeIndex() == J1CalibrationMode.J1CalibrationModeIndex.CALIBRATION_VIBRATION) {
            view.setEnabled(false);
            getServiceContainer().getService(IMachine.class).getMachineController().getVibrationCompensationEnabled()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(response -> {
                        view.setEnabled(true);
                        handleByCompensationState(response);
                    }, LogHelper::log);
            return false;
        }
        return true;
    }

    private void handleByCompensationState(ResponseStructure<IStructure> response) {
        if (response.isSuccess()) {
            if (!((BoolProp) response.dataProp).getValue()) {
                showCompensationOffDialog();
            } else {
                startProcedure();
            }
        } else {
            Logger.e("Start vibration calibration fail, could not get current enable state");
        }
    }

    private void showCompensationOffDialog() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(2, false, false, false, true)
                .setContent(R.string.j1_calibration_start_but_vibration_compensation_off_dialog_content)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> dialog.dismiss())
                .setSecondTv(R.string.all_start, R.color.select_dialog_orange_txt, (dialog, which) -> {
                    dialog.dismiss();
                    startProcedure();
                })
                .show();
    }

    private void startProcedure() {
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(mJ1CalibrationMode.getCalibrationModePath(), mIsGuide)
                .start(getContext());
    }
}
