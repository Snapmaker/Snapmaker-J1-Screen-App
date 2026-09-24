package fabscreen.features.print.print;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.LoadFilamentWidgetPresenter;
import fabscreen.platform.core.ui.presenter.NozzleWidgetPresenter;
import fabscreen.platform.core.ui.view.RulerView;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintChangeFilamentFragment extends BaseFragment {
    private static final String TAG = PrintChangeFilamentFragment.class.getSimpleName();

    @BindView(R2.id.top_bar_back)
    Button mIbBack;

    @BindView(R2.id.rv_widget_set_value_ruler_ruler)
    RulerView mRvRuler;
    @BindView(R2.id.btn_print_change_filament_complete)
    Button mBtnComplete;

    private NozzleWidgetPresenter mNozzleWidgetPresenter;
    private LoadFilamentWidgetPresenter mLoadFilamentWidgetPresenter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.print_change_filament);

        initView();
        initData();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print_change_filament;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mIbBack.setVisibility(View.GONE);

        mNozzleWidgetPresenter = new NozzleWidgetPresenter(disposables);
        mNozzleWidgetPresenter.bind(getView());
        mNozzleWidgetPresenter.connectMachineStatus();

        mLoadFilamentWidgetPresenter = new LoadFilamentWidgetPresenter(disposables);
        mLoadFilamentWidgetPresenter.bind(getView());
        mLoadFilamentWidgetPresenter.connect();

        mLoadFilamentWidgetPresenter.getIsLoadingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mRvRuler.setEnabled(!isMoving);
                    mBtnComplete.setEnabled(!isMoving);
                });
    }

    private void initData() {
        mNozzleWidgetPresenter.setTargetValue(200);
    }

    @OnClick(R2.id.btn_print_change_filament_complete)
    void onClickComplete() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setResume();
        back();
    }
}
