package fabscreen.features.guide.s20.laser._10w.touchplatform;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser._10w.Guide10wLaserActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.XYZControlPanel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("NonConstantResourceId")
public class Guide10wLaserTouchPlatformFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.xyz_panel_touch_platform)
    XYZControlPanel mControlPanel;
    @BindView(R2.id.btn_laser_10w_touch_platform_complete)
    Button mBtnComplete;
    private TouchPlatformViewModel mViewModel;

    public static Guide10wLaserTouchPlatformFragment newInstance() {
        return new Guide10wLaserTouchPlatformFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnComplete.setText(R.string.all_next);

        CoordinateSystemPresenter presenter = new CoordinateSystemPresenter(disposables);
        presenter.ensureCoordinate(1);

        mViewModel = getViewModel();
        setTitle(R.string.settings_laser_touch_platform_title);
        presenter.setOnCoordinateSwitchListener(() -> mViewModel.initToolheadPosition());

        mControlPanel.setStepWidths(0.1f, 1f, 10f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    mViewModel.moveXYZByStep(direction, stepWidth)
                            .as(bindToLifecycle())
                            .subscribe(responseStructure -> {
                                if (responseStructure.isSuccess()) {

                                } else if (responseStructure.isGeneralError()) {
                                    FabConfirm.create(getContext())
                                            .setDescription(getString(R.string.all_moving_limitation))
                                            .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                                dialog.dismiss();
                                            });
                                }
                            }, LogHelper::log);
                });

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtnBack.setEnabled(!isMoving);
                    mControlPanel.setEnabled(!isMoving);
                    mBtnComplete.setEnabled(!isMoving);
                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_10w_touch_platform;
    }

    @Override
    protected TouchPlatformViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(TouchPlatformViewModel.class);
    }

    @OnClick(R2.id.btn_laser_10w_touch_platform_complete)
    void onCompleteClicked() {
        playNormalClickSound();
        mViewModel.savePlatformZOffset();
        mViewModel.upLiftToolhead()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (getActivity() == null) return;
                    ((Guide10wLaserActivity) getActivity()).startThicknessMeasurementCalibrationIntroFragment();
                });
    }
}
