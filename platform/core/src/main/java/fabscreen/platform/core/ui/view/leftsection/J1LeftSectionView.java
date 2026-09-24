package fabscreen.platform.core.ui.view.leftsection;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R;

public class J1LeftSectionView extends LinearLayout implements ILeftSectionView {

    private TextView mTvTitle;
    private View mViewBadge;
    private View mViewIndicator;

    public J1LeftSectionView(@NonNull Context context) {
        this(context, null);
    }

    public J1LeftSectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.button_j1_left_section, this, true);
        mTvTitle = view.findViewById(R.id.tv_title);
        mViewBadge = view.findViewById(R.id.v_badge);
        mViewIndicator = view.findViewById(R.id.v_indicator);

        mViewBadge.setVisibility(GONE);
        mViewIndicator.setVisibility(GONE);
    }

    public void setTitle(int titleRes) {
        mTvTitle.setText(titleRes);
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    @Override
    public void setIcon(int drawableResId) {
        // no implementation
    }

    public void setShowBadge(boolean show) {
        mViewBadge.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void setSelected(boolean selected) {
        mViewIndicator.setVisibility(selected ? VISIBLE : GONE);
        mTvTitle.setTextColor(selected ? 0xFFF7F8FA : 0xFF9B9CA6);
        mTvTitle.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        if (mViewBadge.getVisibility() == VISIBLE) {
            mViewBadge.setVisibility(GONE);
        }
    }

    @Override
    public void setLineCount(int count) {
        if (count == 3) {
            mTvTitle.setLineSpacing(0, 1);
        } else {
            mTvTitle.setLineSpacing(DimensUtils.dp2px(8), 1);
        }
    }
}
