package fabscreen.platform.base.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class FabConfirm {
    private static Context mContext;
    @BindView(R2.id.btn_dialog_confirm_cancel)
    public Button mBtnCancel;
    @BindView(R2.id.btn_dialog_confirm_confirm)
    public Button mBtnConfirm;
    @BindView(R2.id.tv_dialog_confirm_desc)
    TextView mTvDescription;
    @BindView(R2.id.iv_dialog_confirm_icon)
    ImageView mIvIcon;
    @BindView(R2.id.tv_dialog_title_desc)
    TextView mTvTitle;
    int mResid;
    String mRes;
    private AlertDialog mDialog;
    private int mTime = 0;
    private Disposable subscribe;

    FabConfirm(AlertDialog dialog, View view) {
        mDialog = dialog;
        ButterKnife.bind(this, view);
    }

    public static FabConfirm create(Context context) {
        mContext = context;
        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(
                    DimensUtils.dp2px(280f, context),
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // create view
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_confirm, null);
//        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);

        // instance
        return new FabConfirm(dialog, view);
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public FabConfirm setDescription(int resid) {
        mTvDescription.setText(resid);
        return this;
    }

    public FabConfirm setDescription(String desc) {
        mTvDescription.setText(desc);
        return this;
    }

    public FabConfirm setTitle(int resid) {
        mTvTitle.setText(resid);
        return this;
    }

    public FabConfirm setTitle(String desc) {
        mTvTitle.setText(desc);
        return this;
    }

    public FabConfirm setIcon(int resid) {
        mIvIcon.setVisibility(ImageView.VISIBLE);
        mIvIcon.setImageResource(resid);
        return this;
    }

    public FabConfirm setCancel(int resid, AlertDialog.OnClickListener listener) {
        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnCancel.setText(resid);
        mBtnCancel.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public FabConfirm setCancel(String res, AlertDialog.OnClickListener listener) {
        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnCancel.setText(res);
        mBtnCancel.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public FabConfirm setConfirm(int resid, AlertDialog.OnClickListener listener) {
        mBtnConfirm.setText(resid);
        mBtnConfirm.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public FabConfirm setConfirm(String res, AlertDialog.OnClickListener listener) {
        mBtnConfirm.setText(res);
        mBtnConfirm.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public FabConfirm setConfirm(int resid, int time, AlertDialog.OnClickListener listener) {
        mTime = time;
        mResid = resid;
        mBtnConfirm.setText(resid);
        mBtnConfirm.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public FabConfirm setConfirm(String res, int time, AlertDialog.OnClickListener listener) {
        mTime = time;
        mRes = res;
        mBtnConfirm.setText(res);
        mBtnConfirm.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public FabConfirm setCanceledOnTouchOutSide(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public void show() {
        mDialog.show();
        if (mTime != 0) {
            mBtnConfirm.setEnabled(false);
            if (subscribe != null && !subscribe.isDisposed()) subscribe.dispose();
            subscribe = Observable.intervalRange(0, (mTime / 1000) + 1, 0, 1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        if (mResid != 0) {
                            mBtnConfirm.setText(mContext.getString(mResid) + String.format("(%d s)", mTime / 1000 - time));
                            if (time == mTime / 1000) {
                                mBtnConfirm.setText(mContext.getString(mResid));
                                mBtnConfirm.setEnabled(true);
                                subscribe.dispose();
                            }
                        } else if (!mRes.isEmpty()) {
                            mBtnConfirm.setText(mRes + String.format("(%d s)", mTime / 1000 - time));
                            if (time == mTime / 1000) {
                                mBtnConfirm.setText(mRes);
                                mBtnConfirm.setEnabled(true);
                                subscribe.dispose();
                            }
                        }


                    }, LogHelper::log);
        }

        // Dynamically change dialog width (trick)
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            mDialog.getWindow().setLayout(300 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public void dismiss() {
        mDialog.dismiss();
    }
}
