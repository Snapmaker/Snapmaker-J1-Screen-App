package fabscreen.features.guide.s20.laser.autofocus;

import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GuideLaserAutoFocusStep1Fragment extends BaseFragment {
    @BindView(R2.id.btn_guide_next)
    Button mBtnNext;

    public static GuideLaserAutoFocusStep1Fragment newInstance() {
        return new GuideLaserAutoFocusStep1Fragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_auto_focus_step1;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private Observable<Boolean> gotoInitialPosition() {
        // Assume we are at CS#1
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
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
                        ((GuideLaserActivity) getActivity()).startAutoFocusStep2Fragment();
                    }
                }, e -> {
                    mBtnNext.setEnabled(true);
                    LogHelper.log(e);
                });
    }
}
