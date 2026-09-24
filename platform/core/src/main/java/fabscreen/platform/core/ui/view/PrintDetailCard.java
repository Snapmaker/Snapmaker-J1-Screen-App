package fabscreen.platform.core.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

@SuppressLint("NonConstantResourceId")
public class PrintDetailCard extends ConstraintLayout {

    @BindView(R2.id.iv_print_details_card_icon)
    ImageView mIvPrintDetailsIcon;
    @BindView(R2.id.tv_print_details_card_name)
    TextView mTvPrintDetailsName;
    @BindView(R2.id.pb_print_details_card)
    CircleProgressView mProgressBar;
    @BindView(R2.id.tv_print_details_current_value)
    TextView mTvPrintDetailsCurrentValue;
    @BindView(R2.id.tv_print_details_target_value)
    TextView mTvPrintDetailsTargetValue;
    @BindView(R2.id.view_print_details_target_value)
    View mViewPrintDetailsTargetValue;
    @BindView(R2.id.tv_print_details_current_unit)
    TextView mTvPrintDetailsCurrentUnit;

    boolean mIsShowTarget = true;

    public PrintDetailCard(Context context) {
        this(context, null);
    }


    public PrintDetailCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_a400_print_details, this);
        ButterKnife.bind(this, view);
    }

    public PrintDetailCard setIcon(@DrawableRes int iconId) {
        mIvPrintDetailsIcon.setImageResource(iconId);
        return this;
    }

    public PrintDetailCard setDetailsName(String str) {
        mTvPrintDetailsName.setText(str);
        return this;
    }

    public PrintDetailCard setDetailsName(@StringRes int strId) {
        mTvPrintDetailsName.setText(strId);
        return this;
    }


    public PrintDetailCard setProgressValue(int progressValue) {
        mProgressBar.setPercentage(progressValue);
        return this;
    }

    public PrintDetailCard setDetailsCurrentValue(int detailsCurrentValue) {
        mTvPrintDetailsCurrentValue.setText(detailsCurrentValue + "℃");
        return this;
    }

    public PrintDetailCard setDetailsTargetValue(int detailsTargetValue) {
        mViewPrintDetailsTargetValue.setVisibility(mIsShowTarget ? VISIBLE : INVISIBLE);
        mTvPrintDetailsTargetValue.setVisibility(mIsShowTarget ? VISIBLE : INVISIBLE);
        mTvPrintDetailsTargetValue.setText(detailsTargetValue + "℃");
        return this;
    }

    public PrintDetailCard setDetailsPercentValue(int detailsCurrentValue) {
        mTvPrintDetailsCurrentValue.setText(detailsCurrentValue + "%");
        setShowDetailsTargetValue(false);
        return this;
    }

    public PrintDetailCard setDetailsSingleValue(int detailsCurrentValue) {
        mTvPrintDetailsCurrentValue.setText(detailsCurrentValue + "");
        setShowDetailsTargetValue(false);
        mTvPrintDetailsCurrentUnit.setVisibility(VISIBLE);
        mTvPrintDetailsCurrentUnit.setText(R.string.all_spindle_speed_unit);
        return this;
    }

    public PrintDetailCard setShowDetailsTargetValue(boolean isShow) {
        mIsShowTarget = isShow;
        mViewPrintDetailsTargetValue.setVisibility(mIsShowTarget ? VISIBLE : GONE);
        mTvPrintDetailsTargetValue.setVisibility(mIsShowTarget ? VISIBLE : GONE);
        return this;
    }

}
