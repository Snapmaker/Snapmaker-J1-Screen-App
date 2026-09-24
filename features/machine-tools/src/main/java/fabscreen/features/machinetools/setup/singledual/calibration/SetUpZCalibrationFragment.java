package fabscreen.features.machinetools.setup.singledual.calibration;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class SetUpZCalibrationFragment extends BaseFragment {

    private SetUpZCaliViewModel mViewModel;

    public static Fragment newInstance() {
        return new SetUpZCalibrationFragment();
    }

    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_sub_title)
    TextView mTvSubTitle;
    @BindView(R2.id.progress)
    LinearProgressIndicator mProgress;
    @BindView(R2.id.btn_close)
    Button mBtnClose;
    @BindView(R2.id.iv_demonstrate)
    ImageView mIvSetupDemonstrate;
    @BindView(R2.id.tv_demonstrate_desc)
    TextView mTvSetupDesc;
    @BindView(R2.id.btn_start_or_next)
    Button mBtnStartOrNext;

    @BindView(R2.id.view_cali_points)
    View mVCaliPoints;
    @BindView(R2.id.tv_cali_desc)
    TextView mTvCaliDesc;
    @BindView(R2.id.tv_time_estimation)
    TextView mTvTimeEstimation;
    @BindView(R2.id.iv_guide_problem)
    ImageView mIvGuideProblem;
    IPreferences.Helper helper;
    private int mBedCalibrationBedTemperature;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(SetUpZCaliViewModel.class);
        initView();
        helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        mBedCalibrationBedTemperature = helper.getA400LevelingBedCalibrationBedTemperature();
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                .setAllExtruderTemperature(150)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed()
                .setAllTargetTemperature(50)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }

    private void initView() {
        mTvTitle.setText(R.string.guide_a400_double_extruder_step_2_title);
        mProgress.setVisibility(View.INVISIBLE);
        mTvSubTitle.setVisibility(View.INVISIBLE);
        mTvSetupDesc.setText(R.string.guide_a400_double_extruder_step_2_msg);
        mBtnStartOrNext.setText(R.string.all_start);
        mBtnClose.setVisibility(View.INVISIBLE);
        mIvGuideProblem.setVisibility(View.GONE);
    }

    @OnClick(R2.id.btn_start_or_next)
    void onStartOrNextClicked() {
        playNormalClickSound();
        mViewModel.setCaliMode(52)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        mRouter.routeToZCalibration().startForResult(this, 1);
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_guide_setup;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("req code%1$d, res code%2$d", requestCode, resultCode);
        mBtnStartOrNext.setEnabled(false);
        if (resultCode != RESULT_OK) {
            requireActivity().finish();
            return;
        }

        switch (requestCode) {
            case 1:
                mViewModel.setCaliMode(2)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(response -> {
                            if (response.isSuccess()) {
                                helper.setA400LevelingBedCalibrationBedTemperature(0);
                                mRouter.routeToHeatedBedLeveling().startForResult(this, 2);
                            }
                        });
                break;
            case 2:
                helper.setA400LevelingBedCalibrationBedTemperature(mBedCalibrationBedTemperature);
                requireActivity().setResult(resultCode);
                requireActivity().finish();
                break;
        }
    }
}
