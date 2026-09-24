package fabscreen.platform.core.ui.presenter;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.core.ui.view.FabFullScreenDialog;
import fabscreen.platform.core.ui.view.RectEnergyBar.RectEnergyBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static fabscreen.platform.base.legacy.connection.print.DeprecatedPrintController.STATE_PAUSED;
import static fabscreen.platform.base.legacy.connection.print.DeprecatedPrintController.STATE_PRINTING;

public class AirPurifierControlWidgetPresenter extends BasePresenter {

    @BindView(R2.id.btn_air_purifier_power)
    ActionButton mBtnFan;
    @BindView(R2.id.sbg_air_purifier_fan_speed_level)
    SegmentedButtonGroup mSbgFanSpeed;
    @BindView(R2.id.tv_air_purifier_error_power_off)
    TextView mTvTipPowerOff;
    @BindView(R2.id.tv_air_purifier_warning_print_affect_notice)
    TextView mTvWarningNotice;
    @BindView(R2.id.tv_air_purifier_tip_filter_life_low)
    TextView mTvTipFilterLife;
    @BindView(R2.id.reb_air_purifier_lifetime)
    RectEnergyBar mRebLifeTime;

    private int mFanSpeed = 1;
    private boolean mIsAirPurifierOn = false;
    private boolean mIsAirPurifierPowerOff = true;
    private FabFullScreenDialog mHatchDialog;
    private FabFullScreenDialog mFilterDialog;

    private BehaviorSubject<Boolean> mAirPurifierHatchSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mAirPurifierFilterSubject = BehaviorSubject.createDefault(false);

    public AirPurifierControlWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    public void bind(View view) {
        ButterKnife.bind(this, view);
    }

