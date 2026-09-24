package fabscreen.features.machinetools.calibration.s20._30._cnc.changeassistant;

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
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.GuideProgressBar;
import fabscreen.platform.core.ui.view.XYZControlPanel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CncChangeAssistant11Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.xyz_panel_touch_platform)
    XYZControlPanel mControlPanel;
    @BindView(R2.id.btn_next)
    Button mBtNext;
    @BindView(R2.id.view_guide_progress_bar)
    GuideProgressBar mGuideProgressBar;
    private CncChangeAssistantModel mViewModel;

    public static Fragment newInstance() {
        return new CncChangeAssistant11Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("1-1 换刀助手");
        initView();
    }

    private void initView() {
        mGuideProgressBar.setmStepNum(3);
        mGuideProgressBar.setmStepIndex(1);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);

        CoordinateSystemPresenter presenter = new CoordinateSystemPresenter(disposables);
        presenter.ensureCoordinate(1);

        mViewModel = getViewModel();
        presenter.setOnCoordinateSwitchListener(() -> mViewModel.initToolheadPosition());

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


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_change_assistant_1;
    }


    @Override
    protected CncChangeAssistantModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(CncChangeAssistantModel.class);
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        mViewModel.setChangeAssistant()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mControlPanel.setEnabled(true);
                    mBtNext.setEnabled(true);
                    if (getActivity() != null) {
                        ((CncChangeAssistantActivity) getActivity()).gotToCncChangeAssistant2();
                    }
                }, e -> {
                    LogHelper.log(e);
                    mControlPanel.setEnabled(true);
                    mBtNext.setEnabled(true);
                });
    }
}
