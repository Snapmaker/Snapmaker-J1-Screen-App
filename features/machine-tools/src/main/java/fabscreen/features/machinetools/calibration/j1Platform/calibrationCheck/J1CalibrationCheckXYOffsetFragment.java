package fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1CalibrationCheckXYOffsetFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.iv_show_gif)
    ImageView mIvShowImage;
    @BindView(R2.id.iv_calibration_high_temperature_warning_normal)
    ImageView mIvCalibrationTemperatureWarningNormal;
    @BindView(R2.id.iv_calibration_glass_plate_normal)
    ImageView mIvCalibrationGlassPlateNormal;
    @BindView(R2.id.tv_calibration_title)
    TextView mTvCalibrationTitle;
    @BindView(R2.id.tv_calibration_progress)
    TextView mTvCalibrationProgress;
    @BindView(R2.id.tv_calibration_content_1)
    TextView mTvCalibrationContent1;
    @BindView(R2.id.btn_next)
    Button mBtnComplete;
    FDMController fdmController;
    boolean mIsGuide = false;

    public static Fragment newInstance() {
        return new J1CalibrationCheckXYOffsetFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mIsGuide = getArguments().getBoolean("is_guide", false);
        }
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        initView();
        restoringMachine();
    }

    public void restoringMachine() {
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(true)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                }, e -> {
                    LogHelper.log(e);
                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                    requireActivity().finish();
                });
    }

    private void initView() {
        mIvCalibrationTemperatureWarningNormal.setVisibility(View.GONE);
        mIvCalibrationGlassPlateNormal.setVisibility(View.GONE);
        Glide.with(this)
                .load(R.drawable.pic_j1_calibration_check_for_xy_offset)
                .into(mIvShowImage);
        mTvCalibrationTitle.setText(R.string.j1_calibration_calibration_check_check_xy_offset_calibration_title);
//        mTvCalibrationProgress.setText("3/6");
        mTvCalibrationContent1.setText(R.string.j1_calibration_calibration_check_check_xy_offset_calibration_content);
        mIvCalibrationTemperatureWarningNormal.setVisibility(View.VISIBLE);
        mBtnComplete.setText(R.string.all_complete);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_currency;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        if (!mIsGuide) {
            ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        } else {
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideCheckPrint(true);
        }
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }


}
