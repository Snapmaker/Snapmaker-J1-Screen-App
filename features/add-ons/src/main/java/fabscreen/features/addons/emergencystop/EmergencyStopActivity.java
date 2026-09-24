package fabscreen.features.addons.emergencystop;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.ButterKnife;
import fabscreen.features.addons.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.ADDONS_EMERGENCY_STOP)
public class EmergencyStopActivity extends BaseActivity {

    private boolean mIsTriggerOnPowerUp;
    private AlertDialog mDialog;
    private int mStreamId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mIsTriggerOnPowerUp = bundle.getBoolean("is_triggered_on_power_up");
        }

        mStreamId = SoundUtil.playSoundLoop(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_emergency_stop));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showEmergencyWarningDialog(mIsTriggerOnPowerUp);
    }

    private void showEmergencyWarningDialog(boolean isTriggeredOnPowerUp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        if (mDialog != null) {
            if (mDialog.isShowing()) return;
        }
        mDialog = builder.create();
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            mDialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(isTriggeredOnPowerUp
                        ? R.layout.dialog_emergency_stop_warning_2
                        : R.layout.dialog_emergency_stop_warning,
                null);
        mDialog.setView(view);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SoundUtil.stopSound(mApp.getSoundPool(), mStreamId);
    }
}
