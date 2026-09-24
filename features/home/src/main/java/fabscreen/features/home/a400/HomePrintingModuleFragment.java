package fabscreen.features.home.a400;

import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_PAUSED;
import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_PRINTING;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.home.R;
import fabscreen.features.home.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class HomePrintingModuleFragment extends BaseFragment {
    @BindView(R2.id.iv_print_model)
    ImageView mIvPrintModel;
    @BindView(R2.id.tv_file_name)
    TextView mTvFileName;
    @BindView(R2.id.tv_progress)
    TextView mTvProgress;
    @BindView(R2.id.tv_remain_time)
    TextView mTvRemainingTime;
    @BindView(R2.id.ib_pause)
    ImageButton mBtnPause;
    @BindView(R2.id.ib_resume)
    ImageButton mBtnResume;
    @BindView(R2.id.ib_stop)
    ImageButton mBtnStop;

    private HomePrintingModuleViewModel mViewModel;
    // printing/pause
    private int mButtonState;
    private IMachine mA400Machine;

    public static HomePrintingModuleFragment newInstance() {
        return new HomePrintingModuleFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(HomePrintingModuleViewModel.class);
        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_home_printing;
    }

    private void initView() {
        HomePrintingModuleViewModel.PrintModelInfo info = mViewModel.getPrintModelInfo();
        mIvPrintModel.setImageBitmap(info.thumbnail);
        mTvFileName.setText(info.fileName);

        mViewModel.getPrintProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    mTvProgress.setText(progress.percentage + "%");
                    mTvRemainingTime.setText(progress.remainDesc);
                }, LogHelper::log);

        mViewModel.getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(state -> {
                    correctButtonState(state);
                }, LogHelper::log);

        mViewModel.getProcessingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(processing -> {
                    // mBtnStart.setEnabled(!processing);
                    mBtnPause.setEnabled(!processing);
                    mBtnResume.setEnabled(!processing);
                    mBtnStop.setEnabled(!processing);
                }, LogHelper::log);

        mViewModel.getPrintEventObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handlePrintError, LogHelper::log);
    }

    private void handlePrintError(HomePrintingModuleViewModel.PrintEvent error) {
        switch (error) {
            case START_FAIL:
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_start_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                            mRouter.backHome().start(requireContext());
                        })
                        .show();
                break;
            case PAUSE_FAIL:
                // Just a confirm
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_pause_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> dialog.dismiss())
                        .show();
                break;

            case RESUME_FAIL:
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_resume_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
                break;

            case POWER_LOSS_RESUME_FAIL:
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_resume_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                            ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                        })
                        .show();
                break;

            case STOP_SUCCESS:

            case FINISH_SUCCESS:
                // Finish Print.
                mRouter.routeToHome().startAndClear(getContext());
                break;

            case STOP_FAIL:
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_stop_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                        })
                        .show();
                break;

            case FINISH_FAIL:
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_finish_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
                break;
        }
    }

    private void correctButtonState(int state) {
        if (mButtonState != state) {
            mButtonState = state;
            updateButtonState(false);
        }
    }

    private void updateButtonState(boolean withAnimation) {
        if (mButtonState == STATE_PRINTING) {
            // Animate to printing
            if (mBtnResume.getVisibility() == View.VISIBLE) {
                animateToPrinting(withAnimation ? 200 : 0);
            } else {
                mBtnPause.setVisibility(View.VISIBLE);
            }

        } else if (mButtonState == STATE_PAUSED) {
            // Animate to paused
            if (mBtnPause.getVisibility() == View.VISIBLE) {
                animateToPaused(withAnimation ? 200 : 0);
            } else {
                mBtnPause.setVisibility(View.INVISIBLE);
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mA400Machine.getMachineController().getEnclosure().subscribeEnclosureInfo();
        }

        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            mA400Machine.getMachineController().getAirPurifier().subscribeAirPurifierStatusChange();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mA400Machine.getMachineController().getEnclosure().unsubscribeEnclosureInfo();
        }

        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            mA400Machine.getMachineController().getAirPurifier().unsubscribeAirPurifierStatusChange();
        }
    }

    @OnClick(R2.id.v_bottom_container)
    void onClickStart() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToPrintPage().start(getContext());
    }


    @OnClick(R2.id.ib_stop)
    void onClickControlStop() {
        playNormalClickSound();
        mViewModel.stopPrint();
    }

    @OnClick(R2.id.ib_pause)
    void onClickControlPause() {
        playNormalClickSound();
        mViewModel.pausePrint();
        mButtonState = STATE_PAUSED;
        updateButtonState(true);
    }

    @OnClick(R2.id.ib_resume)
    void onClickControlResume() {
        playNormalClickSound();
        mViewModel.resumePrint();
        mButtonState = STATE_PRINTING;
        updateButtonState(true);
    }

    private void animateToPaused(int duration) {
        TranslateAnimation animation = new TranslateAnimation(0, -DimensUtils.dp2px(120), 0, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        mBtnPause.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mBtnStop.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBtnPause.setVisibility(View.INVISIBLE);
                mBtnResume.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void animateToPrinting(int duration) {
        TranslateAnimation animation = new TranslateAnimation(0, DimensUtils.dp2px(120), 0, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        mBtnResume.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBtnResume.setVisibility(View.INVISIBLE);
                mBtnPause.setVisibility(View.VISIBLE);
                mBtnStop.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
