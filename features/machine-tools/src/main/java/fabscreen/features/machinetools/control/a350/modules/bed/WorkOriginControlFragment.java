package fabscreen.features.machinetools.control.a350.modules.bed;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.features.machinetools.R;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class WorkOriginControlFragment extends BaseFragment {

    @BindView(R2.id.tv_widget_coordinate_absolute_x_value)
    TextView mTvAbsoluteX;
    @BindView(R2.id.tv_widget_coordinate_absolute_y_value)
    TextView mTvAbsoluteY;
    @BindView(R2.id.tv_widget_coordinate_absolute_z_value)
    TextView mTvAbsoluteZ;
    @Nullable
    @BindView(R2.id.tv_widget_coordinate_absolute_b_value)
    TextView mTvAbsoluteB;

    @BindView(R2.id.tv_widget_coordinate_relative_x_value)
    TextView mTvRelativeX;
    @BindView(R2.id.tv_widget_coordinate_relative_y_value)
    TextView mTvRelativeY;
    @BindView(R2.id.tv_widget_coordinate_relative_z_value)
    TextView mTvRelativeZ;
    @Nullable
    @BindView(R2.id.tv_widget_coordinate_relative_b_value)
    TextView mTvRelativeB;

    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin)
    ActionButton mBtnPageSetOriginSetOrigin;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_x)
    ActionButton mBtnPageSetOriginSetOriginX;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_y)
    ActionButton mBtnPageSetOriginSetOriginY;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_z)
    ActionButton mBtnPageSetOriginSetOriginZ;
    @Nullable
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_b)
    ActionButton mBtnPageSetOriginSetOriginB;
    @BindView(R2.id.btn_control_laser_page_set_origin_goto_origin)
    ActionButton mBtnPageSetOriginGotoOrigin;
    @BindView(R2.id.btn_control_laser_page_set_origin_home)
    ActionButton mBtnHome;

    private WorkOriginControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new WorkOriginControlFragment();
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

    private void initView() {
        mViewModel.getMachineStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::setMachineStatus);

        mViewModel.getMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> setButtonsEnabled(!isMoving));

        mViewModel.getButtonActiveObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::setButtonActive);
    }

    private void setButtonActive(WorkOriginControlViewModel.ActiveStatus activeStatus) {
        int viewId = activeStatus.viewId;
        boolean isActive = activeStatus.isActive;
        if (viewId == R.id.btn_control_laser_page_set_origin_set_origin_x) {
            mBtnPageSetOriginSetOriginX.setActivated(isActive);
        } else if (viewId == R.id.btn_control_laser_page_set_origin_set_origin_y) {
            mBtnPageSetOriginSetOriginY.setActivated(isActive);
        } else if (viewId == R.id.btn_control_laser_page_set_origin_set_origin_z) {
            mBtnPageSetOriginSetOriginZ.setActivated(isActive);
        } else if (viewId == R.id.btn_control_laser_page_set_origin_set_origin_b) {
            mBtnPageSetOriginSetOriginB.setActivated(isActive);
        } else if (viewId == R.id.btn_control_laser_page_set_origin_set_origin) {
            mBtnPageSetOriginSetOrigin.setActivated(isActive);
        } else if (viewId == R.id.btn_control_laser_page_set_origin_goto_origin) {
            mBtnPageSetOriginGotoOrigin.setActivated(isActive);
        } else if (viewId == R.id.btn_control_laser_page_set_origin_home) {
            mBtnHome.setActivated(isActive);
        }
    }


    private void setButtonsEnabled(boolean enabled) {
        mBtnPageSetOriginSetOrigin.setEnabled(enabled);
        mBtnPageSetOriginSetOriginX.setEnabled(enabled);
        mBtnPageSetOriginSetOriginY.setEnabled(enabled);
        mBtnPageSetOriginSetOriginZ.setEnabled(enabled);
        mBtnPageSetOriginGotoOrigin.setEnabled(enabled);
        if (mBtnPageSetOriginSetOriginB != null) {
            mBtnPageSetOriginSetOriginB.setEnabled(enabled);
        }
        mBtnHome.setEnabled(enabled);
    }

    private void setMachineStatus(DeprecatedMachineInfo machineInfo) {
        float x = (float) machineInfo.x;
        float y = (float) machineInfo.y;
        float z = (float) machineInfo.z;
        float b = (float) machineInfo.b;

        mTvAbsoluteX.setText(String.format(Locale.US, "%.2f", x - mViewModel.getOffsetX()));
        mTvAbsoluteY.setText(String.format(Locale.US, "%.2f", y - mViewModel.getOffsetY()));
        mTvAbsoluteZ.setText(String.format(Locale.US, "%.2f", z - mViewModel.getOffsetZ()));

        // B Axis is implement by Rotary Module instead of Linear Module.
        // For now there is no "machine offset" concept in rotating movement.
        if (mTvAbsoluteB != null) {
            mTvAbsoluteB.setText(String.format(Locale.US, "%.2f", b));
        }

        mTvRelativeX.setText(String.format(Locale.US, "%.2f", x));
        mTvRelativeY.setText(String.format(Locale.US, "%.2f", y));
        mTvRelativeZ.setText(String.format(Locale.US, "%.2f", z));
        if (mTvRelativeB != null) {
            mTvRelativeB.setText(String.format(Locale.US, "%.2f", b));
        }
    }

    @Override
    protected WorkOriginControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(WorkOriginControlViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return mViewModel.isRotaryAvailable() ? R.layout.fragment_control_laser_page_4axis_set_origin : R.layout.fragment_control_laser_page_set_origin;
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_x)
    void onClickSetOriginX(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setX(0);
        mViewModel.setOriginByFlag(vector, view.getId());
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_y)
    void onClickSetOriginY(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setY(0);
        mViewModel.setOriginByFlag(vector, view.getId());
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_z)
    void onClickSetOriginZ(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setZ(0);
        mViewModel.setOriginByFlag(vector, view.getId());
    }

    @Optional
    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_b)
    void onClickSetOriginB(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setB(0);
        mViewModel.setOriginByFlag(vector, view.getId());
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin)
    void onClickSetOrigin(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        if (mViewModel.isRotaryAvailable()) {
            vector.setB(0);
        }
        mViewModel.setOriginByFlag(vector, view.getId());
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_goto_origin)
    void onClickGotoOrigin(View view) {
        playNormalClickSound();
        mViewModel.gotoOrigin(view.getId());
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_home)
    void onClickHome(View view) {
        playNormalClickSound();
        mViewModel.goHome(view.getId());
    }
}
