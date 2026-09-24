package fabscreen.features.machinetools.control.a350.modules.jog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.SteeringView;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LinearJogFragment extends BaseFragment {

    @BindView(R2.id.sbg_control_steps)
    SegmentedButtonGroup mSbgStepWidths;
    @BindView(R2.id.sv_control_panel_xy)
    SteeringView mSvControlXY;
    @BindView(R2.id.btn_control_panel_z_plus)
    Button mBtnControlZPlus;
    @BindView(R2.id.btn_control_panel_z_minus)
    Button mBtnControlZMinus;
    private LinearJogViewModel mViewModel;

    private LinearJogFragment() {
    }

    public static Fragment newInstance() {
        return new LinearJogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    protected LinearJogViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(LinearJogViewModel.class);
    }

    private void initView() {
        mSbgStepWidths.setOnPositionChangedListener(position -> mViewModel.changeStepWidth(position));
        mSbgStepWidths.setPosition(1, false);

        mSvControlXY.setOnDirectionClickedListener(direction -> {
            mViewModel.moveXYZByStep(direction);
            playNormalClickSound();
        });
        mBtnControlZMinus.setOnClickListener(v -> mViewModel.moveXYZByStep(MoveController.Direction.DOWN));
        mBtnControlZPlus.setOnClickListener(v -> mViewModel.moveXYZByStep(MoveController.Direction.UP));

        mViewModel.getMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> setButtonsEnabled(!isMoving));
    }

    private void setButtonsEnabled(boolean enabled) {
        mBtnControlZPlus.setEnabled(enabled);
        mBtnControlZMinus.setEnabled(enabled);
        mSvControlXY.setEnabled(enabled);
    }

    @Override
    protected int getLayoutResID() {
        return mViewModel.isRotaryAvailable() ? R.layout.widget_control_panel_xyz_axes_for_4axis : R.layout.widget_control_panel_xyz_axes;
    }
}
