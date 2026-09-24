package fabscreen.features.settings.a400.maintenance.configparams;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.FabInputDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("NonConstantResourceId")
public class MaintainConfigParamsFragment extends BaseFragment {

    @BindView(R2.id.cv_single_single_initial_z)
    ConfigParamView mCvSingleSingleInitialZ;
    @BindView(R2.id.cv_single_dual_left_z)
    ConfigParamView mCvSingleDualLeftZ;
    @BindView(R2.id.cv_single_dual_right_z)
    ConfigParamView mCvSingleDualRightZ;
    @BindView(R2.id.cv_single_dual_x_offset)
    ConfigParamView mCvSingleDualXOffset;
    @BindView(R2.id.cv_single_dual_y_offset)
    ConfigParamView mCvSingleDualYOffset;
    @BindView(R2.id.cv_laser_focal_length)
    ConfigParamView mCvLaserFocalLength;
    @BindView(R2.id.cv_laser_platform_height)
    ConfigParamView mCvLaserPlatformHeight;
    @BindView(R2.id.cv_laser_4axis_center_height)
    ConfigParamView mCvLaser4AxisCenterHeight;

    private MaintainConfigParamsViewModel mViewModel;

    public static Fragment newInstance() {
        return new MaintainConfigParamsFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_maintenance_config_params;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(MaintainConfigParamsViewModel.class);
        initView();
    }

    private void initView() {
        setTitle("配置参数");
        mCvSingleSingleInitialZ.setTitle("初始打印高度");
        mCvSingleDualLeftZ.setTitle("左喷嘴初始打印高度");
        mCvSingleDualRightZ.setTitle("右喷嘴初始打印高度");
        mCvSingleDualXOffset.setTitle("双喷嘴 X 偏移");
        mCvSingleDualYOffset.setTitle("双喷嘴 Y 偏移");
        mCvLaserFocalLength.setTitle("激光焦距");
        mCvLaserPlatformHeight.setTitle("平台高度");
        mCvLaser4AxisCenterHeight.setTitle("四轴轴心高度");

        switch (mViewModel.getHeadType()) {
            case HEAD_3DP:
                mCvSingleSingleInitialZ.setVisibility(View.VISIBLE);
                mCvSingleSingleInitialZ.setOnEditClickListener(() -> editInitialZOffset(0));
                mViewModel.fetchZOffset();
                break;
            case HEAD_3DP_DOUBLE_EXTRUDER:
                mCvSingleDualLeftZ.setVisibility(View.VISIBLE);
                mCvSingleDualLeftZ.setOnEditClickListener(() -> editInitialZOffset(0));
                mViewModel.fetchZOffset();
                mCvSingleDualRightZ.setVisibility(View.VISIBLE);
                mCvSingleDualRightZ.setOnEditClickListener(() -> editInitialZOffset(1));
                mViewModel.fetchZOffset();
                mCvSingleDualXOffset.setVisibility(View.VISIBLE);
                mCvSingleDualXOffset.setOnEditClickListener(this::editSingleDualX);
                mCvSingleDualYOffset.setVisibility(View.VISIBLE);
                mCvSingleDualYOffset.setOnEditClickListener(this::editSingleDualY);
                mViewModel.fetchXYOffset();
                break;

            case HEAD_LASER:
            case HEAD_LASER_10W:
                mCvLaserFocalLength.setVisibility(View.VISIBLE);
                mCvLaserFocalLength.setOnEditClickListener(this::editFocalLength);
                mViewModel.fetchFocalLength();
                mCvLaserPlatformHeight.setVisibility(View.VISIBLE);
                mCvLaserPlatformHeight.setOnEditClickListener(this::editPlatformHeight);
                mViewModel.fetchPlatformHeight();
                mCvLaser4AxisCenterHeight.setVisibility(View.VISIBLE);
                mCvLaser4AxisCenterHeight.setOnEditClickListener(this::edit4AxisCenterHeight);
                mViewModel.fetch4AxisCenterHeight();
                break;
        }

        observeValues();
    }

    private void observeValues() {
        mViewModel.getZOffset0Observable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> {
                    mCvSingleSingleInitialZ.setValue(value);
                    mCvSingleDualLeftZ.setValue(value);
                }, LogHelper::log);

        mViewModel.getZOffset1Observable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mCvSingleDualRightZ.setValue(value), LogHelper::log);

        mViewModel.getXOffsetObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mCvSingleDualXOffset.setValue(value), LogHelper::log);

        mViewModel.getYOffsetObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mCvSingleDualYOffset.setValue(value), LogHelper::log);

        mViewModel.getFocalLenObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mCvLaserFocalLength.setValue(value), LogHelper::log);

        mViewModel.getPlatformHeightObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mCvLaserPlatformHeight.setValue(value), LogHelper::log);

        mViewModel.getRotaryCenterHeightObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mCvLaser4AxisCenterHeight.setValue(value), LogHelper::log);
    }

    private void edit4AxisCenterHeight() {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getCurrentRotaryCenterHeight())
                .setButton("ok", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setRotaryCenterHeight(Float.parseFloat(FabInputDialog.getsInstance().getEditTextContent()));
                })
                .show();
    }

    private void editPlatformHeight() {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getCurrentPlatformHeight())
                .setButton("ok", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setPlatformHeight(Float.parseFloat(FabInputDialog.getsInstance().getEditTextContent()));
                })
                .show();
    }

    private void editFocalLength() {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getCurrentFocalLength())
                .setButton("ok", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setFocalLen(Float.parseFloat(FabInputDialog.getsInstance().getEditTextContent()));
                })
                .show();
    }

    private void editSingleDualY() {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getCurrentYOffset())
                .setButton("ok", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setYOffset(Float.parseFloat(FabInputDialog.getsInstance().getEditTextContent()));
                })
                .show();
    }

    private void editSingleDualX() {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getCurrentXOffset())
                .setButton("ok", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setXOffset(Float.parseFloat(FabInputDialog.getsInstance().getEditTextContent()));
                })
                .show();
    }

    private void editInitialZOffset(int index) {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getCurrentZOffset(index))
                .setButton("ok", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setZOffset(index, Float.parseFloat(FabInputDialog.getsInstance().getEditTextContent()));
                })
                .show();
    }
}
