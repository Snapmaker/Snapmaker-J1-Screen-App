package fabscreen.features.machinetools.control.a350.modules.jog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

/**
 * Common views for Control - Jog Mode
 */
public abstract class ControlJogFragment extends BaseFragment {
    protected ControlJogViewModel mViewModel;
    @BindView(R2.id.vp_jog_mods)
    ViewPager mVpJogModes;
    @BindView(R2.id.tl_jog_indicator)
    TabLayout mTlJogIndicator;
    @BindView(R2.id.tv_widget_coordinate_absolute_x_value)
    TextView mTvAbsoluteX;
    @BindView(R2.id.tv_widget_coordinate_absolute_y_value)
    TextView mTvAbsoluteY;
    @BindView(R2.id.tv_widget_coordinate_absolute_z_value)
    TextView mTvAbsoluteZ;
    @Nullable
    @BindView(R2.id.tv_widget_coordinate_absolute_b_value)
    TextView mTvAbsoluteBValue;

    @Override
    protected abstract int getLayoutResID();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    protected void modifyViewBeforeBind(View rootView) {
        super.modifyViewBeforeBind(rootView);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    protected void initView() {
        setCoordinateValue();
        // setup jog pages(xyz, b), coordinate b visibility
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(LinearJogFragment.newInstance());
        if (mViewModel.isRotaryAvailable()) {
            fragments.add(RotaryJogFragment.newInstance());
            mTlJogIndicator.setVisibility(View.VISIBLE);
            mTlJogIndicator.setupWithViewPager(mVpJogModes);
            mTlJogIndicator.setEnabled(false);
        }
        mVpJogModes.setAdapter(new JogPagerAdapter(getParentFragmentManager(), fragments));
    }

    protected abstract void setCoordinateValue();

    @Override
    protected ControlJogViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(ControlJogViewModel.class);
    }
}
