package fabscreen.features.machinetools.calibration.j1Platform.LevelingXY;

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
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.HorizontalPrecisionIndicator;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LevelingXYAuxiliaryCalibrationFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.horizontal_indicator)
    HorizontalPrecisionIndicator mHpiXYCalibration;
    @BindView(R2.id.top_bar_back)
    Button mBtBack;
    @BindView(R2.id.btn_next)
    Button mBtNext;
    @BindView(R2.id.tv_leveling_bed_auxiliary_calibration_content)
    TextView mTvShowCount;
    @BindView(R2.id.tv_leveling_bed_auxiliary_calibration_title)
    TextView mTvShowTitle;
    FDMController fdmController;
    @BindView(R2.id.rectangle_2)
    ImageView mIvShowImage;

    public static Fragment newInstance() {
        return new LevelingXYAuxiliaryCalibrationFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        initView();
        initOperation();
    }

    private void initView() {
        Glide.with(this)
                .load(R.drawable.gif_leveling_xy_calibration_content)
                .into(mIvShowImage);
        mHpiXYCalibration.setVisibility(View.INVISIBLE);
        mHpiXYCalibration.setPrecisionIndicatorListener(isValid -> {/*do nothing*/});
        mTvShowTitle.setText(R.string.leveling_XY_calibration_title);
        mTvShowCount.setText(R.string.leveling_XY_calibration_running_content);
        mBtNext.setEnabled(false);
        mBtBack.setEnabled(false);
    }


    private void initOperation() {
        fdmController.setCalibrationMode(100)
                .flatMap(success -> success.isSuccess() ? fdmController.startXYCalibration() : Observable.just(success))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        mBtNext.setEnabled(true);
                        mBtBack.setEnabled(true);
                        mTvShowCount.setText(R.string.leveling_XY_calibration_success_content);
                    } else if (responseStructure.resultProp.getValue().equals(201)) {
                        mBtNext.setEnabled(true);
                        mBtBack.setEnabled(true);
                        DecisionDialog.create(getContext())
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                                .setTitle(R.string.all_wizard_data_missing)
                                .setContent(R.string.all_wizard_data_missing_content)
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setFirstTv(R.string.all_wizard_data_missing_check, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                                    ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                                            .exitCalibration(false)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .as(bindToLifecycle())
                                            .subscribe(success -> {
                                                dialog.dismiss();
                                                if (success.isSuccess()) {
                                                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                                                    ServiceContainer.getInstance().getService(IPreferences.class).getHelper().reset();
                                                    mRouter.backHome().start(requireContext());
                                                }
                                            });
                                })).show();
                    } else {
                        mBtNext.setEnabled(true);
                        mBtBack.setEnabled(true);
                        errorBack("setCalibrationMode", responseStructure.resultProp.getValue());
                    }
                }, LogHelper::log);
    }


    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((LevelingXYAuxiliaryCalibrationActivity) getActivity()).gotoRestoringMachine();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_auxiliary_calibration_get_height_difference;
    }


}
