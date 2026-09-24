package fabscreen.features.guide.s20._3dp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.LoadFilamentWidgetPresenter;
import fabscreen.platform.core.ui.presenter.NozzleWidgetPresenter;
import fabscreen.platform.base.view.FabConfirm;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Guide3DPPrepareFilamentFragment extends BaseFragment {
    @BindView(R2.id.btn_guide_3dp_prepare_filament_next)
    Button mBtnNext;
    private NozzleWidgetPresenter mNozzleWidgetPresenter;
    private LoadFilamentWidgetPresenter mLoadFilamentWidgetPresenter;

    public static Guide3DPPrepareFilamentFragment newInstance() {
        return new Guide3DPPrepareFilamentFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.control_load_filament);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_3dp_prepare_filament;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mNozzleWidgetPresenter = new NozzleWidgetPresenter(disposables);
        mNozzleWidgetPresenter.bind(getView());
        mNozzleWidgetPresenter.connectMachineStatus();

        mLoadFilamentWidgetPresenter = new LoadFilamentWidgetPresenter(disposables);
        mLoadFilamentWidgetPresenter.bind(getView());
        mLoadFilamentWidgetPresenter.connect();

        mLoadFilamentWidgetPresenter.getReadyToLoadObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(readyToLoad -> mBtnNext.setEnabled(readyToLoad));

        FabConfirm.create(getContext())
                .setDescription(R.string.control_heat_warning)
                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                    dialog.dismiss();
                    mNozzleWidgetPresenter.setTargetValue(200);
                })
                .setCancel(R.string.all_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnHeadOff() {
        // Set the temperature off
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M104 S0");
    }

    @OnClick(R2.id.btn_guide_3dp_prepare_filament_next)
    void onClickNext() {
        playNormalClickSound();
        turnHeadOff()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (getActivity() == null) return;
                    ((Guide3DPActivity) getActivity()).startCompleteFragment();
                });
    }

    @Override
    protected void back() {
        // Set the temperature off
        turnHeadOff()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    super.back();
                });
    }
}
