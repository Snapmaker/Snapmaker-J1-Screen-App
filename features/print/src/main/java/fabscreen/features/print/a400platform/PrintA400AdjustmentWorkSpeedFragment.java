package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintA400AdjustmentWorkSpeedFragment extends BaseFragment {
    private static final int WORK_SPEED_MAX_VALUE = 200;
    private static final int WORK_SPEED_MIN_VALUE = 1;

    @BindView(R2.id.cas_j1_print_adjustment_progress)
    CustomArcSeekBar mCsbCurrent;
    @BindView(R2.id.tv_j1_print_adjustment_current)
    TextView mTvCurrent;
    @BindView(R2.id.btn_print_adjustment_cancel)
    Button mBtnCancel;
    @BindView(R2.id.btn_print_adjustment_confirm)
    Button mBtnConfirm;
    @BindView(R2.id.tv_progress_value)
    TextView mTvProgressValue;

    public static Fragment newInstance() {
        return new PrintA400AdjustmentWorkSpeedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

        updateWorkSpeed();
    }

    private void initView() {
        final int deltaValue = WORK_SPEED_MAX_VALUE - WORK_SPEED_MIN_VALUE;
        mCsbCurrent.setMax(deltaValue);
        if (getParentFragment() instanceof PrintA400AdjustmentContainerFragment) {
            ((PrintA400AdjustmentContainerFragment) getParentFragment()).setModelTitle(getString(R.string.a400_print_print_setting_part_work_speed));
        }

        mCsbCurrent.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvCurrent.setText(progress + WORK_SPEED_MIN_VALUE + "");
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                        .setExtruderWorkSpeed(0, 0, customArcSeekBar.getProgress() + WORK_SPEED_MIN_VALUE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(responseStructure -> updateWorkSpeed());
            }
        });

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_adjustment_work_speed;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    void updateWorkSpeed() {
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                .getExtruderWorkSpeed(IMachine.WorkType.FDM, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    int key = (int) baseStructure.getProp("key").getValue();
                    ArrayList<UInt16Prop> workSpeedList = (ArrayList<UInt16Prop>) baseStructure.getProp("workSpeed").getValue();
                    Logger.d("key %d, arrayList workSpeed %s", key, workSpeedList.toString());
                    mCsbCurrent.setProgress(workSpeedList.get(0).getValue() - WORK_SPEED_MIN_VALUE);
                });
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @OnClick(R2.id.btn_print_adjustment_cancel)
    public void onClickCancel() {

    }

    @OnClick(R2.id.btn_print_adjustment_confirm)
    public void onClickConfirm() {

    }

}