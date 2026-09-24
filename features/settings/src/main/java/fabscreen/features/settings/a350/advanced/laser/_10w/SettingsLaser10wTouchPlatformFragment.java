package fabscreen.features.settings.a350.advanced.laser._10w;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.XYZControlPanel;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("NonConstantResourceId")
public class SettingsLaser10wTouchPlatformFragment extends BaseFragment {

    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.xyz_panel_touch_platform)
    XYZControlPanel mControlPanel;
    @BindView(R2.id.btn_laser_10w_touch_platform_complete)
    Button mBtnComplete;
    private TouchPlatformViewModel mViewModel;

    public static Fragment getInstance() {
        return new SettingsLaser10wTouchPlatformFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = getViewModel();
        setTitle(R.string.settings_laser_touch_platform_title);

        CoordinateSystemPresenter presenter = new CoordinateSystemPresenter(disposables);
        presenter.ensureCoordinate(1);

        presenter.setOnCoordinateSwitchListener(() -> mViewModel.initToolheadPosition());

        mControlPanel.setStepWidths(0.1f, 1f, 10f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    mViewModel.moveXYZByStep(direction, stepWidth);
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
                .subscribe(success ->
                        requireFragmentManager().popBackStack(SettingsAdvancedLaser10WFragment.class.getSimpleName(), 0));
    }

    @Override
    protected void back() {
        requireFragmentManager().popBackStack(
                SettingsLaser10wToolheadFocusCalibrationFragment.class.getSimpleName(),
                1
        );
    }
}
