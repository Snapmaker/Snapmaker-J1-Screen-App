package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.platformHeight;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.core.ui.view.A400DirectionControlPanel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400Calibration10WLaserPlatformHeightFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.cp_a400_calibration_move)
    A400DirectionControlPanel mCpMove;
    @BindView(R2.id.fragment_calibration_content)
    TextView mTvContent;
    @BindView(R2.id.bt_a400_calibration_submit)
    Button mBtnSubmit;
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;

    private TouchPlatformViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400Calibration10WLaserPlatformHeightFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.checkHome()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mViewModel.initToolheadPosition();
                    }
                });

    }

    private void initView() {
        setTitle(R.string.calibration_platform_height_calibration_10w_title);
        mTvContent.setText(R.string.calibration_platform_height_calibration_10w_content);
        mTvTopBarContent.setVisibility(View.INVISIBLE);
        mGuideProgressBar.setMax(1);
        mGuideProgressBar.setProgress(1);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mBtnSubmit.setText(R.string.all_save);
        mCpMove.setStepWidths(0.1f, 1f, 10f, 100f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    mViewModel.moveXYZByStep(direction, stepWidth);

                });
        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mCpMove.setEnabled(!isMoving);
                    mBtnBack.setEnabled(!isMoving);
                    mBtnSubmit.setEnabled(!isMoving);
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


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_cnc_manual_tool_change_2;
    }


    @Override
    protected TouchPlatformViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(TouchPlatformViewModel.class);
    }


    @OnClick(R2.id.bt_a400_calibration_submit)
    void onClickNext() {
        playNormalClickSound();
        mViewModel.savePlatformZOffset()
                .flatMap(responseStructure -> mViewModel.upLiftToolhead())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (getActivity() == null) return;
                    ((A400Calibration10WLaserPlatformHeightCalibrationActivity) getActivity()).gotoComplete();
                });
    }
}
