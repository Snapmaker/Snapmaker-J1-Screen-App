package fabscreen.platform.base.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import fabscreen.platform.base.R;
import fabscreen.platform.base.helper.DimensUtils;

public class FileLoadingDialog {

    private static FileLoadingDialog sInstance;

    private AlertDialog mDialog;

    public CircularProgressIndicator mProgressbar;
    //    public CircularProgressView mCirProgressbar;
    public TextView mTvProgressContent;
    public TextView mTvContent;
    private static boolean mIsJ1;

    public static FileLoadingDialog create(Context context, boolean isJ1) {
        mIsJ1 = isJ1;
        if (sInstance != null) {
            sInstance.mDialog.cancel();
            sInstance.mDialog.dismiss();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(mIsJ1 ? R.layout.dialog_j1_file_loading : R.layout.dialog_a400_file_loading, null);
        dialog.setView(view);
        sInstance = new FileLoadingDialog();
        sInstance.mDialog = dialog;
        if (mIsJ1) {
            sInstance.mProgressbar = view.findViewById(R.id.progressbar);
            sInstance.mTvContent = view.findViewById(R.id.tv_dialog_loading_tv);
        } else {
            sInstance.mProgressbar = view.findViewById(R.id.progressbar);
            sInstance.mTvProgressContent = view.findViewById(R.id.tv_progress_content);
            sInstance.mTvContent = view.findViewById(R.id.tv_dialog_loading_tv);
        }

        return sInstance;
    }

    public FileLoadingDialog setCanceledOnTouchOutSide(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public FileLoadingDialog setContent(String progress) {
        mTvContent.setText(progress);
        return this;
    }

    public FileLoadingDialog setProgress(int progress) {
        mTvProgressContent.setText(progress + "%");
        mProgressbar.setProgress(progress);
        return this;
    }

    public void show() {
        mDialog.show();
        Window window = mDialog.getWindow();
        if (mIsJ1) {
            mProgressbar.show();
        }
        if (window == null) {
            return;
        }
        window.setLayout(
                mIsJ1 ? WindowManager.LayoutParams.WRAP_CONTENT : DimensUtils.dp2px(580, mDialog.getContext()),
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            if (mProgressbar != null) {
                mProgressbar.hide();
            }
            mDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }
}
