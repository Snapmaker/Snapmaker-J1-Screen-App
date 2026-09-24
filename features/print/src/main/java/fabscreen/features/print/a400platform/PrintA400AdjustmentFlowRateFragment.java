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
import fabscreen.features.print.j1platform.PrintJ1AdjustmentFlowRateFragment;
import fabscreen.features.print.j1platform.viewmodel.PrintJ1AdjustmentExtruderViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintA400AdjustmentFlowRateFragment extends BaseFragment {
    private static final int FLOW_RATE_MAX_VALUE = 100;
    private static final int FLOW_RATE_MIN_VALUE = 1;

    @BindView(R2.id.cas_target_left)
    CustomArcSeekBar mCasTargetLeft;
    @BindView(R2.id.cas_target_right)
    CustomArcSeekBar mCasTargetRight;
    @BindView(R2.id.tv_l_current_temp)
    TextView mTvCurrentLTemp;
    @BindView(R2.id.tv_r_current_temp)
    TextView mTvCurrentRTemp;
    @BindView(R2.id.tv_l_t_setting)
    TextView mTvLTSetting;
    @BindView(R2.id.tv_r_t_setting)
    TextView mTvRTSetting;
    @BindView(R2.id.btn_print_adjustment_cancel)
    Button mBtnCancel;
    @BindView(R2.id.btn_print_adjustment_confirm)
    Button mBtnConfirm;
    @BindView(R2.id.tv_progress_value)
    TextView mTvProgressValue;

    private PrintJ1AdjustmentExtruderViewModel mViewModel;
    private PrintController mPrintController;
    public static final int LEFT_NOZZLE_TYPE = 101;
    public static final int RIGHT_NOZZLE_TYPE = 102;
    private int mNozzleType;

    public static Fragment newInstance(int nozzleType) {
        PrintA400AdjustmentFlowRateFragment flowRateFragment = new PrintA400AdjustmentFlowRateFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("nozzleType", nozzleType);
        flowRateFragment.setArguments(bundle);
        return flowRateFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNozzleType = requireArguments().getInt("nozzleType", LEFT_NOZZLE_TYPE);

        mPrintController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mViewModel = getViewModel();
        initView();

        getProgress(0, 0);
        getProgress(1, 0);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_adjustment_flow_rate;
    }

    private void initView() {
        if (getParentFragment() instanceof PrintA400AdjustmentContainerFragment) {
            ((PrintA400AdjustmentContainerFragment) getParentFragment()).setModelTitle(getString(
                    mNozzleType==LEFT_NOZZLE_TYPE?
                            R.string.a400_print_print_setting_left_flow_rate:
                            R.string.a400_print_print_setting_right_flow_rate));
        }

        int deltaValue = FLOW_RATE_MAX_VALUE - FLOW_RATE_MIN_VALUE;
        mCasTargetLeft.setMax(deltaValue);
        mCasTargetRight.setMax(deltaValue);

        // listen seekbar
        mCasTargetLeft.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvLTSetting.setText((progress + FLOW_RATE_MIN_VALUE) + "");
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                mPrintController.setFDMFlowRate(0, 0, customArcSeekBar.getProgress() + FLOW_RATE_MIN_VALUE).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> getProgress(0, 0));

            }
        });

        mCasTargetRight.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvRTSetting.setText((progress + FLOW_RATE_MIN_VALUE) + "");
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                mPrintController.setFDMFlowRate(1, 0, customArcSeekBar.getProgress() + FLOW_RATE_MIN_VALUE).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> getProgress(1, 0));
            }
        });
    }

    @Override
    protected PrintJ1AdjustmentExtruderViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(PrintJ1AdjustmentExtruderViewModel.class);
    }

    void getProgress(int index, int extruderIndex) {
        mPrintController.getFDMFlowRate(index, extruderIndex).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
            BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
            int key = (int) baseStructure.getProp("key").getValue();
            ArrayList<UInt16Prop> flowRateList = (ArrayList<UInt16Prop>) baseStructure.getProp("flowRateArray").getValue();
            Logger.d("key %d, arrayList flowRate %s", key, flowRateList.toString());
            if (index == 0) {
                mTvCurrentLTemp.setText("" + flowRateList.get(extruderIndex).getValue());
                mCasTargetLeft.setProgress(flowRateList.get(extruderIndex).getValue() - FLOW_RATE_MIN_VALUE);
            } else {
                mTvCurrentRTemp.setText("" + flowRateList.get(extruderIndex).getValue());
                mCasTargetRight.setProgress(flowRateList.get(extruderIndex).getValue() - FLOW_RATE_MIN_VALUE);
            }
        });
    }

    @OnClick(R2.id.btn_print_adjustment_cancel)
    public void onClickCancel() {

    }

    @OnClick(R2.id.btn_print_adjustment_confirm)
    public void onClickConfirm() {

    }

}
