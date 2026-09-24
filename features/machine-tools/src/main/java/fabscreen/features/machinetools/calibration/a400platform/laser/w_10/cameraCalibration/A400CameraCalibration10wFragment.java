package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.cameraCalibration;

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
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class A400CameraCalibration10wFragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.tv_a400_calibration_camera_info_title)
    TextView mTvSubTitle;
    @BindView(R2.id.tv_a400_calibration_camera_info)
    TextView mTvContent;

    private final BehaviorSubject<Boolean> mIsMovePopUpSubject = BehaviorSubject.createDefault(false);

    public static Fragment newInstance() {
        return new A400CameraCalibration10wFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        checkHome()
                .as(bindToLifecycle())
                .subscribe(order -> {
                }, LogHelper::log);

    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovePopUpSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .doOnNext(machineStatus -> {
                        mIsMovePopUpSubject.onNext(false);
                    })
                    .flatMap(integer -> Observable.just(integer == 0));
        } else {
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed));
        }
    }

    private void initView() {
        setTitle(R.string.calibration_camera_calibration_10w_title);
        mTvTopBarContent.setText(R.string.calibration_camera_calibration_10w_content);
        mTvSubTitle.setText(R.string.guide_a400_camera_calibration_1_2_subtitle);
        mTvContent.setText(R.string.guide_a400_camera_calibration_1_2_msg);
        mGuideProgressBar.setMax(2);
        mGuideProgressBar.setProgress(1);
        mIsMovePopUpSubject.
                observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        fabLoading.show();
                    } else {
                        fabLoading.dismiss();
                    }
                });
    }


    @OnClick(R2.id.bt_a400_calibration_camera_info_next)
    public void onClickNext() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.TIP_TYPE)
                .setPic(R.drawable.ic_laser_glass)
                .setTitle(R.string.open_laser_titie)
                .setContent(R.string.calibration_laser_on_reminder)
                .setCanceledOnTouchOutSide(true)
                .setFirstTv(getResources().getString(R.string.all_cancel), R.color.select_dialog_left_text_color, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getResources().getString(R.string.all_confirm), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    ((A400CameraCalibration10wActivity) requireActivity()).gotoCalibrationLaser();
                })
                .show();
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_camrea_info;
    }
}
