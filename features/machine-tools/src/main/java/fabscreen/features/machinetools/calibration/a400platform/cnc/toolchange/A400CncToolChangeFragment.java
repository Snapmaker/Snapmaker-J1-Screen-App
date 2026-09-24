package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.A400CncManualToolViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.ui.view.XYZControlPanelFourRange;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class A400CncToolChangeFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.fragment_calibration_image)
    ImageView mIvImage;
    @BindView(R2.id.fragment_calibration_title)
    TextView mTvTitle;
    @BindView(R2.id.fragment_calibration_content)
    TextView mTvContent;
    @BindView(R2.id.cp_a400_calibration_move)
    XYZControlPanelFourRange mXYZControlPanel;
    @BindView(R2.id.bt_a400_calibration_submit)
    Button mBtnNext;
    PublishSubject<Boolean> mIsMovePopUpSubject = PublishSubject.create();

    private A400CncManualToolViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400CncToolChangeFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        checkHome().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe();
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovePopUpSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(integer -> service.getMachineController().updateCoordinateSystem(1))
                    .doOnNext(machineStatus -> {
                        mIsMovePopUpSubject.onNext(false);
                    })
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed));
        } else {
            return Observable.just(true);
        }
    }

    private void initView() {
        setTitle(R.string.calibration_cnc_tool_change);
        mTvTopBarContent.setText(R.string.calibration_cnc_tool_change_first_steps);
        mTvTitle.setText(R.string.calibration_cnc_tool_change_first_steps_content_title);
        mTvContent.setText(R.string.calibration_cnc_tool_change_first_steps_content);
        mBtnNext.setText(R.string.all_next);
        mGuideProgressBar.setMax(3);
        mGuideProgressBar.setProgress(1);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mXYZControlPanel.setStepWidths(0.1f, 1f, 10f, 50f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    playNormalClickSound();
                    mViewModel.move(direction, stepWidth);
                });
        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mXYZControlPanel.setEnabled(!isMoving);
                });
        mIsMovePopUpSubject.observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(aBoolean -> {
            if (aBoolean) {
                fabLoading.show();
            } else {
                fabLoading.dismiss();
            }
        });
        mViewModel.getIsMachineMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    if (isMoving) {
                        fabLoading.show();
                    } else {
                        fabLoading.dismiss();
                    }
                });
    }

    @OnClick(R2.id.bt_a400_calibration_submit)
    public void onClickSubmit() {
        playNormalClickSound();
        mViewModel.setZ()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        ((A400CncToolChangeAssistantActivity) requireActivity()).gotoCncReplacement();
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_cnc_manual_tool_basic;
    }

    @Override
    protected A400CncManualToolViewModel getViewModel() {
        return getViewModelProvider().get(A400CncManualToolViewModel.class);
    }
}