    public void connectStatus() {
        // init view
        mSbgFanSpeed.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    mFanSpeed = 1;
                    break;
                case 1:
                    mFanSpeed = 2;
                    break;
                case 2:
                    mFanSpeed = 3;
                    break;
            }
            setAirPurifierFanSpeed();
        });

        mRebLifeTime.setMaxLevel(3);
        mRebLifeTime.initialize();
        mRebLifeTime.setPosition(2);


        // get air purifier status and fan status
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().getAirPurifierStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(airPurifierStatus -> {
                    mIsAirPurifierPowerOff = airPurifierStatus.getModuleStatus() == SSTPPacketContent.AirPurifierStatus.AIR_PURIFIER_STATUS_POWER_OFF;
                    mBtnFan.setEnabled(!mIsAirPurifierPowerOff);
                    mFanSpeed = airPurifierStatus.getFanSpeedLevel();
                    mIsAirPurifierOn = airPurifierStatus.isFanOn();
                    mBtnFan.setActivated(airPurifierStatus.isFanOn());
                    mSbgFanSpeed.setPosition(airPurifierStatus.getFanSpeedLevel() - 1, false);
                    mRebLifeTime.setPosition(airPurifierStatus.getFilterLife());
                    mTvTipFilterLife.setVisibility(airPurifierStatus.getFilterLife() == 0 ? TextView.VISIBLE : TextView.GONE);
                    handleErrorBit((byte) airPurifierStatus.getModuleStatus());
                    updateTipsView();
                }, LogHelper::log);
        addDisposable(sub);

        // Once the Print State changes, call update tips view during print.
        sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(printState -> updateTipsView());
        addDisposable(sub);

        sub = mAirPurifierHatchSubject
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event) {
                        if (mHatchDialog != null && mHatchDialog.isShowing()) return;

                        showNotProperlyClosedDialog();
                    }
                });
        addDisposable(sub);

        sub = mAirPurifierFilterSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event) {
                        if (mFilterDialog != null && mFilterDialog.isShowing()) return;

                        showFilterDrawOutDialog();
                    } else {
                        if (mFilterDialog != null && mFilterDialog.isShowing()) {
                            mFilterDialog.dismiss();
                            mFilterDialog = null;
                        }
                    }
                });
        addDisposable(sub);
    }

    private void setAirPurifierFanSpeed() {
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().setFanSpeedLevel(0, mFanSpeed)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {/**/}, LogHelper::log);
        addDisposable(sub);
    }

    private void handleErrorBit(byte errorBit) {
        boolean isFilterDrawOut = (errorBit & SSTPPacketContent.AirPurifierStatus.BIT_FILTER_DRAW_OUT) != 0;
        boolean isHatchOpened = (errorBit & SSTPPacketContent.AirPurifierStatus.BIT_HATCH_OPENED_WHEN_WORKING) != 0;

        // comment this code because we don't need to notify hatch open.
//        mAirPurifierHatchSubject.onNext(isHatchOpened);
        mAirPurifierFilterSubject.onNext(isFilterDrawOut);
    }

    private void showFilterDrawOutDialog() {
        mFilterDialog = FabFullScreenDialog.create(getContext())
                .setIcon(R.drawable.pic_air_purifier_filter_not_detected_240x160)
                .setTitle(R.string.warning_air_purifier_cartridge_not_properly_seated_title)
                .setMessage(R.string.warning_air_purifier_cartridge_not_properly_seated_desc)
                .setPositive(R.string.all_confirm, (dialog, which) -> {
                    Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setAirPurifierEnabled(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(ret -> {
                                mFilterDialog.dismiss();
                                mFilterDialog = null;
                            });
                    addDisposable(sub);
                });
        mFilterDialog.show();
    }

    private void showNotProperlyClosedDialog() {
        mHatchDialog = FabFullScreenDialog.create(getContext())
                .setIcon(R.drawable.pic_air_purifier_lid_opened_240x320)
                .setTitle(R.string.warning_air_purifier_not_properly_closed)
                .setMessage(R.string.warning_air_purifier_not_properly_closed_desc)
                .setPositive(R.string.all_confirm, (dialog, which) -> {
                    Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setAirPurifierEnabled(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(ret -> {
                                mHatchDialog.dismiss();
                                mHatchDialog = null;
                            });
                    addDisposable(sub);
                });
        mHatchDialog.show();
    }

    // Update air purifier tips. Only one tip will show up at the same time.
    private void updateTipsView() {
        if (mIsAirPurifierPowerOff) {
            // If air purifier is power off, then show up power off warning and hide notice tip.
            mTvWarningNotice.setVisibility(TextView.GONE);
            mTvTipPowerOff.setVisibility(TextView.VISIBLE);
        } else {
            mTvTipPowerOff.setVisibility(TextView.GONE);

            final int printState = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState();
            final boolean hasPrintJob = (printState == STATE_PRINTING) || (printState == STATE_PAUSED);
            // If switching on the air purifier during 3DP or CNC printing, then show up notice tip.
            if (mIsAirPurifierOn && hasPrintJob) {
                IMachine.WorkType headType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
                switch (headType) {
                    case FDM: {
                        mTvWarningNotice.setVisibility(TextView.VISIBLE);
                        mTvWarningNotice.setText(R.string.warning_air_purifier_turn_on_during_print_3dp);
                        break;
                    }
                    case CNC: {
                        mTvWarningNotice.setVisibility(TextView.VISIBLE);
                        mTvWarningNotice.setText(R.string.warning_air_purifier_turn_on_during_print_cnc);
                        break;
                    }
                    case LASER:
                    case NONE:
                    default: {
                        mTvWarningNotice.setVisibility(TextView.GONE);
                        break;
                    }
                }
            } else {
                mTvWarningNotice.setVisibility(TextView.GONE);
            }
        }
    }

    @OnClick(R2.id.btn_air_purifier_power)
    void onClickFan() {
        mBtnFan.setEnabled(false);
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setAirPurifierEnabled(!mIsAirPurifierOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mBtnFan.setEnabled(true);
                }, e -> {
                    mBtnFan.setEnabled(true);
                });
        addDisposable(sub);
    }

}
