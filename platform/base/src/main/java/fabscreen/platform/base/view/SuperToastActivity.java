package fabscreen.platform.base.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;

public class SuperToastActivity extends Activity {
    @Nullable
    @BindView(R2.id.btn_dialog_close)
    Button mIvClose;

    public static final String TOAST_MESSAGE = "TOAST_MESSAGE";
    public static final String TOAST_DRAWABLE = "TOAST_DRAWABLE";
    public static final String TOAST_TITLE = "TOAST_TITLE";
    public static final String TOAST_SHOW_TIME = "TOAST_SHOW_TIME";
    public static final String TOAST_J1_USB = "TOAST_J1_USB";
    public static final String TOAST_PERMANENT_DISPLAY = "TOAST_PERMANENT_DISPLAY";
    public static final int DEFAULT_SHOW_TIME = 1750;
    public static final int VIEW_DONE = -1;
    private boolean mIsJ1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int mSeriesId = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId;
        mIsJ1 = mSeriesId == IMachine.MachineSeries.J;
        setContentView(mIsJ1 ? R.layout.activity_j1_toast : R.layout.activity_a400_toast);
        ButterKnife.bind(this);
        onNewIntent(getIntent());
        // Play sound effect when toast activity created.
        IAppService appService = ServiceContainer.getInstance().getService(IAppService.class);
        SoundUtil.playSound(appService.getSoundPool(), appService.getSoundIdByResourceId(R.raw.sound_toast_show));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int toastPicId = getIntent().getIntExtra(TOAST_DRAWABLE, VIEW_DONE);
        ImageView ivLogo = findViewById(R.id.iv_dialog_logo);
        TextView tvNormalView = findViewById(R.id.tv_normal_view);
        if (toastPicId == -1) {
            ivLogo.setVisibility(View.GONE);
            tvNormalView.setVisibility(View.VISIBLE);
        } else {
            ivLogo.setVisibility(View.VISIBLE);
            tvNormalView.setVisibility(View.GONE);
            ivLogo.setImageResource(toastPicId);
        }

        String titleContent = getIntent().getStringExtra(TOAST_TITLE);
        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        if (TextUtils.isEmpty(titleContent)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(titleContent);
        }

        String message = getIntent().getStringExtra(TOAST_MESSAGE);
        TextView tvContent = findViewById(R.id.iv_dialog_content);
        if (TextUtils.isEmpty(message)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(message);
        }

        int j1UsbDrawable = getIntent().getIntExtra(TOAST_J1_USB, VIEW_DONE);
        if (j1UsbDrawable != VIEW_DONE && mIsJ1) {
            ImageView ivUsbLogo = findViewById(R.id.iv_j1_usb_logo);
            ivUsbLogo.setVisibility(View.VISIBLE);
            ivUsbLogo.setImageResource(j1UsbDrawable);
        }
        int showTime = intent.getIntExtra(TOAST_SHOW_TIME, DEFAULT_SHOW_TIME);

        getWindow().setGravity(mIsJ1 ? Gravity.CENTER : Gravity.TOP);

        boolean isPermanentDisplay = intent.getBooleanExtra(TOAST_PERMANENT_DISPLAY, false);

        if (mIvClose!=null){
            mIvClose.setVisibility(isPermanentDisplay ? View.VISIBLE : View.GONE);
        }

        new Handler().postDelayed(() -> {
            finish();
            overridePendingTransition(0, R.anim.push_up_out);
        }, showTime);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            finish();
            overridePendingTransition(0, R.anim.push_up_out);
        }

        return super.onTouchEvent(event);
    }

    @Optional
    @OnClick(R2.id.btn_dialog_close)
    void onClickClose() {
        finish();
        overridePendingTransition(0, R.anim.push_up_out);
    }
}
