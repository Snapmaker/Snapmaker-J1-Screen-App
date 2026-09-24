package fabscreen.features.settings.a400.maintenance.machineinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import fabscreen.features.settings.R;

public class MachineInfoItemView extends LinearLayout {

    private TextView mTvTitle;
    private TextView mTvContent;

    public MachineInfoItemView(Context context) {
        this(context, null);
    }

    public MachineInfoItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MachineInfoItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);

        View itemView = View.inflate(getContext(), R.layout.item_merge_machine_info_attribute, this);
        mTvTitle = itemView.findViewById(R.id.tv_attribute_title);
        mTvContent = itemView.findViewById(R.id.tv_attribute_value);
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setTitle(@StringRes int title) {
        mTvTitle.setText(title);
    }

    public void setContent(String content) {
        mTvContent.setText(content);
    }

    public void setContent(@StringRes int content) {
        mTvContent.setText(content);
    }
}
