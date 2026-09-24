package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GuideRotaryLaserManualFocusStep1Fragment extends BaseFragment {
    @BindView(R2.id.tv_guide_laser_auto_focus_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_laser_auto_focus_content)
    TextView mTvContent;
    @BindView(R2.id.btn_guide_next)
    Button mBtnNext;

    public static GuideRotaryLaserManualFocusStep1Fragment newInstance() {
        return new GuideRotaryLaserManualFocusStep1Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_auto_focus_step1;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mBtnNext.setText(R.string.all_start);
        mTvTitle.setText(R.string.laser_calibration_manual_focus);
        mTvContent.setText(R.string.laser_calibration_manual_focus_desc);
    }

    private Observable<Boolean> gotoInitialPosition() {
        // Assume we are at CS#1
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
                .flatMap(response -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                .flatMap(response -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800"))
                .map(coordinateSystem -> true);
    }

    @OnClick(R2.id.btn_guide_next)
    void onClickNext() {
        playNormalClickSound();
        mBtnNext.setEnabled(false);

        gotoInitialPosition()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mBtnNext.setEnabled(true);

                    if (getActivity() != null) {
                        ((GuideRotaryLaserActivity) getActivity()).startManualFocusStep2Fragment();
                    }
                }, e -> {
                    mBtnNext.setEnabled(true);
                    LogHelper.log(e);
                });
    }
}
