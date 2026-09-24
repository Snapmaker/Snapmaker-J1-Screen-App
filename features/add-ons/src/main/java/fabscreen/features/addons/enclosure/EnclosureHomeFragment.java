package fabscreen.features.addons.enclosure;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.addons.R;
import fabscreen.features.addons.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class EnclosureHomeFragment extends BaseFragment {
    @BindView(R2.id.btn_widget_enclosure_led_strip)
    ActionButton mBtnEnclosureLed;
    @BindView(R2.id.btn_widget_enclosure_cooling_fan)
    ActionButton mBtnEnclosureFan;
    @BindView(R2.id.btn_enclosure_settings)
    Button mBtnEnclosureSettings;
    private EnclosureViewModel mViewModel;

    public static EnclosureHomeFragment getInstance() {
        return new EnclosureHomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_enclosure);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_enclosure;
    }

    @Override
    protected EnclosureViewModel getViewModel() {
        return getViewModelProvider().get(EnclosureViewModel.class);
    }

    private void initView() {
        mViewModel.getEnclosureStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mBtnEnclosureLed.setEnabled(status.isReady());
                    mBtnEnclosureFan.setEnabled(status.isReady());
                    mBtnEnclosureSettings.setEnabled(status.isReady());

                    mBtnEnclosureLed.setActivated(status.isLedOn());
                    mBtnEnclosureFan.setActivated(status.isFanOn());
                });
    }

    @OnClick(R2.id.btn_widget_enclosure_led_strip)
    void onClickLed() {
        playNormalClickSound();
        boolean isLedOn = mViewModel.isEnclosureLedOn();

        // We define level 0 means close the led, 1-100 means open the led (Max value 100).
        mViewModel.setLedLevel(isLedOn ? 0 : 100);
    }

    @OnClick(R2.id.btn_widget_enclosure_cooling_fan)
    void onClickFan() {
        playNormalClickSound();
        boolean isFanOn = mViewModel.isEnclosureFanOn();

        // We define level 0 means close the fan, 1-100 means open the fan (Max value 100).
        mViewModel.setFanLevel(isFanOn ? 0 : 100);
    }

    @OnClick(R2.id.btn_enclosure_settings)
    void onClickSettings() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((EnclosureActivity) getActivity()).startEnclosureSettings();
        }
    }
}
