package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import fabscreen.platform.core.R;

public class ShowState extends ConstraintLayout {
    ImageView mIvShowState;
    TextView mTvStateName;
    TextView mTvStateTemperature;
    View mViewSeparator;
    TextView mTvStateTargetTemp;

    public ShowState(Context context) {
        this(context, null);
    }

    public ShowState(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowState(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ShowState(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

//        initAttrs(attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View parentView = inflater.inflate(R.layout.view_show_state, this, true);

        mIvShowState = parentView.findViewById(R.id.iv_show_state);
        mTvStateName = parentView.findViewById(R.id.tv_show_state_name);
        mTvStateTemperature = parentView.findViewById(R.id.tv_show_state_temperature);
        mViewSeparator = parentView.findViewById(R.id.view_show_state_separator_);
        mTvStateTargetTemp = parentView.findViewById(R.id.tv_show_state_target_temp);
    }


    public void ChangeShowStateDrawable(@DrawableRes int DrawableId) {
        mIvShowState.setImageResource(DrawableId);
    }

    public void ChangeTemperature(String temperature) {
        mTvStateTemperature.setText(temperature);
    }

    public void ChangeName(@StringRes int newNameId) {
        mTvStateName.setText(newNameId);
    }

    public void ChangeName(String newName) {
        mTvStateName.setText(newName);
    }

    public void ChangeTargetTemp(String temperature) {
        mViewSeparator.setVisibility(VISIBLE);
        mTvStateTargetTemp.setVisibility(VISIBLE);
        mTvStateTargetTemp.setText(temperature);
    }

    public void initView(
            @DrawableRes int drawableId,
            @StringRes int nameId,
            String temperature) {
        mIvShowState.setImageResource(drawableId);
        mTvStateName.setText(nameId);
        mTvStateTemperature.setText(temperature + " ℃");
    }

    public void initView(
            @DrawableRes int drawableId,
            String nameId,
            String temperature) {
        mIvShowState.setImageResource(drawableId);
        mTvStateName.setText(nameId);
        mTvStateTemperature.setText(temperature + " ℃");
    }

}
