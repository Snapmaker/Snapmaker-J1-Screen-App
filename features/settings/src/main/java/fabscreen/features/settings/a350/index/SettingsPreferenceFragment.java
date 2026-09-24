package fabscreen.features.settings.a350.index;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class SettingsPreferenceFragment extends BaseFragment {
    @BindView(R2.id.btn_settings_preferences_analysis)
    Button mBtnAnalysis;
    private BehaviorSubject<Boolean> mFirebaseAnalyticsOptionSubject = BehaviorSubject.createDefault(true);

    public static SettingsPreferenceFragment getInstance() {
        return new SettingsPreferenceFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.settings_user_preference);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_preference;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        boolean firebaseAnalyticsFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getFirebaseAnalyticsFlag();
        mFirebaseAnalyticsOptionSubject.onNext(firebaseAnalyticsFlag);

        mFirebaseAnalyticsOptionSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(enabled -> {
                    mBtnAnalysis.setActivated(enabled);
                    getFirebaseAnalytics().setAnalyticsCollectionEnabled(enabled);
                    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled);
                });

    }

    @OnClick(R2.id.btn_settings_preferences_analysis)
    void onClickAnalysisOption() {
        playNormalClickSound();
        boolean firebaseAnalyticsFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getFirebaseAnalyticsFlag();
        firebaseAnalyticsFlag = !firebaseAnalyticsFlag;

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setFirebaseAnalyticsFlag(firebaseAnalyticsFlag);
        mFirebaseAnalyticsOptionSubject.onNext(firebaseAnalyticsFlag);
    }

}
