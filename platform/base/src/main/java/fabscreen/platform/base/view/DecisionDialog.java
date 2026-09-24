package fabscreen.platform.base.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
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
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.BaseAppService;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;

public class DecisionDialog {

    //    private static DecisionDialog sInstance;
    private static DecisionDialog sInstance;

    private AlertDialog mDialog;

    public boolean mIsSmallWidth = true;
    public ImageView mPic;
    public TextView mTitleTv;
    public TextView mWarmTv;
    public TextView mContentTv;
    public TextView mCancelBtn;
    public TextView mSecondBtn;
    public TextView mThirdBtn;
    public View mViewEmpty;
    public View mVNormalLine;
    public View mVDropShadowLine;

    public TextView mFirstLine;
    public TextView mSecondLine;
    public static final int BTN_ONE = 1;
    public static final int BTN_TWO = 2;
    public static final int BTN_THREE = 3;

    public static BaseAppService mApp;
    public static final int TIP_TYPE = 101;
    public static final int WARMING_TYPE = 102;
    public static final int ERROR_TYPE = 103;
    private int mType = TIP_TYPE;
    public static int mSoundId;
    public static boolean mIsJ1;

    public static DecisionDialog create(Context context) {
//        if (sInstance != null) {
//            sInstance.mDialog.cancel();
//            sInstance.mDialog.dismiss();
//        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_decision, null);
        dialog.setView(view);

        mApp = (BaseAppService) ServiceContainer.getInstance().getService(IAppService.class);

        sInstance = new DecisionDialog();
        sInstance.mDialog = dialog;

        sInstance.mTitleTv = view.findViewById(R.id.tv_title);
        sInstance.mContentTv = view.findViewById(R.id.tv_content);
        sInstance.mCancelBtn = view.findViewById(R.id.cancel_btn);
        sInstance.mSecondBtn = view.findViewById(R.id.second_btn);
        sInstance.mThirdBtn = view.findViewById(R.id.third_btn);
        sInstance.mFirstLine = view.findViewById(R.id.one_line);
        sInstance.mSecondLine = view.findViewById(R.id.second_line);
        sInstance.mPic = view.findViewById(R.id.iv_warm_pic);
        sInstance.mWarmTv = view.findViewById(R.id.tv_warm_tip);
        sInstance.mViewEmpty = view.findViewById(R.id.view_empty);
        sInstance.mVNormalLine = view.findViewById(R.id.v_normal_line);
        sInstance.mVDropShadowLine = view.findViewById(R.id.v_drop_shadow_line);
        sInstance.mPic.setVisibility(View.GONE);
        sInstance.mWarmTv.setVisibility(View.GONE);
        sInstance.mContentTv.setVisibility(View.GONE);
        sInstance.mViewEmpty.setVisibility(View.GONE);
        mIsJ1 = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J;
        if (mIsJ1) {
            sInstance.mContentTv.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.palette_grey_french));
            sInstance.mVNormalLine.setVisibility(View.VISIBLE);
            sInstance.mVDropShadowLine.setVisibility(View.GONE);
        } else {
            sInstance.mVNormalLine.setVisibility(View.GONE);
            sInstance.mVDropShadowLine.setVisibility(View.VISIBLE);
            sInstance.mContentTv.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.palette_white_silver));
            sInstance.mContentTv.setTextSize(24);
            sInstance.mSecondBtn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        sInstance.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!mIsJ1){
                    SoundUtil.stopSound(mApp.getSoundPool(), mSoundId);
                }
            }
        });

        return sInstance;
    }

    /**
     * @param btnNum        Number of buttons
     * @param hasPic        have picture?
     * @param hasWarmPrompt
     * @param hasTitle
     * @param sSmallWidth   j1 only select true
     *                      a400 have two width
     * @return
     */
    public DecisionDialog setDialogStatus(int btnNum, boolean hasPic, boolean hasWarmPrompt, boolean hasTitle, boolean sSmallWidth) {
        switch (btnNum) {
            case BTN_ONE:
                mSecondBtn.setVisibility(View.GONE);
                mThirdBtn.setVisibility(View.GONE);

                mFirstLine.setVisibility(View.GONE);
                mSecondLine.setVisibility(View.GONE);
                break;
            case BTN_TWO:
                mThirdBtn.setVisibility(View.GONE);
                mSecondLine.setVisibility(View.GONE);
                break;
            case BTN_THREE:
                break;
        }
        sInstance.mPic.setVisibility(hasPic ? View.VISIBLE : View.GONE);
        sInstance.mWarmTv.setVisibility(hasWarmPrompt ? View.VISIBLE : View.GONE);
        sInstance.mTitleTv.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
        sInstance.mIsSmallWidth = sSmallWidth;
        return this;
    }

    public static DecisionDialog getsInstance() {
        return sInstance;
    }

    public DecisionDialog setPic(int imageResource) {
        mPic.setImageResource(imageResource);
        mPic.setVisibility(View.VISIBLE);
        return this;
    }

    public DecisionDialog setContent(@StringRes int contentId) {
        mContentTv.setText(contentId);
        mContentTv.setVisibility(View.VISIBLE);
        return this;
    }

    public DecisionDialog setContent(String content) {
        mContentTv.setText(content);
        mContentTv.setVisibility(View.VISIBLE);
        return this;
    }

    public DecisionDialog setContentColor(int color) {
        mContentTv.setTextColor(ContextCompat.getColor(mDialog.getContext(), color));
        mContentTv.setVisibility(View.VISIBLE);
        return this;
    }

    public DecisionDialog setTitle(@StringRes int titleId) {
        mTitleTv.setText(titleId);
        mTitleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public DecisionDialog setTitle(String title) {
        mTitleTv.setText(title);
        mTitleTv.setVisibility(View.VISIBLE);
        return this;
    }

    //if j1 dialog height is insufficient,please using this
    public DecisionDialog needMoreHeight() {
        mViewEmpty.setVisibility(View.VISIBLE);
        return this;
    }

    public DecisionDialog setCanceledOnTouchOutSide(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public DecisionDialog setFirstTv(String content, int color, AlertDialog.OnClickListener listener) {
        mCancelBtn.setText(content);
        mCancelBtn.setTextColor(ContextCompat.getColorStateList(mDialog.getContext(), color));
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
                SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
            }
        });
        return this;
    }

    public DecisionDialog setFirstTv(int contentId, int color, AlertDialog.OnClickListener listener) {
        mCancelBtn.setText(contentId);
        mCancelBtn.setTextColor(ContextCompat.getColorStateList(mDialog.getContext(), color));
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
                SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
            }
        });
        return this;
    }

    public DecisionDialog setSecondTv(String content, int color, AlertDialog.OnClickListener listener) {
        mSecondBtn.setText(content);
        mSecondBtn.setTextColor(ContextCompat.getColorStateList(mDialog.getContext(), color));
        mSecondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
                SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
            }
        });
        return this;
    }

    public DecisionDialog setSecondTv(@StringRes int contentId, int color, AlertDialog.OnClickListener listener) {
        mSecondBtn.setText(contentId);
        mSecondBtn.setTextColor(ContextCompat.getColorStateList(mDialog.getContext(), color));
        mSecondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
                SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
            }
        });
        return this;
    }

    public DecisionDialog setThirdTv(String content, int color, AlertDialog.OnClickListener listener) {
        mThirdBtn.setText(content);
        mThirdBtn.setTextColor(ContextCompat.getColorStateList(mDialog.getContext(), color));
        mThirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
                SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
            }
        });
        return this;
    }

    public DecisionDialog setThirdTv(@StringRes int contentId, int color, AlertDialog.OnClickListener listener) {
        mThirdBtn.setText(contentId);
        mThirdBtn.setTextColor(ContextCompat.getColorStateList(mDialog.getContext(), color));
        mThirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, 0);
                SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
            }
        });
        return this;
    }

    public DecisionDialog setType(int type) {
        mType = type;
        return this;
    }

    public DecisionDialog setWarmTv(String content, int color) {
        mWarmTv.setText(content);
        mWarmTv.setTextColor(ContextCompat.getColor(mDialog.getContext(), color));
        return this;
    }

    public DecisionDialog setWarmTv(@StringRes int contentId, int color) {
        mWarmTv.setText(contentId);
        mWarmTv.setTextColor(ContextCompat.getColor(mDialog.getContext(), color));
        return this;
    }

    public void show() {
        mDialog.show();

        Window window = mDialog.getWindow();
        if (window == null) return;
        TypedValue typedValue = new TypedValue();
        mDialog.getContext().getTheme().resolveAttribute(mIsSmallWidth ? R.attr.theme_dialog_small_width : R.attr.theme_dialog_middle_width
                , typedValue, true);
        window.setLayout(
                DimensUtils.dp2px(typedValue.getFloat(), mDialog.getContext()),
                WindowManager.LayoutParams.WRAP_CONTENT);

        if (mIsJ1){
            return;
        }

        switch (mType) {
            case TIP_TYPE:
                mSoundId = SoundUtil.playSoundHasId(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_dialog_tip));
                break;
            case WARMING_TYPE:
                mSoundId = SoundUtil.playSoundHasId(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_dialog_warming));
                break;
            case ERROR_TYPE:
                mSoundId = SoundUtil.playSoundHasId(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_dialog_error));
                break;
        }
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
