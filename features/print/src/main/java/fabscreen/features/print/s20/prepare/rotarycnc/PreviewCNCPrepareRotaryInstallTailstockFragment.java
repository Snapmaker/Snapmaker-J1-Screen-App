package fabscreen.features.print.s20.prepare.rotarycnc;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class PreviewCNCPrepareRotaryInstallTailstockFragment extends BaseFragment {
    public static PreviewCNCPrepareRotaryInstallTailstockFragment newInstance() {
        return new PreviewCNCPrepareRotaryInstallTailstockFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.preview_cnc_rotary_install_tailstock);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_cnc_prepare_install_tailstock;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_cnc_prepare_install_tailstock_next)
    void onClickNext() {
        playNormalClickSound();
        Bundle arguments = getArguments();
        if (arguments == null) {
            Logger.w("Arguments not exist.");
            return;
        }
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeToPrintPage()
                .start(getContext());
    }
}
