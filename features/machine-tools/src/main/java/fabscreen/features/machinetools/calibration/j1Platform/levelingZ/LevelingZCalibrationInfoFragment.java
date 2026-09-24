package fabscreen.features.machinetools.calibration.j1Platform.levelingZ;

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
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LevelingZCalibrationInfoFragment extends J1CalibrationBaseFragment {
    FDMController mFdmController;
    @BindView(R2.id.btn_next)
    Button mBtNext;

    public static Fragment newInstance() {
        return new LevelingZCalibrationInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mBtNext.setEnabled(false);
    }

    @OnCheckedChanged(R2.id.cb_leveling_bed_calibration_prepare_check)
    public void onCheckChange(CompoundButton view, boolean isCheck) {
        mBtNext.setEnabled(isCheck);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_bed_calibration_prepare;
    }


    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        fabMoving.show();
        mFdmController.setCalibrationMode(51)
                .flatMap(success -> success.isSuccess() ? mFdmController.moveZCalibrationIndex(0, 1) : Observable.just(success))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    fabMoving.dismiss();
                    if (success.isSuccess()) {
                        if (getActivity() == null) return;
                        ((LevelingZCalibrationActivity) getActivity()).gotoLevelingZCalibrationLInstructions();
                    } else if (success.isGeneralError()) {
                        back();
                    }

                });

    }
}
