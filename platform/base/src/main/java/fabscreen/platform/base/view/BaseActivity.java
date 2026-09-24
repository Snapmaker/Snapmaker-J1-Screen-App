package fabscreen.platform.base.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.akexorcist.localizationactivity.core.LanguageSetting;
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate;
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.Locale;

import fabscreen.platform.base.BuildConfig;
import fabscreen.platform.base.R;
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.BaseAppService;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.view.debugtool.FloatWindow;
import fabscreen.platform.base.view.screensaver.ScreenSaverActivity;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends FragmentActivity implements OnLocaleChangedListener {
    private final LocalizationActivityDelegate mLocalizationDelegate = new LocalizationActivityDelegate(this);

    protected BaseAppService mApp;
    protected IMachine mMachine;
    protected IRouter mRouter;
    protected FloatWindow mFloatWindow;

    protected final CompositeDisposable disposables = new CompositeDisposable();
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean mIsJ1;

    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = () -> startActivity(new Intent(BaseActivity.this, ScreenSaverActivity.class));
    private IPreferences.Helper mPrefHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mLocalizationDelegate.addOnLocaleChangedListener(this);
        mLocalizationDelegate.onCreate();
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

        mApp = (BaseAppService) ServiceContainer.getInstance().getService(IAppService.class);
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mRouter = ServiceContainer.getInstance().getService(IRouter.class);
        mPrefHelper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Logger.d("Route: Create " + getClass().getSimpleName());
        mIsJ1 = mMachine.getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J;
        mFloatWindow = new FloatWindow();
        getLifecycle().addObserver(mFloatWindow);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApp.setCurrentActivity(this);
        mApp.enterContext(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalizationDelegate.onResume(this);
        if (mPrefHelper.getScreenSaverEnabled()) {
            mHandler.postDelayed(mRunnable, mPrefHelper.getScreenSaverDelayTime() * 1000L);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    // enter screen saver after no user interaction
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mPrefHelper.getScreenSaverEnabled()) {
            return super.dispatchTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeCallbacks(mRunnable);
                break;
            case MotionEvent.ACTION_UP:
                mHandler.postDelayed(mRunnable, mPrefHelper.getScreenSaverDelayTime() * 1000L);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * onDestroy
     * <p>
     * Release disposables.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        mApp.leaveContext(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (BuildConfig.DEBUG) {
            // Hide nav bars on AVD.
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    protected <T> AutoDisposeConverter<T> bindToLifecycle() {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Method for communications between fragments.
     *
     * @param resultCode int
     * @param data       Intent that contains result
     */
    public void onFragmentResult(int resultCode, @Nullable Intent data) {
    }

    public void addFragment(int containerId, @NonNull Fragment fragment) {
        this.addFragment(null, containerId, fragment, true);
    }

    public void addFragment(int containerId, @NonNull Fragment fragment, boolean isHide) {
        this.addFragment(null, containerId, fragment, isHide);
    }

    /**
     * Add fragment to container.
     */
    public void addFragment(String stackName, int containerId, @NonNull Fragment fragment, boolean isHide) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment);
        final int fragmentCount = fragmentManager.getFragments().size();
        if (fragmentCount > 0) {
            // hide the fragment on top now
            if (isHide) {
                Fragment topFragment = fragmentManager.getFragments().get(fragmentCount - 1);
                transaction.hide(topFragment);
            }

            transaction.addToBackStack(stackName);
        }
        transaction.commit();
    }

    /**
     * Replace fragment on top of container.
     */
    public void replaceFragment(int containerId, @NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerId, fragment);
        transaction.commit();
    }

    /**
     * Pop fragment on top of stack.
     */
    public void popFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @NonNull
    protected FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    protected <T extends BaseViewModel> T getViewModel(Class<T> modelClass) {
        return new ViewModelProvider(this).get(modelClass);
    }

    public void playNormalClickSound() {
        if (!mIsJ1) {
            SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
        }
    }

    public void playSwitchSound() {
        if (!mIsJ1) {
            SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_switch));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        applyOverrideConfiguration(mLocalizationDelegate.updateConfigurationLocale(newBase));
        super.attachBaseContext(newBase);
    }

    @Override
    public Context getApplicationContext() {
        return mLocalizationDelegate.getApplicationContext(super.getApplicationContext());
    }

    @Override
    public Resources getResources() {
        return mLocalizationDelegate.getResources(super.getResources());
    }

    public void setLanguage(Locale locale) {
        Locale old = LanguageSetting.getLanguage(this);
        Locale newLocale = locale;
        Logger.d("setting language...");
        Logger.d("old locale is %1$s, new locale is %2$s", old, newLocale);
        mLocalizationDelegate.setLanguage(this, locale);
    }

    public Locale getCurrentLanguage() {
        return mLocalizationDelegate.getLanguage(this);
    }

    @Override
    public void onBeforeLocaleChanged() {

    }

    @Override
    public void onAfterLocaleChanged() {

    }

    public void sendEnclosureMessage(PerpetualPopuBean bean) {
        if (!mIsJ1 && mMachine.getMachineStatusSubjectHolder().getValue().status != PrintController.STATE_IDLE) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFloatWindow.showToast(bean);
                }
            });
        }
    }

    public void sendAirPurifierMessage(PerpetualPopuBean bean) {
        if (!mIsJ1) {
            new SuperToastHelper.Builder()
                    .setDrawable(bean.getImgRes())
                    .setTitle(getString(bean.getTitle()))
                    .setMessage(getString(bean.getContent()))
                    .setPermanentDisplay(true)
                    .build()
                    .showToast(this);
        }
    }
}
