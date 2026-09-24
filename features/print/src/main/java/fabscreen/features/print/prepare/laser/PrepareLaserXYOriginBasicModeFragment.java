package fabscreen.features.print.prepare.laser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.view.BaseFragment;

public class PrepareLaserXYOriginBasicModeFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("2-1 准备激光作业");
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_prepare_laser_basic_mode;
    }

    @OnClick(R2.id.btn_prepare_laser_basic_mode_next)
    void onClickNext() {
        playNormalClickSound();
        ((PrepareLaserActivity) requireActivity()).gotoLaserWorkHeightAutoMeasureFragment();
    }

    @OnClick(R2.id.btn_prepare_laser_basic_mode_select_mode)
    void onClickSelectMode() {
        playNormalClickSound();
        ((PrepareLaserActivity) requireActivity()).gotoLaserXYOriginSelectModeFragment();
    }

}

