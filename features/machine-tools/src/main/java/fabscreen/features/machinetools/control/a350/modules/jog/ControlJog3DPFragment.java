package fabscreen.features.machinetools.control.a350.modules.jog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ControlJog3DPFragment extends ControlJogFragment {
    @BindView(R2.id.btn_widget_coordinate_home)
    ActionButton mBtnHome;

    private ControlJog3DPFragment() {
    }

    public static Fragment newInstance() {
        return new ControlJog3DPFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void modifyViewBeforeBind(View rootView) {
        super.modifyViewBeforeBind(rootView);
        View view;
        if (mViewModel.isRotaryAvailable()) {
            view = LayoutInflater.from(requireContext()).inflate(R.layout.widget_coordinate_xyzb_panel, (ViewGroup) rootView, false);
        } else {
            view = LayoutInflater.from(requireContext()).inflate(R.layout.widget_coordinate_panel, (ViewGroup) rootView, false);
        }
        if (rootView == null) return;
        ViewGroup container = (ViewGroup) rootView;
        container.addView(view, 0);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_control_jog;
    }

    protected void initView() {
        super.initView();

        // Home button status
        mViewModel.getHomeButtonMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtnHome.setActivated(isMoving);
                    mBtnHome.setEnabled(!isMoving);
                });
    }

    @Override
    protected void setCoordinateValue() {
        // set coordinate xyzb value
        mViewModel.getMachineStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    float x = (float) machineStatus.x;
                    float y = (float) machineStatus.y;
                    float z = (float) machineStatus.z;
                    float b = (float) machineStatus.b;

                    mTvAbsoluteX.setText(String.format(Locale.US, "%.2f", x));
                    mTvAbsoluteY.setText(String.format(Locale.US, "%.2f", y));
                    mTvAbsoluteZ.setText(String.format(Locale.US, "%.2f", z));
                    if (mTvAbsoluteBValue != null) {
                        mTvAbsoluteBValue.setText(String.format(Locale.US, "%.2f", b));
                    }
                });
    }

    @OnClick(R2.id.btn_widget_coordinate_home)
    void onClickHome() {
        playNormalClickSound();
        mViewModel._3DPGoHome();
    }
}
