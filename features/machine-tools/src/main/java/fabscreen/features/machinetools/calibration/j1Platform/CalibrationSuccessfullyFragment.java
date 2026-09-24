package fabscreen.features.machinetools.calibration.j1Platform;

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
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CalibrationSuccessfullyFragment extends J1CalibrationBaseFragment {
    private boolean mIsGuide = false;

    public static Fragment newInstance() {
        return new CalibrationSuccessfullyFragment();
    }

    public static Fragment newInstance(boolean isGuide) {
        Fragment fragment = new CalibrationSuccessfullyFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_guide", isGuide);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mIsGuide = getArguments().getBoolean("is_guide", false);
        }
        if (mIsGuide) {
            restoringMachine();
        }
    }

    public void restoringMachine() {
        fabMoving.show();
        Vector vector = new Vector();
        vector.setZ(95);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController()
                .gotoAbsolutePosition(vector)
                .flatMap(responseStructure -> ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(true))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    fabMoving.dismiss();
                }, e -> {
                    fabMoving.dismiss();
                    LogHelper.log(e);
                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                    requireActivity().finish();
                });
    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        if (!mIsGuide) {
            ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        }
        requireActivity().finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_calibration_success;
    }
}
