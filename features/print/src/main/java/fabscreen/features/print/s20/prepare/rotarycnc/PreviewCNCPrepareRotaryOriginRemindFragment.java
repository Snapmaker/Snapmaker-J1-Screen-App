package fabscreen.features.print.s20.prepare.rotarycnc;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class PreviewCNCPrepareRotaryOriginRemindFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_cnc_origin_remind;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_cnc_rotary_origin_remind_next)
    void onClickNext() {
        playNormalClickSound();
        PreviewActivity activity = (PreviewActivity) getActivity();

        if (activity != null) {
            activity.gotoCNCPrepareSafetyGogglesFragment();
        }
    }
}
