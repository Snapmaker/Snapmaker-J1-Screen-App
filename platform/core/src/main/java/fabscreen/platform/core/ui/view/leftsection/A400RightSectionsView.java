package fabscreen.platform.core.ui.view.leftsection;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import fabscreen.platform.core.R;

public class A400RightSectionsView extends RelativeLayout implements ILeftSectionView {

    private RelativeLayout mRlBg;
    private View mVSelectType;
    private ImageView mIvLogo;
    private TextView mTvTitle;
    private Context mContext;

    public A400RightSectionsView(Context context) {
        this(context, null);
        mContext = context;
    }

    public A400RightSectionsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_a400_right_setions, this, true);
        mRlBg = view.findViewById(R.id.rl_calibration_mode);
        mVSelectType = view.findViewById(R.id.view_calibration_mode);
        mIvLogo = view.findViewById(R.id.view_calibration_mode_logo);
        mTvTitle = view.findViewById(R.id.tv_calibration_mode_title);
    }

    @Override
    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    @Override
    public void setTitle(int titleResId) {
        mTvTitle.setText(titleResId);
    }

    @Override
    public void setIcon(@DrawableRes int drawableResId) {
        if (drawableResId == 0) return;
        mIvLogo.setImageResource(drawableResId);
    }

    @Override
    public void setShowBadge(boolean show) {

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mVSelectType.setVisibility(selected ? VISIBLE : GONE);
        mIvLogo.setSelected(selected);
        mRlBg.setBackgroundColor(ContextCompat.getColor(mContext,
                selected ? R.color.palette_grey_nero : R.color.palette_black_snapmaker));
        mTvTitle.setTextColor(ContextCompat.getColor(mContext,
                selected ? R.color.palette_blue_ribbon : R.color.palette_white_pure));
        mTvTitle.setTypeface(Typeface.defaultFromStyle(selected ? Typeface.BOLD : Typeface.NORMAL));

    }

    @Override
    public void setLineCount(int count) {
        // non implementation
    }
}
