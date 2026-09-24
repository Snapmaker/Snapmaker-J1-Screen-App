package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.A400EnclosureControlViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintA400AdjustmentEnclosureControlFragment extends BaseFragment {

    @BindView(R2.id.btn_fan)
    ActionButton mAbFan;
    @BindView(R2.id.btn_led_strip)
    ActionButton mAbLed;
    @BindView(R2.id.tv_door_status)
    TextView mTvDoorStatus;
    private A400EnclosureControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new PrintA400AdjustmentEnclosureControlFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
        mViewModel.subscribeEnclosureStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.unSubscribeEnclosureStatus();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mViewModel.getEnclosureStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(info -> {
                    int status = info.getStatus();
                    mAbLed.setEnabled(status == 2);
                    mAbFan.setEnabled(status == 2);
                    mAbLed.setActivated(info.isLedOn());
                    mAbFan.setActivated(info.isFanOn());
                    mTvDoorStatus.setText(info.isDoorOpen() ? "外罩门状态：开" : "外罩门状态：关");
                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_adjustment_enclosure;
    }

    @OnClick(R2.id.btn_led_strip)
    public void onClickLed() {
        playNormalClickSound();
        Logger.d("---FDT--- check btn_led_strip");
        boolean isLedOn = mViewModel.isEnclosureLedOn();
        Logger.d("---FDT--- check isEnclosureLedOn: mViewModel.isEnclosureLedOn()");
        // We define level 0 means close the led, 1-100 means open the led (Max value 100).
        mViewModel.setLedLevel(isLedOn ? 0 : 100);
    }

    @OnClick(R2.id.btn_fan)
    public void onClickFan() {
        playNormalClickSound();
        boolean isFanOn = mViewModel.isEnclosureFanOn();

        // We define level 0 means close the fan, 1-100 means open the fan (Max value 100).
        mViewModel.setFanLevel(isFanOn ? 0 : 100);
    }

    @Override
    protected A400EnclosureControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(A400EnclosureControlViewModel.class);
    }
}
