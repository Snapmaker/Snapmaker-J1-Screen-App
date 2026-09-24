package fabscreen.features.settings.a350.advanced.laser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LaserFocusModificationFragment extends BaseFragment {
    private LaserFocusPresenter mPresenter;

    public static LaserFocusModificationFragment newInstance() {
        return new LaserFocusModificationFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_focus_modification;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mPresenter = new LaserFocusPresenter(disposables);
        mPresenter.bind(getView());
        mPresenter.connect();
        boolean isRotaryAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
        mPresenter.setMaxValue(isRotaryAvailable ? 150 : 40);
    }

    @OnClick(R2.id.btn_laser_focus_modification_save)
    void onClickSave() {
        playNormalClickSound();
        final float laserFocus = mPresenter.getTargetValue();
        Logger.d("Set laser focus " + laserFocus);

        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setFocalLength(laserFocus)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> back(), LogHelper::log);
    }
}
