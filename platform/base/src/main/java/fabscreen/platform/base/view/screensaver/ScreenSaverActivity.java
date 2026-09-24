package fabscreen.platform.base.view.screensaver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate;
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.machine.controller.MachineOperationStatus;
import fabscreen.platform.base.view.GradientCircularProgressBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Show a screen saver. Not extend BaseActivity.
 */
public class ScreenSaverActivity extends AppCompatActivity implements OnLocaleChangedListener {
    private final LocalizationActivityDelegate mLocalizationDelegate = new LocalizationActivityDelegate(this);

    @BindView(R2.id.cl_container)
    ConstraintLayout mClContainer;
    @BindView(R2.id.iv_logo)
    ImageView mIvLogo;
    @BindView(R2.id.gcpb_printing)
    GradientCircularProgressBar mGcpbPrinting;
    @BindView(R2.id.tv_progress)
    TextView mTvProgress;
    @BindView(R2.id.tv_time_label)
    TextView mTvTimeLabel;
    @BindView(R2.id.tv_time_display)
    TextView mTvTimeDisplay;
    @BindView(R2.id.cpi_error)
    CircularProgressIndicator mCpiError;
    @BindView(R2.id.group_progress_normal)
    Group mGroupProgressNormal;
    @BindView(R2.id.group_progress_abnormal)
    Group mGroupProgressAbnormal;

    private ScreenSaverViewModel mViewModel;
    private long mFrameSeq;

    private boolean isPrintFinished = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mLocalizationDelegate.addOnLocaleChangedListener(this);
        mLocalizationDelegate.onCreate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_server);
        ButterKnife.bind(this);
        mViewModel = new ViewModelProvider(this).get(ScreenSaverViewModel.class);
        initView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    public Context getApplicationContext() {
        return mLocalizationDelegate.getApplicationContext(super.getApplicationContext());
    }

    @Override
    public Resources getResources() {
        return mLocalizationDelegate.getResources(super.getResources());
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        applyOverrideConfiguration(mLocalizationDelegate.updateConfigurationLocale(newBase));
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalizationDelegate.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ensure screen saver not in the stack.
        if (!isFinishing()) {
            finish();
        }
    }

    private void initView() {
        dimTheWindow();

        mViewModel.getTimerObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(aLong -> {
                    updateProgress();
                    if (aLong % 5 == 0) {
                        // change position every 10 seconds(5 ticks)
                        mFrameSeq++;
                        calculateFrame();
                    }
                });

        mViewModel.getPrintStateObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(this::refreshWorkState, LogHelper::log);
    }

    /**
     * Frame display order: 0,1,2,1; 0,1,2,1; 0,1,2,1; ...
     */
    private void calculateFrame() {
        if (mFrameSeq % 2 != 0) {
            showFrame(1);
        } else if (mFrameSeq / 2 % 2 == 0) {
            showFrame(0);
        } else {
            showFrame(2);
        }
    }

    private void refreshWorkState(Integer state) {
        if (MachineOperationStatus.SYSTEM_STATUS_IDLE.valueEquals(state) && !mViewModel.getPrintFinished()) {
            showIdleSaver();
        } else if (MachineOperationStatus.SYSTEM_STATUS_PAUSED.valueEquals(state)) {
            showPrintPausedSaver();
        } else {
            showPrintSaver();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateProgress() {
        int progress = mViewModel.getPrintProgress();
        mGcpbPrinting.setProgress(progress);
        mCpiError.setProgress(progress);
        mTvProgress.setText(progress + "%");
        mTvTimeLabel.setText(progress == 100 ? R.string.j1_print_time_cost : R.string.j1_print_remaining_time);
        mTvTimeDisplay.setText(formatTime(progress == 100 ? mViewModel.getAllTime() : mViewModel.getRemainTime()));
    }

    // Make UI less bright
    private void dimTheWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.screenBrightness = 0.1f;
        window.setAttributes(attributes);
    }

    private void showIdleSaver() {
        mIvLogo.setVisibility(View.VISIBLE);
        mGroupProgressNormal.setVisibility(View.INVISIBLE);
        mGroupProgressAbnormal.setVisibility(View.INVISIBLE);
    }

    private void showPrintSaver() {
        mIvLogo.setVisibility(View.INVISIBLE);
        mGroupProgressNormal.setVisibility(View.VISIBLE);
        mGroupProgressAbnormal.setVisibility(View.INVISIBLE);
    }

    private void showPrintPausedSaver() {
        mIvLogo.setVisibility(View.INVISIBLE);
        mGroupProgressNormal.setVisibility(View.INVISIBLE);
        mGroupProgressAbnormal.setVisibility(View.VISIBLE);
    }

    /**
     * int[] frameIds = [0,1,2]
     *
     * @param frameId 0 left; 1 mid, 2 right.
     */
    private void showFrame(int frameId) {
        ConstraintSet set = new ConstraintSet();
        set.clone(mClContainer);
        set.clear(R.id.iv_logo, ConstraintSet.START);
        set.clear(R.id.iv_logo, ConstraintSet.END);
        set.clear(R.id.iv_logo, ConstraintSet.TOP);
        set.clear(R.id.iv_logo, ConstraintSet.BOTTOM);
        set.clear(R.id.gcpb_printing, ConstraintSet.START);
        set.clear(R.id.gcpb_printing, ConstraintSet.END);
        set.clear(R.id.cpi_error, ConstraintSet.START);
        set.clear(R.id.cpi_error, ConstraintSet.END);
        int margin = (int) DimensUtils.dp2px(60);
        switch (frameId) {
            case 0:
                // start to start of parent
                set.connect(R.id.iv_logo, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin);
                set.connect(R.id.iv_logo, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, margin);
                set.connect(R.id.gcpb_printing, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin);
                set.connect(R.id.cpi_error, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin);
                break;
            case 1:
                // start to start of parent; end to end of parent
                set.connect(R.id.iv_logo, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin);
                set.connect(R.id.iv_logo, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin);
                set.connect(R.id.iv_logo, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, margin);
                set.connect(R.id.iv_logo, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, margin);
                set.connect(R.id.gcpb_printing, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin);
                set.connect(R.id.gcpb_printing, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin);
                set.connect(R.id.cpi_error, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin);
                set.connect(R.id.cpi_error, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin);
                break;
            case 2:
                // end to end of parent
                set.connect(R.id.iv_logo, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin);
                set.connect(R.id.iv_logo, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, margin);
                set.connect(R.id.gcpb_printing, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin);
                set.connect(R.id.cpi_error, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin);
                break;
        }

        TransitionManager.beginDelayedTransition(mClContainer);
        set.applyTo(mClContainer);
    }

    public static String formatTime(long timeInSecond) {
        int hour = (int) (timeInSecond) / 3600;
        int minute = ((int) (timeInSecond) % 3600) / 60;
        int second = ((int) (timeInSecond) % 60);

        if (hour < 1) {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    @Override
    public void onAfterLocaleChanged() {

    }

    @Override
    public void onBeforeLocaleChanged() {

    }
}
