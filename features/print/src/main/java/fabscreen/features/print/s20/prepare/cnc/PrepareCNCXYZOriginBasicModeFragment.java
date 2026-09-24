package fabscreen.features.print.s20.prepare.cnc;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrepareCNCXYZOriginBasicModeFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("2-1 准备CNC作业");
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_prepare_cnc_basic_mode;
    }

    public void closePrepare() {
        requireActivity().finish();
    }

    @OnClick(R2.id.btn_prepare_cnc_basic_mode_next)
    void onClickNext() {
        playNormalClickSound();
        AndroidSchedulers.mainThread().scheduleDirect(this::closePrepare, 200, TimeUnit.MILLISECONDS);
        ServiceContainer.getInstance().getService(IRouter.class).routeToPrintPage().start(getContext());
    }

    @OnClick(R2.id.btn_prepare_cnc_basic_mode_select_mode)
    void onClickSelectMode() {
        playNormalClickSound();
        ((PrepareCNCActivity) requireActivity()).gotoCNCXYZOriginSelectModeFragment();
    }

}

