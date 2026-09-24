package fabscreen.features.print.s20.prepare.cnc;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.view.BaseFragment;

public class PrepareCNCXYZOriginSelectModeFragment extends BaseFragment {

    private int mSelectMode = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_prepare_cnc_xyz_origin_select_mode;
    }

    @OnClick(R2.id.btn_prepare_cnc_xyz_origin_select_mode_basic)
    void onClickBasic() {
        playNormalClickSound();
        mSelectMode = 0;
    }

    @OnClick(R2.id.btn_prepare_cnc_xyz_origin_select_mode_advance)
    void onClickAdvance() {
        playNormalClickSound();
        mSelectMode = 1;
    }

    @OnClick(R2.id.btn_prepare_cnc_xyz_origin_select_mode_cancel)
    void OnClickCancel() {
        playNormalClickSound();
        back();
    }

    @OnClick(R2.id.btn_prepare_cnc_xyz_origin_select_mode_confirm)
    void onClickConfirm() {
        playNormalClickSound();
        final boolean isBasicMode = (mSelectMode == 0);
        if (isBasicMode) {
            ((PrepareCNCActivity) requireActivity()).gotoCNCXYZOriginBasicModeFragment();
        } else {
            ((PrepareCNCActivity) requireActivity()).gotoCNCXYZOriginAdvanceModeFragment();
        }
    }
}

