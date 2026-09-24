package fabscreen.platform.core.ui.common.selection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import fabscreen.platform.core.R;


public class CalibrationModeSelectionView extends LinearLayout {
    private TextView mTvTitle;
    private TextView mTvContent;
    private View mViewCheck;
    private View mViewMode;
    private RelativeLayout mRl;
    private Context mContext;
    private View view;

    public CalibrationModeSelectionView(@NonNull Context context) {
        this(context, null);
        mContext=context;
    }

    public CalibrationModeSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        init();
    }

    private void init() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.button_calibration_right_section, this, true);
        mTvTitle = view.findViewById(R.id.tv_calibration_mode_title);
        mTvContent = view.findViewById(R.id.tv_calibration_mode_content);
        mViewCheck = view.findViewById(R.id.view_calibration_mode_check);
        mViewMode = view.findViewById(R.id.view_calibration_mode);
        mRl = view.findViewById(R.id.rl_calibration_mode);
    }

    public void setTitle(int titleRes) {
        mTvTitle.setText(titleRes);
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setContent(int contentRes) {
        mTvContent.setVisibility(TextView.VISIBLE);
        mTvContent.setText(contentRes);
    }

    public void setContent(String content) {
        mTvContent.setVisibility(TextView.VISIBLE);
        mTvContent.setText(content);
    }

    @Override
    public void setSelected(boolean selected) {
        mViewCheck.setVisibility(selected ? VISIBLE : INVISIBLE);
        mViewMode.setVisibility(selected ? VISIBLE : INVISIBLE);
        mTvTitle.setTextColor(ContextCompat.getColor(mContext,selected ? R.color.palette_blue_ribbon : R.color.palette_white_pure));
        mTvTitle.setMaxLines(selected ? 5 : 2);
        mRl.setBackgroundColor(ContextCompat.getColor(mContext, selected? R.color.palette_grey_nero : R.color.palette_black_snapmaker));
    }
}
