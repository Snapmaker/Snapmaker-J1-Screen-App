package fabscreen.features.print.prepare.laser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.view.BaseFragment;

public class PrepareLaserWorkHeightSelectModeFragment extends BaseFragment {

    private int mSelectMode = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_prepare_laser_work_height_select_mode;
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_select_mode_auto_measure)
    void onClickAutoMeasure() {
        playNormalClickSound();
        mSelectMode = 0;
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_select_mode_input_material)
    void onClickInputMaterial() {
        playNormalClickSound();
        mSelectMode = 1;
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_select_mode_touch_material)
    void onClickTouchMaterial() {
        playNormalClickSound();
        mSelectMode = 2;
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_select_mode_manual)
    void onClickManual() {
        playNormalClickSound();
        mSelectMode = 3;
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_select_mode_cancel)
    void OnClickCancel() {
        playNormalClickSound();
        back();
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_select_mode_confirm)
    void onClickConfirm() {
        playNormalClickSound();
        switch (mSelectMode) {
            case 0:
                ((PrepareLaserActivity) requireActivity()).gotoLaserWorkHeightAutoMeasureFragment();
                break;
            case 1:
                ((PrepareLaserActivity) requireActivity()).gotoLaserWorkHeightInputMaterialFragment();
                break;
            case 2:
                ((PrepareLaserActivity) requireActivity()).gotoLaserWorkHeightTouchMaterialFragment();
                break;
            case 3:
                ((PrepareLaserActivity) requireActivity()).gotoLaserWorkHeightManualAdjustFragment();
                break;
            default:
                break;
        }
    }
}

