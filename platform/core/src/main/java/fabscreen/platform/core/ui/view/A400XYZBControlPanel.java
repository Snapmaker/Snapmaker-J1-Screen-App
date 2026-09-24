package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.common.a400jogpanel.JogPanelViewPagerAdapter;
import fabscreen.platform.core.ui.data.MoveController;

public class A400XYZBControlPanel extends ConstraintLayout {

    @BindView(R2.id.vp_calibration_control)
    ViewPager2 mVpControl;
    @BindView(R2.id.iv_calibration_control_first)
    ImageView mIvFirstSign;
    @BindView(R2.id.iv_calibration_control_second)
    ImageView mIvSecondSign;
    @BindView(R2.id.sbg_xyz_control_steps)
    SegmentedButtonGroup mSbgXYZControlSteps;
    @BindView(R2.id.sbg_b_control_steps)
    SegmentedButtonGroup mSbgBControlSteps;

    private OnDirectionClickListener mDirectionListener;
    private Context mContext;
    private JogPanelViewPagerAdapter mViewPageAdapter;
    private float[] mWidths = {0.1f, 1f, 10f, 100f};
    private float mStepWidth = mWidths[1];
    private int mViewPageCuter = 0;

    public A400XYZBControlPanel(@NonNull Context context) {
        super(context);
        init(context);
    }

    public A400XYZBControlPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_calibration_control_xyzb, this);
        mContext = context;
        ButterKnife.bind(this, view);

        mSbgXYZControlSteps.setPosition(1, false);
        mSbgXYZControlSteps.setOnPositionChangedListener(position -> {
            if (mWidths.length < 4) return;
            mStepWidth = mWidths[position];
            Log.e("123", "==XYZ==Position" + position);
            if (mDirectionListener != null) {
                mDirectionListener.onPositionChange(position);
            }
        });

        mSbgBControlSteps.setOnPositionChangedListener(position -> {
            if (mWidths.length < 4) return;
            mStepWidth = mWidths[position];
            Log.e("123", "==B==Position" + position);
            if (mDirectionListener != null) {
                mDirectionListener.onPositionChange(position);
            }
        });

        List<Integer> vpTypeList = new ArrayList<>();
        vpTypeList.add(JogPanelViewPagerAdapter.XYZ_TYPE);
        vpTypeList.add(JogPanelViewPagerAdapter.B_TYPE);
        mViewPageAdapter = new JogPanelViewPagerAdapter(vpTypeList);
        mVpControl.setAdapter(mViewPageAdapter);
        mVpControl.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (mViewPageCuter != position) {
                    mViewPageCuter = position;
                    changeSign(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        mViewPageAdapter.setOnItemClickListener(new JogPanelViewPagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MoveController.Direction direction) {
                if (mDirectionListener != null) {
                    mDirectionListener.onDirectionClicked(direction, mStepWidth);
                }
            }
        });
    }

    public void setStepWidths(float width0, float width1, float width2, float width3) {
        mWidths[0] = width0;
        mWidths[1] = width1;
        mWidths[2] = width2;
        mWidths[3] = width3;
    }

    private void changeSign(int position) {
        mIvFirstSign.setBackgroundResource(position == 0 ? R.drawable.ic_view_pager_normal : R.drawable.ic_view_pager_select);
        mIvSecondSign.setBackgroundResource(position == 1 ? R.drawable.ic_view_pager_normal : R.drawable.ic_view_pager_select);
        mSbgXYZControlSteps.setVisibility(position == 0 ? VISIBLE : GONE);
        mSbgBControlSteps.setVisibility(position == 1 ? VISIBLE : GONE);
        if (position == 0) {
            setStepWidths(0.1f, 1f, 10f, 100f);
            mSbgXYZControlSteps.setPosition(1, true);
        } else {
            setStepWidths(0.2f, 1f, 5f, 90f);
            mSbgBControlSteps.setPosition(1, true);
        }
        mDirectionListener.onPositionChange(1);
    }

    public void setRotaryStuffVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        mIvFirstSign.setVisibility(visibility);
        mIvSecondSign.setVisibility(visibility);
        mVpControl.setUserInputEnabled(visible);
    }


    public void setOnDirectionClickListener(OnDirectionClickListener listener) {
        mDirectionListener = listener;
    }

    public interface OnDirectionClickListener {
        void onDirectionClicked(MoveController.Direction direction, float stepWidth);

        void onPositionChange(int position);
    }

}
