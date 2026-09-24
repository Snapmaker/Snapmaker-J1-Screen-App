package fabscreen.features.settings.a350.experiment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ExperimentCrashlyticsTestFragment extends BaseFragment {
    @BindView(R2.id.btn_experiment_crashlytic_test)
    Button mBtnCrashTest;
    int mCrashDelayTime;
    Thread mThread;

    Disposable mSubscribe;

    @BindView(R2.id.tv_experiment_crashlytics_show)
    TextView mTvCrashTest;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_experiment);

        mBtnCrashTest.setText("Crash!");
    }

    @OnTextChanged(R2.id.ed_experiment_crashlytics)
    void onProtectTemperatureChange(CharSequence protectTemperature) {
        try {
            mCrashDelayTime = Integer.parseInt(protectTemperature.toString());
        } catch (Exception e) {
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment_crashlytics_test;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_experiment_crashlytic_test)
    void onClickCrash() {
        playNormalClickSound();
        throw new RuntimeException("Crashlytics Test");
    }

    @OnClick(R2.id.btn_experiment_crashlytic_test_delay)
    void onClickDelayCrash() {
        playNormalClickSound();
        mTvCrashTest.setText("设置延时Crash时间：" + mCrashDelayTime + " s");
        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
            mSubscribe = null;
        }
        // When a crash occurs, the process terminates and all threads are destroyed automatically, without control reclaim.
        mSubscribe = Observable.timer(mCrashDelayTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scuess -> {
                    throw new RuntimeException("Crashlytics Test");
                });

    }
}
