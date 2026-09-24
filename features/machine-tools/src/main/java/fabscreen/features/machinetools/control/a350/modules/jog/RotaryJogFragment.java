package fabscreen.features.machinetools.control.a350.modules.jog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.machinetools.R;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RotaryJogFragment extends BaseFragment {
    @BindView(R2.id.sbg_control_b_axis_steps)
    SegmentedButtonGroup mSbgBAxisSteps;
    @BindView(R2.id.btn_widget_b_axis_clockwise)
    ActionButton mBtnBAxisClockwise;
    @BindView(R2.id.btn_widget_b_axis_counterclockwise)
    ActionButton mBtnBAxisCounterClockwise;
    private RotaryJogViewModel mViewModel;

    private RotaryJogFragment() {
    }

    public static Fragment newInstance() {
        return new RotaryJogFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mSbgBAxisSteps.setOnPositionChangedListener(position -> mViewModel.changeStepWidth(position));
        mSbgBAxisSteps.setPosition(1, false);
        mViewModel.getMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> setButtonsEnabled(!isMoving));
        mBtnBAxisClockwise.setOnClickListener(v -> mViewModel.spin(true));
        mBtnBAxisCounterClockwise.setOnClickListener(v -> mViewModel.spin(false));
    }

    private void setButtonsEnabled(boolean enabled) {
        mBtnBAxisClockwise.setEnabled(enabled);
        mBtnBAxisCounterClockwise.setEnabled(enabled);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.widget_control_panel_b_axis;
    }

    @Override
    protected RotaryJogViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(RotaryJogViewModel.class);
    }
}
