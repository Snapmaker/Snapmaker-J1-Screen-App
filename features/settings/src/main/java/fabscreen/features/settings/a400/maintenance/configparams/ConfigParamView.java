package fabscreen.features.settings.a400.maintenance.configparams;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import fabscreen.features.settings.R;

public class ConfigParamView extends LinearLayout {

    private OnEditClickListener mOnEditClickListener;
    private TextView mTvTitle;
    private TextView mTvNumber;

    public ConfigParamView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        View view = View.inflate(getContext(), R.layout.item_config_param, this);
        mTvTitle = view.findViewById(R.id.tv_title);
        mTvNumber = view.findViewById(R.id.tv_number);
        Button btnEdit = view.findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(v -> {
            if (mOnEditClickListener == null) return;
            mOnEditClickListener.onClick();
        });
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setValue(String number) {
        mTvNumber.setText(number);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        mOnEditClickListener = listener;
    }

    interface OnEditClickListener {
        void onClick();
    }
}
