package fabscreen.features.machinetools.calibration;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.core.ui.view.dialog.RemoveGlassPlateDialogFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class J1CalibrationBaseFragment extends BaseFragment {
    protected FileLoadingDialog fabMoving;
    protected FileLoadingDialog fabHoming;
    DecisionDialog decisionDialog;
    private boolean isGuide;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabMoving = FileLoadingDialog.create(requireContext(), true);
        fabMoving.setContent(getString(R.string.j1_calibration_moving));
        fabHoming = FileLoadingDialog.create(requireContext(), true);
        fabHoming.setContent(getString(R.string.j1_calibration_moving));
        if (getArguments() != null) {
            isGuide = getArguments().getBoolean("is_guide", false);
        }
    }

    @Override
    protected void back() {
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                .setContent(R.string.j1_calibration_quit_msg)
                .setType(DecisionDialog.WARMING_TYPE)
                .setContentColor(R.color.palette_grey_french)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_left_text_color, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getString(R.string.all_quit), R.color.palette_red_sunset, ((dialog, which) -> {
                    fabMoving.show();
                    dialog.dismiss();
                    ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                            .exitCalibration(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                fabMoving.dismiss();
                                if (success.isSuccess()) {
                                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                                    finishActivityWithResultOk();
                                }
                            });
                }))
                .show();
    }

    protected void errorBack(String ProcessName, int initTemperature) {
        Logger.e(ProcessName + initTemperature);
        decisionDialog = DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                .setTitle(R.string.calibration_failed_title)
                .setContent(R.string.calibration_failed_content)
                .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                            .exitCalibration(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                fabMoving.dismiss();
                                if (success.isSuccess()) {
                                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                                    finishActivityWithResultOk();
                                }
                            });
                    dialog.dismiss();
                    fabMoving.show();
                }));
        decisionDialog.show();
    }

    protected void showRemovePlateDialog(int initTemperature) {
        if (isGuide) {
            heating();
        } else {
            RemoveGlassPlateDialogFragment.newInstance(initTemperature)
                    .setOnClickListener(this::heating)
                    .show(getChildFragmentManager(), "remove_plate");
        }
    }

    public void heating() {
    }
}
