package fabscreen.features.settings.a350.factory;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.core.ui.view.RulerView;
import io.reactivex.subjects.BehaviorSubject;

public class FactoryBrightnessTestFragment extends BaseFragment {
    private static final String TAG = "FactoryBrightnessTestFragment";

    @BindView(R2.id.tv_factory_brightness_value)
    TextView mTvBrightnessValue;

    @BindView(R2.id.rv_factory_brightness_ruler)
    RulerView mRvBrightness;

    private ContentResolver mContentResolver;
    private Window mWindow;
    private WindowManager.LayoutParams mLayoutPars;

    private BehaviorSubject<Integer> mBrightnessSubject = BehaviorSubject.createDefault(0);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("Brightness Test");

        if (getActivity() == null) return;

        mContentResolver = getActivity().getContentResolver();
        // Window object, that will store a reference to the current window
        mWindow = getActivity().getWindow();
        mLayoutPars = mWindow.getAttributes();

        try {
            Settings.System.putInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            // Get the current system brightness
            int brightness = (Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS));
            mRvBrightness.setCurrentValue(brightness * 100 / 255);
            mBrightnessSubject.onNext(brightness * 100 / 255);
        } catch (Settings.SettingNotFoundException e) {
            // Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness");
            FabAlert.alert(getContext(), "Could not access system brightness!");
            LogHelper.log(e);
        }

        // on value changed
        mRvBrightness.setOnValueChangedListener(value -> {
            if (value != mBrightnessSubject.getValue()) {
                mBrightnessSubject.onNext((int) value);
            }
        });

        // set brightness value
        mBrightnessSubject
                .as(bindToLifecycle())
                .subscribe(value -> {
                    mTvBrightnessValue.setText(String.format(Locale.getDefault(), "亮度：%d", value));
                    Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, value * 255 / 100);
                    mLayoutPars.screenBrightness = value / (float) 255;
                    mWindow.setAttributes(mLayoutPars);
                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_factory_brightness_test;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }
}
