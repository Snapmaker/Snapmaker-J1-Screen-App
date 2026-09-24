package fabscreen.features.machinetools.calibration.s20._30._cnc.setorigin;

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
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.GuideProgressBar;
import fabscreen.platform.core.ui.view.XYZControlPanel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CncSetOriginFragment extends BaseFragment {
    @BindView(R2.id.view_guide_progress_bar)
    GuideProgressBar mGuideProgressBar;
    @BindView(R2.id.xyz_panel_touch_platform)
    XYZControlPanel mControlPanel;
    @BindView(R2.id.btn_next)
    Button mBtNext;
    private CncSetOriginViewModel mViewModel;

    public static Fragment newInstance() {
        return new CncSetOriginFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("1-1 手动对刀");
        initView();
    }


    private void initView() {
        mViewModel = getViewModel();
        mGuideProgressBar.setmStepNum(1);
        mGuideProgressBar.setmStepIndex(1);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);

        // 需要 记录当前位置
        CoordinateSystemPresenter presenter = new CoordinateSystemPresenter(disposables);
        presenter.ensureCoordinate(1);
        // 需要 记录恢复当前位置?
//        presenter.setOnCoordinateSwitchListener(() -> mViewModel.initMove());

        mControlPanel.setStepWidths(0.1f, 1f, 10f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    mViewModel.moveXYZByStep(direction, stepWidth);
                });

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mControlPanel.setEnabled(!isMoving);
                    mBtNext.setEnabled(!isMoving);
                });

    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        mViewModel.saveOrigin()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (getActivity() != null) {
                        ((CncSetOriginActivity) getActivity()).gotToTCncSetOriginSuccess();
                    } else {
                        mControlPanel.setEnabled(true);
                        mBtNext.setEnabled(true);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mControlPanel.setEnabled(true);
                    mBtNext.setEnabled(true);
                })

        ;
    }

    @Override
    protected void back() {
        ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        requireActivity().finish();
    }

    @Override
    protected CncSetOriginViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(CncSetOriginViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_toolhead_focus_calibration_10;
    }
}
