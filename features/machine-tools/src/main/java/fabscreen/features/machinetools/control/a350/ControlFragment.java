package fabscreen.features.machinetools.control.a350;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.ControlViewModel;
import fabscreen.features.machinetools.control.a350.modules.bed.HeatedBedControlFragment;
import fabscreen.features.machinetools.control.a350.modules.bed.WorkOriginControlFragment;
import fabscreen.features.machinetools.control.a350.modules.jog.ControlJog3DPFragment;
import fabscreen.features.machinetools.control.a350.modules.jog.ControlJogOtherFragment;
import fabscreen.features.machinetools.control.a350.modules.toolhead._3dp.SingleNozzleControlFragment;
import fabscreen.features.machinetools.control.a350.modules.toolhead.cnc.SpindleSpeedControlFragment;
import fabscreen.features.machinetools.control.a350.modules.toolhead.laser.LaserPowerControlFragment;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.ViewUtils;
import fabscreen.platform.core.ui.view.bottombar.BottomBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Host fragment for control pages.
 * See {@link ControlFragment#initPageResources()} to learn about what pages are loaded for different toolhead use cases.
 */
public class ControlFragment extends BaseFragment {

    @BindView(R2.id.vp_control)
    ViewPager mVpControl;
    @BindView(R2.id.bb_control)
    BottomBar mBbControl;
    private ControlViewModel mViewModel;
    private int mHeadType;

    public static Fragment newInstance() {
        return new ControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        mHeadType = mViewModel.getHeadType();
        initView();

        // Ensuring coordinate is a must have action for control fragment.
        CoordinateSystemPresenter coordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        coordinateSystemPresenter.ensureCoordinate(mHeadType == Module.ModuleType.HEAD_3DP ? 0 : 1);
    }

    private void initView() {
        setTitle(R.string.all_control);
        initPageResources();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_control;
    }

    @Override
    protected ControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(ControlViewModel.class);
    }

    private void initPageResources() {
        // init resources for different control mode
        int[] titles = new int[3];
        int[] icons = new int[3];
        ArrayList<Fragment> fragments = new ArrayList<>();

        titles[0] = R.string.control_jog_mode;
        icons[0] = R.drawable.btn_all_axes_normal_64x64;

        switch (mHeadType) {
            case Module.ModuleType.HEAD_UNPLUGGED:
            case Module.ModuleType.HEAD_3DP:
                titles[1] = R.string.control_nozzle;
                titles[2] = R.string.print_heated_bed;
                icons[1] = R.drawable.btn_all_nozzle_normal_64x64;
                icons[2] = R.drawable.btn_all_heated_bed_normal_64x64;

                fragments.add(ControlJog3DPFragment.newInstance());
                fragments.add(SingleNozzleControlFragment.newInstance());
                fragments.add(HeatedBedControlFragment.newInstance());
                break;
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W:
                titles[1] = R.string.control_set_origin;
                titles[2] = R.string.print_laser_power;
                icons[1] = R.drawable.btn_all_set_origin_normal_64x64;
                icons[2] = R.drawable.btn_all_laser_power_normal_64x64;

                fragments.add(ControlJogOtherFragment.newInstance());
                fragments.add(WorkOriginControlFragment.newInstance());
                fragments.add(LaserPowerControlFragment.newInstance());
                break;
            case Module.ModuleType.HEAD_CNC:
                titles[1] = R.string.control_set_origin;
                titles[2] = R.string.print_spindle_spend;
                icons[1] = R.drawable.btn_all_set_origin_normal_64x64;
                icons[2] = R.drawable.btn_all_work_speed_normal_64x64;

                fragments.add(ControlJogOtherFragment.newInstance());
                fragments.add(WorkOriginControlFragment.newInstance());
                fragments.add(SpindleSpeedControlFragment.newInstance());
                break;
        }

        setupBottomBar(titles, icons);
        setupViewPager(fragments);
    }

    private void setupViewPager(ArrayList<Fragment> fragments) {
        ControlPagerAdapter adapter = new ControlPagerAdapter(getParentFragmentManager(), fragments);
        mVpControl.setAdapter(adapter);
        mVpControl.setOffscreenPageLimit(fragments.size() - 1);
        mVpControl.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mBbControl.selectTab(position);
            }
        });
    }

    private void setupBottomBar(int[] titles, int[] icons) {
        // check if resources valid
        for (int i : titles) {
            if (i == 0) return;
        }

        for (int i : icons) {
            if (i == 0) return;
        }

        // set resources
        mBbControl.addItem(ViewUtils.createBottomBarItem(titles[0], icons[0]));
        mBbControl.addItem(ViewUtils.createBottomBarItem(titles[1], icons[1]));
        mBbControl.addItem(ViewUtils.createBottomBarItem(titles[2], icons[2]));

        mBbControl.setBarBackgroundColor(R.color.bottom_bar_background);
        mBbControl.initialize();

        mBbControl.selectTab(0);
        mBbControl.setOnTabSelectedListener(position -> mVpControl.setCurrentItem(position));
    }

    @Override
    protected void back() {
        mViewModel.leaveControl(mHeadType)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> super.back());
    }
}
