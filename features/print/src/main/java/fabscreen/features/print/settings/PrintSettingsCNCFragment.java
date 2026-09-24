package fabscreen.features.print.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.FeedRateWidgetPresenter;

public class PrintSettingsCNCFragment extends BaseFragment {
    private static final String TAG = "PrintSettingsCNC";

    @BindView(R2.id.view_print_settings_cnc_page_feed_rate)
    View mViewPageFeedRate;

    private FeedRateWidgetPresenter mFeedRateWidgetPresenter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.print_settings_adjust_settings);

        Log.d(TAG, "enter fragment");

        initFeedRatePage();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print_settings_cnc;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initFeedRatePage() {
        mFeedRateWidgetPresenter = new FeedRateWidgetPresenter(disposables);
        mFeedRateWidgetPresenter.bind(mViewPageFeedRate);
        mFeedRateWidgetPresenter.connectPrintSettings();
    }
}
