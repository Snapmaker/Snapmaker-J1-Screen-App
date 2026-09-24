package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;

public class A400LevelingBedCalibrationCompleteFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.tv_a400_calibration_complete_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_a400_calibration_complete_content)
    TextView mTvContent;

    public static Fragment newInstance() {
        return new A400LevelingBedCalibrationCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText(R.string.calibration_heated_bed_leveling_completed_title);
        mTvContent.setText(R.string.calibration_heated_bed_leveling_completed_content);

        getServiceContainer().getService(IMachine.class).getFDMController().exitCalibration(true).as(bindToLifecycle()).subscribe();
        playProcedureCompleteSound();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_complete;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        requireActivity().finish();
    }

    @OnClick(R2.id.btn_back_home)
    void onClickbackHome() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(getContext());
    }
}
