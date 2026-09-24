package fabscreen.platform.core.ui.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

public class StepIntroductionDialog {
    @BindView(R2.id.tv_step_introduction_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_step_introduction_content)
    TextView mTvContent;
    @BindView(R2.id.iv_step_introduction)
    ImageView mIvContent;
    @BindView(R2.id.iv_step_introduction_back)
    ImageView mIvBack;
    private AlertDialog mDialog;

    public StepIntroductionDialog(AlertDialog dialog, View view) {
        mDialog = dialog;
        ButterKnife.bind(this, view);
    }


    public static StepIntroductionDialog create(Context context) {
        // create dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_step_introduction, null);
        dialog.setView(view);

        return new StepIntroductionDialog(dialog, view);
    }

    public StepIntroductionDialog setTitle(@StringRes int resId) {
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setText(resId);
        return this;
    }

    public StepIntroductionDialog setTitle(String resStr) {
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setText(resStr);
        return this;
    }

    public StepIntroductionDialog setContent(@StringRes int resId) {
        mTvContent.setVisibility(View.VISIBLE);
        mTvContent.setText(resId);
        return this;
    }

    public StepIntroductionDialog setContent(String resStr) {
        mTvContent.setVisibility(View.VISIBLE);
        mTvContent.setText(resStr);
        return this;
    }

    public StepIntroductionDialog setImage(@DrawableRes int resId) {
        mIvContent.setVisibility(View.VISIBLE);
        mIvContent.setImageResource(resId);
        return this;
    }

    public StepIntroductionDialog setOnClickBack(View.OnClickListener listener) {
        mIvBack.setVisibility(View.VISIBLE);
        mIvBack.setOnClickListener(listener);
        return this;
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void setCanceledOnTouchOutSide(boolean b) {
        mDialog.setCanceledOnTouchOutside(b);
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
            Window window = mDialog.getWindow();
            if (window == null) return;
            window.setLayout(
                    DimensUtils.dp2px(1184, mDialog.getContext()),
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
