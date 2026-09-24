package fabscreen.features.print.print;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.AirPurifierControlWidgetPresenter;
import fabscreen.platform.core.ui.presenter.EnclosureControlWidgetPresenter;
import fabscreen.platform.core.ui.presenter.FeedRateWidgetPresenter;
import fabscreen.platform.core.ui.view.NoScrollViewPager;
import fabscreen.platform.core.ui.view.ViewPagerAdapter;
import fabscreen.platform.core.ui.view.ViewUtils;
import fabscreen.platform.core.ui.view.bottombar.BottomBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintDrawerCNCFragment extends BaseFragment implements PrintActivity.OnDrawerOpenedListener {
    private static final String TAG = "PrintDrawerCNC";

    @BindView(R2.id.bb_print_drawer_bottom_bar)
    BottomBar mBbBottomBar;
    @BindView(R2.id.vp_print_drawer_view_pager)
    NoScrollViewPager mVpViewPager;

    @BindView(R2.id.view_print_settings_cnc_page_feed_rate)
    View mViewPageFeedRate;
    @BindView(R2.id.view_print_settings_add_on_enclosure)
    View mViewPageEnclosure;
    @BindView(R2.id.view_print_settings_add_on_air_purifier)
    View mViewPageAirPurifier;
    @BindView(R2.id.hsv_bottom_bar)
    HorizontalScrollView mViewBottomBar;


    private boolean mIsEnclosurePlugged;
    private boolean mIsAirPurifierPlugged;

    private FeedRateWidgetPresenter mFeedRateWidgetPresenter;
    private EnclosureControlWidgetPresenter mEnclosureControlWidgetPresenter;
    private AirPurifierControlWidgetPresenter mAirPurifierControlWidgetPresenter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsEnclosurePlugged = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable;
        mIsAirPurifierPlugged = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print_drawer_cnc;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        initBottomBar();
        initViewPager();
        initFeedRatePage();

        if (mIsEnclosurePlugged) {
            initEnclosurePage();
        }

        if (mIsAirPurifierPlugged) {
            initAirPurifierPage();
        }
        if (getActivity() != null) {
            ((PrintActivity) getActivity()).setOnDrawerOpenedListener(this);
        }
    }

    private void initBottomBar() {
        mBbBottomBar.addItem(ViewUtils.createBottomBarItem(R.string.print_feed_rate, R.drawable.btn_all_work_speed_normal_64x64));

        // enclosure
        if (mIsEnclosurePlugged) {
            mBbBottomBar.addItem(ViewUtils.createBottomBarItem(R.string.all_enclosure, R.drawable.btn_all_enclosure_control_64x56));
        }

        if (mIsAirPurifierPlugged) {
            mBbBottomBar.addItem(ViewUtils.createBottomBarItem(R.string.all_air_purifier, R.drawable.btn_all_air_purifier_normal_64x64));
        }
        mBbBottomBar.setBarBackgroundColor(R.color.bottom_bar_background);
        mBbBottomBar.initialize();

        mBbBottomBar.selectTab(0);
        mBbBottomBar.setOnTabSelectedListener(position -> mVpViewPager.setCurrentItem(position));
    }

    private void initViewPager() {
        ArrayList<View> views = new ArrayList<>();

        views.add(mViewPageFeedRate);

        if (mIsEnclosurePlugged) {
            views.add(mViewPageEnclosure);
        }
        if (mIsAirPurifierPlugged) {
            views.add(mViewPageAirPurifier);
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(views);
        mVpViewPager.setAdapter(adapter);
        mVpViewPager.setOffscreenPageLimit(views.size());
        mVpViewPager.setCurrentItem(0);
    }

    private void initFeedRatePage() {
        mFeedRateWidgetPresenter = new FeedRateWidgetPresenter(disposables);
        mFeedRateWidgetPresenter.bind(mViewPageFeedRate);
        if (ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getRecoveryFlag()) {
            ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                    .getAdjustSettingFeedRate()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(adjustSettings -> {
                        Logger.d("get feedRate from power loss %.1f", adjustSettings.value);
                        mFeedRateWidgetPresenter.connectPrint(adjustSettings.value);
                    }, LogHelper::log);
        } else {
            mFeedRateWidgetPresenter.connectPrint();
        }
    }

    private void initEnclosurePage() {
        mEnclosureControlWidgetPresenter = new EnclosureControlWidgetPresenter(disposables);
        mEnclosureControlWidgetPresenter.bind(mViewPageEnclosure);
        mEnclosureControlWidgetPresenter.connectStatus();
    }

    private void initAirPurifierPage() {
        mAirPurifierControlWidgetPresenter = new AirPurifierControlWidgetPresenter(disposables);
        mAirPurifierControlWidgetPresenter.bind(mViewPageAirPurifier);
        mAirPurifierControlWidgetPresenter.connectStatus();
    }

    @Override
    public void onDrawerOpened() {
    }

    @Override
    public void onDrawerClosed() {
        mBbBottomBar.selectTab(0);
        mVpViewPager.setCurrentItem(0);
        mViewBottomBar.fullScroll(View.FOCUS_LEFT);
    }

    @Override
    protected void back() {
        PrintActivity activity = (PrintActivity) getContext();
        if (activity != null) {
            activity.closeDrawer();
        }
    }
}
