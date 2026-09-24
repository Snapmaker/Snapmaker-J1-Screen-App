package fabscreen.features.print.s20.prepare.rotarylaser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class PreviewLaserRotaryPrepareModeFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.laser_choose_mode);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_mode;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_prepare_auto)
    void onClickAutoMode() {
        playNormalClickSound();
        Logger.i("Choose auto focus mode.");
        PreviewActivity activity = (PreviewActivity) getContext();
        if (activity != null) {
            activity.gotoLaserRotarySetMaterial();
        }
    }

    @OnClick(R2.id.btn_preview_laser_prepare_manual)
    void onClickManualMode() {
        playNormalClickSound();
        Logger.i("Choose manual focus mode.");
        PreviewActivity activity = (PreviewActivity) getContext();
        if (activity != null) {
            activity.gotoLaserPrepareSafetyGogglesFragment(false);
        }
    }
}
