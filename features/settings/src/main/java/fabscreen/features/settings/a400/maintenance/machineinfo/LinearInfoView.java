package fabscreen.features.settings.a400.maintenance.machineinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import fabscreen.features.settings.R;
import fabscreen.platform.base.helper.DimensUtils;

public class LinearInfoView extends LinearLayout {

    private TextView mTvLinearTitle;
    private TextView mTvAttributeContent;

    public LinearInfoView(Context context) {
        this(context, null);
    }

    public LinearInfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.shape_a400_bg_machine_info);
        setPadding(0, DimensUtils.dp2pxInt(48), 0, DimensUtils.dp2pxInt(48));
        setOrientation(VERTICAL);

        View.inflate(getContext(), R.layout.item_merge_machine_info_linear_module, this);
        mTvLinearTitle = findViewById(R.id.tv_linear_title);
        View attributeItemView = findViewById(R.id.miiv_linear_limit_switch);
        TextView tvAttributeTitle = attributeItemView.findViewById(R.id.tv_attribute_title);
        tvAttributeTitle.setText("Limit Switch");
        mTvAttributeContent = attributeItemView.findViewById(R.id.tv_attribute_value);
        mTvAttributeContent.setText("Off");
    }

    public void setLinearTitle(String title) {
        mTvLinearTitle.setText(title);
    }

    public void setLinearTitle(@StringRes int title) {
        mTvLinearTitle.setText(title);
    }

    public void setAttributeContent(String content) {
        mTvAttributeContent.setText(content);
    }

    public void setAttributeContent(@StringRes int content) {
        mTvAttributeContent.setText(content);
    }
}
