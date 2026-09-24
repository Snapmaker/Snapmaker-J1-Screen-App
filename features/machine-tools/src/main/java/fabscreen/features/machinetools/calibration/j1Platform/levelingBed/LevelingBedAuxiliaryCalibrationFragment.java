package fabscreen.features.machinetools.calibration.j1Platform.levelingBed;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.core.ui.view.HorizontalPrecisionIndicator;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LevelingBedAuxiliaryCalibrationFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.horizontal_indicator)
    HorizontalPrecisionIndicator mHpiLevelingBed;
    @BindView(R2.id.top_bar_back)
    Button mBtBack;
    @BindView(R2.id.btn_next)
    Button mBtNext;
    @BindView(R2.id.tv_leveling_bed_auxiliary_calibration_content)
    TextView mTvShowCount;
    @BindView(R2.id.tv_leveling_bed_auxiliary_calibration_title)
    TextView mTvShowTitle;
    @BindView(R2.id.rectangle_2)
    ImageView mIvHeight;
    private LevelingBedAuxiliaryCalibrationModel mViewModel;


    public static Fragment newInstance() {
        return new LevelingBedAuxiliaryCalibrationFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.init();
    }

    private void initView() {
        Glide.with(this)
                .load(R.drawable.pic_leveling_bed_calibration_auxiliary_left_point_content)
                .into(mIvHeight);
        mViewModel.getOnErrorSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (fabMoving != null && fabMoving.isShowing()) fabMoving.dismiss();
                    errorBack("calibratePointByIndex", responseStructure.resultProp.getValue());
                });

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtNext.setEnabled(false);
                    int count = mViewModel.getCount();
                    Logger.d("---FDT--- getIsMovingObservable :" + count + "\t" + isMoving);
                    if (count == 2) {
                        Glide.with(this)
                                .load(R.drawable.pic_leveling_bed_calibration_auxiliary_left_point_content)
                                .into(mIvHeight);
                        mTvShowCount.setText(R.string.leveling_bed_calibration_auxiliary_left_point_content);
                        mTvShowTitle.setText(R.string.leveling_bed_calibration_left_point_title);
                    } else if (count == 3) {
                        Glide.with(this)
                                .load(R.drawable.pic_leveling_bed_calibration_auxiliary_right_point_content)
                                .into(mIvHeight);
                        mTvShowCount.setText(R.string.leveling_bed_calibration_auxiliary_right_point_content);
                        mTvShowTitle.setText(R.string.leveling_bed_calibration_right_point_title);
                    }
                    mBtBack.setEnabled(!isMoving);
                    if (isMoving) {
                        fabMoving.show();
                    } else {
                        fabMoving.dismiss();
                        if (count == LevelingBedAuxiliaryCalibrationModel.FIRST_CALIBRATION) {
                            mViewModel.moveContent(++count, false);
                        }
                    }
                });

        mViewModel.getProgress()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    if (progress.equals(-999f)) {
                        errorBack("calibratePointByIndex", 200);
                    } else {
                        mHpiLevelingBed.setValue(progress);
                    }
//                    if (progress.equals(-999f)) {
//                        DecisionDialog.create(requireContext())
//                                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
//                                .setContent(R.string.j1_calibration_fail_to_obtain_data)
//                                .setFirstTv(R.string.all_quit, R.color.select_dialog_grey_txt, ((dialog, which) -> {
//                                    ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
//                                            .exitCalibration(false)
//                                            .observeOn(AndroidSchedulers.mainThread())
//                                            .as(bindToLifecycle())
//                                            .subscribe(success -> {
//                                                if (success.isSuccess()) {
//                                                    dialog.dismiss();
//                                                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
//                                                    finishActivityWithResultOk();
//                                                }
//                                            });
//                                }))
//                                .setSecondTv(R.string.all_retry, R.color.select_dialog_orange_txt, (dialog, which) -> {
//                                    mViewModel.setGetZhightState(true);
//                                    dialog.dismiss();
//                                })
//                                .show();
//                    } else {
//
//                    }

                });
        mHpiLevelingBed.setPrecisionIndicatorListener(isValid -> mBtNext.setEnabled(isValid));

    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        int count = mViewModel.getCount();
        mViewModel.setGetZhightState(false);
        if (count == LevelingBedAuxiliaryCalibrationModel.THIRD_CALIBRATION) {
            mViewModel.unSubscribeGetZHeightDifference()
                    .flatMap(success -> mViewModel.setGetHeightDifferenceState(false))
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        if (success.isSuccess()) {
                            ((LevelingBedAuxiliaryCalibrationActivity) requireActivity()).gotoRestoringMachine();
                        }
                    });
        } else {
            mViewModel.moveContent(++count, false);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_auxiliary_calibration_get_height_difference;
    }


    @Override
    protected LevelingBedAuxiliaryCalibrationModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(LevelingBedAuxiliaryCalibrationModel.class);
    }
}
