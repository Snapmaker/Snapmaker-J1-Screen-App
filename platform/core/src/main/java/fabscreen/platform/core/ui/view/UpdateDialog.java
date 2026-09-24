package fabscreen.platform.core.ui.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R;

public class UpdateDialog {

    private static UpdateDialog sInstance;
    private AlertDialog mDialog;

    private TextView mTvTitle;
    private LinearLayout mLlChangeLog;
    private Button mBtnClose;
    private Button mBtnUpdate;

    public static UpdateDialog create(Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_update, null);
        dialog.setView(view);
        sInstance = new UpdateDialog();
        sInstance.mDialog = dialog;

        sInstance.mTvTitle = view.findViewById(R.id.tv_update_title);
        sInstance.mLlChangeLog = view.findViewById(R.id.ll_update_content);
        sInstance.mBtnClose = view.findViewById(R.id.iv_close);
        sInstance.mBtnUpdate = view.findViewById(R.id.btn_update_new);


        sInstance.mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return sInstance;
    }

    public UpdateDialog setTitle(String title) {
        mTvTitle.setText(title);
        return this;
    }

    public UpdateDialog setContent(List<ChangelogItem> items, Context context) {
        mLlChangeLog.removeAllViews();
        for (ChangelogItem item : items) {
            TextView textView = new TextView(context);
            textView.setTextColor(ContextCompat.getColor(context, R.color.palette_grey_french));
            textView.setText(item.words);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            if (item.type == ChangelogItem.ChangelogType.TITLE) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                layoutParams.leftMargin = (int) DimensUtils.dp2px(0f);
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                layoutParams.leftMargin = (int) DimensUtils.dp2px(12f);
            }
            mLlChangeLog.addView(textView, layoutParams);
        }
        return this;
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public UpdateDialog setOnClickUpdate(AlertDialog.OnClickListener listener) {
        mBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
            }
        });
        return this;
    }

    public UpdateDialog setCanceledOnTouchOutSide(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public void show(int width) {
        mDialog.show();

        Window window = mDialog.getWindow();
        if (window == null) return;
        window.setLayout(
                DimensUtils.dp2px(width, mDialog.getContext()),
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public static class ChangelogItem {
        public String words;
        public ChangelogType type;

        public ChangelogItem(String words, ChangelogType type) {
            this.words = words;
            this.type = type;
        }

        public enum ChangelogType {
            TITLE,
            DESC
        }
    }

}
