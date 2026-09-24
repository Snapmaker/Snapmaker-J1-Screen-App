package fabscreen.features.machinetools.calibration.j1Platform.LevelingXY;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;

public class LevelingXYCalibration2Fragment extends J1CalibrationBaseFragment {

    public static Fragment newInstance() {
        return new LevelingXYCalibration2Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        initView();
    }

    private void initView() {

    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_leveling_xy_calibration_2;
    }


    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        requireActivity().finish();
    }

}
