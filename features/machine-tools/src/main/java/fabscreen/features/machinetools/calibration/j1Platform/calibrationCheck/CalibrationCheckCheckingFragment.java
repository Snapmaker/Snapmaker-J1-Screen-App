package fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;

public class CalibrationCheckCheckingFragment extends J1CalibrationBaseFragment {

    @BindView(R2.id.btn_next)
    Button mBtNext;

    boolean isGuide = false;

    public static Fragment newInstance() {
        return new CalibrationCheckCheckingFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            isGuide = getArguments().getBoolean("is_guide", false);
        }
        if (isGuide) {
            mBtNext.setText(R.string.all_next);
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideCheckPrint(true);
        }
    }

    @OnClick(R2.id.btn_next)
    public void onNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        requireActivity().finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_check_checking;
    }
}
