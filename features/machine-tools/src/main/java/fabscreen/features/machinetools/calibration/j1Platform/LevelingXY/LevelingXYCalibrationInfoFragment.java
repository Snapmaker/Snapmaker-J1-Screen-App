package fabscreen.features.machinetools.calibration.j1Platform.LevelingXY;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;

public class LevelingXYCalibrationInfoFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.btn_next)
    Button mBtNext;

    public static Fragment newInstance() {
        return new LevelingXYCalibrationInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_leveling_xy_calibration;
    }

    @OnCheckedChanged(R2.id.cb_check_next)
    public void onCheckChange(CompoundButton view, boolean isCheck) {
        mBtNext.setEnabled(isCheck);
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((LevelingXYCalibrationActivity) getActivity()).gotoLevelingXYPrintCalibration();
    }

    @OnClick(R2.id.bt_leveling_xy)
    void onClickXy() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToControlPage().start(getContext());
    }

}
