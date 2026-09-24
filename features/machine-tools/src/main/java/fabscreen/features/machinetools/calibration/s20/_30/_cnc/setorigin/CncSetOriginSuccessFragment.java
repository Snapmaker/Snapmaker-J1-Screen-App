package fabscreen.features.machinetools.calibration.s20._30._cnc.setorigin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;

public class CncSetOriginSuccessFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new CncSetOriginSuccessFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("1-1 手动对刀");
    }

    @OnClick(R2.id.btn_next)
    void onClickBackHome() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(getContext());
    }

    @OnClick(R2.id.btn_go_to_work)
    void onClickGotoWor() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToFilesPage(3).start(getContext());
        requireActivity().finish();
    }

    @Override
    protected void back() {
        ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        requireActivity().finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_origin_success;
    }
}
