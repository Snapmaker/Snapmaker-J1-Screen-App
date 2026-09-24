package fabscreen.platform.core.ui.view.leftsection;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import fabscreen.platform.core.R;

public class A400LeftSectionsView extends LinearLayout implements ILeftSectionView {

    private TextView mTvTitle;
    private ImageView mIvIcon;

    public A400LeftSectionsView(Context context) {
        this(context, null);
    }

    public A400LeftSectionsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.btn_a400_left_selection, this, true);
        mTvTitle = view.findViewById(R.id.tv_title);
        mIvIcon = view.findViewById(R.id.iv_icon);
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
        mIvIcon.setImageResource(drawableResId);
    }

    @Override
    public void setShowBadge(boolean show) {
        // no implementation
    }

    @Override
    public void setSelected(boolean selected) {
        mTvTitle.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        mIvIcon.setSelected(selected);
        super.setSelected(selected);
    }

    @Override
    public void setLineCount(int count) {
        // non implementation
    }
}
