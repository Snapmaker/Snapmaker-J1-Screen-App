package fabscreen.platform.base.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import fabscreen.platform.base.R;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;

public class WarmTipDialog {

    public enum WarmTipDialogSize {
        SIZE_S,
        SIZE_M
    }

    private static WarmTipDialog sInstance;
    private AlertDialog mDialog;
    private boolean mIsSmallWidth = true;
    private WarmTipDialogSize mDialogSize = WarmTipDialogSize.SIZE_S;
    private TextView mTipTv;
    private ImageView mTipImg;
    private TextView mTitleTv;

    public static WarmTipDialog create(Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();

        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_tip, null);
        dialog.setView(view);

        sInstance = new WarmTipDialog();
        sInstance.mDialog = dialog;
        sInstance.mDialog.setCanceledOnTouchOutside(false);
        sInstance.mTipTv = view.findViewById(R.id.tip_tv);
        sInstance.mTipImg = view.findViewById(R.id.tip_icon);
        sInstance.mTitleTv = view.findViewById(R.id.title_tv);

        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.A) {
            sInstance.mTipTv.setTextColor(ContextCompat.getColor(context, R.color.palette_white_silver));
            sInstance.mTipTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        }

        dialog = builder.create();
        dialog.setView(view);

        return sInstance;
    }

    public static WarmTipDialog getsInstance() {
        return sInstance;
    }

    public WarmTipDialog setPic(int imgResources) {
        sInstance.mTipImg.setImageResource(imgResources);
        sInstance.mTipImg.setVisibility(View.VISIBLE);
        return this;
    }

    public WarmTipDialog setTitle(String title) {
        sInstance.mTitleTv.setText(title);
        sInstance.mTitleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public WarmTipDialog setTitle(@StringRes int titleId) {
        sInstance.mTitleTv.setText(titleId);
        sInstance.mTitleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public WarmTipDialog setContent(@StringRes int content) {
        sInstance.mTipTv.setText(content);
        sInstance.mTipTv.setVisibility(View.VISIBLE);
        return this;
    }

    public WarmTipDialog setContent(String content) {
        sInstance.mTipTv.setText(content);
        sInstance.mTipTv.setVisibility(View.VISIBLE);
        return this;
    }

    public WarmTipDialog setOUtSideCanTouch(boolean canTouch) {
        mDialog.setCanceledOnTouchOutside(canTouch);
        return this;
    }

    public WarmTipDialog setDialogWidthSize(WarmTipDialogSize size) {
        mDialogSize = size;
        return this;
    }

    public void show() {
        mDialog.show();
        Window window = mDialog.getWindow();
        if (window == null) return;
        TypedValue typedValue = new TypedValue();
        int dialogWidth = R.attr.theme_dialog_small_width;
        switch (mDialogSize) {
            case SIZE_S:
                dialogWidth = R.attr.theme_dialog_small_width;
                break;
            case SIZE_M:
                dialogWidth = R.attr.theme_dialog_middle_width;
                break;
        }
        mDialog.getContext().getTheme().resolveAttribute(dialogWidth, typedValue, true);
        window.setLayout(
                DimensUtils.dp2px(typedValue.getFloat(), mDialog.getContext()),
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

}
