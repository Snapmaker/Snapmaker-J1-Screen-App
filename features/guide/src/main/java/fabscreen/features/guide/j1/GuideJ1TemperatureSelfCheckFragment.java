package fabscreen.features.guide.j1;

import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_LEFT;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class GuideJ1TemperatureSelfCheckFragment extends BaseFragment {
    @BindView(R2.id.guide_j1_nozzle_check_title)
    TextView mTvNozzleCheck;
    @BindView(R2.id.guide_j1_heated_bed_check_title)
    TextView mTvHeatedBedCheck;

    @BindView(R2.id.st_nozzle_state_left)
    TextView mShowStateLeft;
    @BindView(R2.id.st_nozzle_state_right)
    TextView mShowStateRight;
    @BindView(R2.id.st_heated_bed)
    TextView mShowStateHeatedBed;
    @BindView(R2.id.progressBar)
    ProgressBar mProgress;
    @BindView(R2.id.progressBar_2)
    ProgressBar mProgress2;
    private final BehaviorSubject<Boolean> mLeftTemperatureState = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Boolean> mRightTemperatureState = BehaviorSubject.createDefault(false);
    @BindView(R2.id.btn_next)
    Button mBtNext;
    FDMController fdmController;
    private final BehaviorSubject<Boolean> mBedTemperatureState = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Boolean> mNozzleReady = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Boolean> mBedReady = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Integer> mTemperatureCheckCountSubject = BehaviorSubject.createDefault(0);
    private final BehaviorSubject<Integer> mNozzleReadyCountSubject = BehaviorSubject.createDefault(0);
    private final BehaviorSubject<Integer> mHeatedBedReadyCountSubject = BehaviorSubject.createDefault(0);
    private int mPreCheckTemp = 0;
    @BindView(R2.id.iv_temperature)
    ImageView mIvTemperature;
    @BindView(R2.id.iv_temperature_2)
    ImageView mIvTemperature2;

    public static Fragment newInstance() {
        return new GuideJ1TemperatureSelfCheckFragment();
    }

    private static Boolean getTemperatureState(Boolean leftTemperatureState, Boolean rightTemperatureState) {
        return leftTemperatureState && rightTemperatureState;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        initView();
    }

    private void initView() {
        mBtNext.setEnabled(false);
        fdmController
                .getToolheadStatusSubjectHolder(0)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolHeadInfo -> {
                            double leftTemperature = 0;
                            double leftTargetTemperature = 0;
                            for (Extruder e : fdmToolHeadInfo.getExtruderList()) {
                                switch (e.getId()) {
                                    case EXTRUDER_LEFT:
                                        leftTemperature = ((int) (e.getTemperature() * 1000)) / 1000.f;
                                        leftTargetTemperature = e.getTargetTemperature();
                                        break;
                                    default:
                                        break;
                                }
                            }
                            mShowStateLeft.setText(String.format("%.3f", leftTemperature) + "/ " + leftTargetTemperature + " °C");
                            if (leftTargetTemperature != 0) {
                                mLeftTemperatureState.onNext((leftTemperature >= leftTargetTemperature - 20) && (leftTemperature <= leftTargetTemperature + 20));
                            }
                        }, LogHelper::log
                );

        fdmController
                .getToolheadStatusSubjectHolder(1)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolHeadInfo -> {
                            double leftTemperature = 0;
                            double leftTargetTemperature = 0;
                            for (Extruder e : fdmToolHeadInfo.getExtruderList()) {
                                switch (e.getId()) {
                                    case EXTRUDER_LEFT:
                                        leftTemperature = ((int) (e.getTemperature() * 1000)) / 1000.f;
                                        leftTargetTemperature = e.getTargetTemperature();
                                        break;
                                    default:
                                        break;
                                }
                            }
                            mShowStateRight.setText(String.format("%.3f", leftTemperature) + "/ " + leftTargetTemperature + " °C");
                            if (leftTargetTemperature != 0) {
                                mRightTemperatureState.onNext((leftTemperature >= leftTargetTemperature - 20) && (leftTemperature <= leftTargetTemperature + 20));
                            }
                        }, LogHelper::log
                );

        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController()
                .getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(heatedBedStatus -> {
                    HeatedBed.ZoneInfo info = heatedBedStatus.getZoneList().get(0);
                    float currentHeatedBedTemp = info.getCurrentTemperature();
                    int targetHeatedBedTemp = info.getTargetTemperature();
                    mShowStateHeatedBed.setText(String.format(Locale.ENGLISH, "%.1f", currentHeatedBedTemp) + "/ " + targetHeatedBedTemp + " °C");
                    if (targetHeatedBedTemp != 0) {
                        mBedTemperatureState.onNext((currentHeatedBedTemp >= targetHeatedBedTemp - 10) && (currentHeatedBedTemp <= targetHeatedBedTemp + 10));
                    }
                }, LogHelper::log);

        Observable.zip(mLeftTemperatureState, mRightTemperatureState, GuideJ1TemperatureSelfCheckFragment::getTemperatureState)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    boolean isEnoughCount = mNozzleReadyCountSubject.getValue() >= 30;
                    if (success) {
                        mNozzleReadyCountSubject.onNext(mNozzleReadyCountSubject.getValue() + 1);
                    } else {
                        mNozzleReadyCountSubject.onNext(0);
                    }
                    mIvTemperature.setVisibility(isEnoughCount ? View.VISIBLE : View.GONE);
                    mProgress.setVisibility(!isEnoughCount ? View.VISIBLE : View.GONE);
                    Logger.e("Nozzle count " + mNozzleReadyCountSubject.getValue());
                    mNozzleReady.onNext(isEnoughCount && success);
                    mTvNozzleCheck.setText(isEnoughCount && success ? "Nozzle Checked" : "Checking Nozzle…");
                });

        mBedTemperatureState.debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    boolean isEnoughCount = mHeatedBedReadyCountSubject.getValue() >= 30;
                    if (success) {
                        mHeatedBedReadyCountSubject.onNext(mHeatedBedReadyCountSubject.getValue() + 1);
                    } else {
                        mHeatedBedReadyCountSubject.onNext(0);
                    }
                    mProgress2.setVisibility(!isEnoughCount ? View.VISIBLE : View.GONE);
                    mIvTemperature2.setVisibility(isEnoughCount ? View.VISIBLE : View.GONE);
                    Logger.e("Heated Bed count " + mHeatedBedReadyCountSubject.getValue());
                    mBedReady.onNext(isEnoughCount && success);
                    mTvHeatedBedCheck.setText(isEnoughCount && success ? "Heated Bed Checked" : "Checking heated bed…");
                });

        mTemperatureCheckCountSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(count -> {
                    if (count == 0) {
                        // start up
                        mPreCheckTemp = 300;
                        startHeatedUpNozzle(mPreCheckTemp);
                        startHeatedUpHeatedBed();
                    } else if (count == 1) {
                        mPreCheckTemp = 220;
                        startHeatedUpNozzle(mPreCheckTemp);
                    } else {

                    }
                });

        checkReady();
    }

    void startHeatedUpNozzle(int temp) {
        fdmController.setExtruderTemperature(0, 0, temp)
                .flatMap(success -> fdmController.setExtruderTemperature(1, 0, temp))
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    int result = responseStructure.resultProp.getValue();
                    if (result != 0) {
                        // TODO
                    }
                }, LogHelper::log);
    }

    void startHeatedUpHeatedBed() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController()
                .getHeatedBed()
                .setZoneTargetTemperature(0, 100)
                .as(bindToLifecycle())
                .subscribe(result -> {

                }, LogHelper::log);
    }

    void checkReady() {
        Observable.zip(mNozzleReady, mBedReady, (r1, r2) -> r1 & r2)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ready -> {
                    Logger.d("ready " + ready);
                    if (mTemperatureCheckCountSubject.getValue() > 1) {
                        // double check confirm
                        mBtNext.setEnabled(ready);
                    } else {
                        if (ready) {
                            if (mPreCheckTemp == 300) {
                                mTemperatureCheckCountSubject.onNext(1);
                            } else if (mPreCheckTemp == 220) {
                                mTemperatureCheckCountSubject.onNext(2);
                            } else {
                                // doing nothing
                            }
                        }
                    }
                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_temperature_self_check;
    }

    @Override
    protected void back() {

        DecisionDialog.create(getContext())
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                .setContent("The current data will not be saved. Are you sure to quit?")
                .setContentColor(R.color.palette_grey_french)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_left_text_color, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getString(R.string.all_yes), R.color.palette_red_sunset, ((dialog, which) -> {
                    coolDownToolHead();
                    coolDownHeatedBed();
                    dialog.dismiss();
                    requireActivity().finish();
                }))
                .show();
    }

    void coolDownToolHead() {
        fdmController.setExtruderTemperature(0, 0, 0)
                .flatMap(success -> fdmController.setExtruderTemperature(1, 0, 0))
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    int result = responseStructure.resultProp.getValue();
                    if (result != 0) {
                        // TODO
                    }
                }, LogHelper::log);
    }

    void coolDownHeatedBed() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController()
                .getHeatedBed()
                .setZoneTargetTemperature(0, 0)
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideTemperatureSelfCheck(true);
        ((J1GuideActivity) requireActivity()).checkNext();
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController().subscribeExtruderChange();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().subscribeTemperatureChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController().unSubscribeExtruderChange();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().unsubscribeTemperatureChange();
    }
}
