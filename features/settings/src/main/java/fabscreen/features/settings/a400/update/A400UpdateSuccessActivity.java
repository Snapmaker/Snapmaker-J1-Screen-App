package fabscreen.features.settings.a400.update;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.res.ResourcesCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.receiver.InstallProcessReceiver;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.A400_SETTINGS_UPDATE_SUCCESS)
public class A400UpdateSuccessActivity extends BaseActivity {
    private static final String TAG = "A400UpdateSuccessActivity";

    public static final String KEY_UPDATE_EVENT = "updateEvent";

    private static final int EVENT_BIG_BIN = 0;
    private static final int EVENT_EM_BIN = 1;
    private static final int EVENT_STARTUP = 2;

    private int mEvent = -1;

    @BindView(R2.id.ll_updated)
    LinearLayout mLlUpdated;
    @BindView(R2.id.ll_update_when_plugged_in)
    LinearLayout mLlUpdatedWhenPluggedIn;
    @BindView(R2.id.ll_updated_startup)
    LinearLayout mLlUpdatedStartup;
    @BindView(R2.id.btn_take_action)
    Button mBtnTakeAction;
    @BindView(R2.id.group_update)
    Group mGroupUpdate;
    @BindView(R2.id.group_startup)
    Group mGroupStartup;
    private UpdateSuccessViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel(UpdateSuccessViewModel.class);
        setContentView(R.layout.activity_update_success);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEvent = getIntent().getIntExtra(KEY_UPDATE_EVENT, -1);
        boolean fromStartup = mEvent == EVENT_STARTUP;
        mBtnTakeAction.setText(fromStartup ? "Complete" : "Restart Now");
        mGroupUpdate.setVisibility(fromStartup ? View.INVISIBLE : View.VISIBLE);
        mGroupStartup.setVisibility(fromStartup ? View.VISIBLE : View.INVISIBLE);

        mViewModel.getUpdateChangesObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshView, e -> {
                    UpdateSuccessViewModel.FirmwareChanges changes = new UpdateSuccessViewModel.FirmwareChanges();
                    changes.updatedHWNameList.add(e.toString());
                    refreshView(changes);
                    LogHelper.log(e);
                });
    }

    private void refreshView(UpdateSuccessViewModel.FirmwareChanges changes) {
        Logger.d("HW change list: %s", changes.updatedHWNameList);
        refreshByView(mLlUpdated, changes.updatedHWNameList);
        refreshByView(mLlUpdatedWhenPluggedIn, changes.toBeUpdatedNameList);
        refreshByView(mLlUpdatedStartup, changes.updatedHWNameList);
    }

    private void refreshByView(LinearLayout container, List<String> nameList) {
        if (container.getVisibility() != View.VISIBLE) return;
        Logger.d("Adding HW name to list...");
        for (String name : nameList) {
            TextView textView = new TextView(this);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            textView.setTextColor(Color.WHITE);
            textView.setText(name);
            textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_a400_module_indicator, null), null, null, null);
            textView.setCompoundDrawablePadding((int) DimensUtils.dp2px(12));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = 15;
            container.addView(textView, lp);
        }
    }

    @OnClick(R2.id.btn_take_action)
    void onTakeActionClicked() {
        playNormalClickSound();
        switch (mEvent) {
            case EVENT_STARTUP:
                mRouter.backHome().start(this);
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setShowUpdateResultWhenHome(false);
                break;
            case EVENT_EM_BIN:
                // Reboot screen to ensure relaunching.
                Logger.d("Update modules success, rebooting..");
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setShowUpdateResultWhenHome(true);
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                powerManager.reboot(null);
                break;
            case EVENT_BIG_BIN:
                String apkPath = getIntent().getStringExtra("apkPath");

                if (TextUtils.isEmpty(apkPath)) {
                    Logger.d("APK path accidentally being empty.");
                    return;
                }

                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setShowUpdateResultWhenHome(true);

                // Send broadcast to update FabScreen itself.
                Intent updateIntent = new Intent(this, InstallProcessReceiver.class);
                updateIntent.putExtra("URL", apkPath);
                updateIntent.putExtra("OPERATION", "local_file");
                updateIntent.putExtra("PACKAGE_NAME", getPackageName());
                sendBroadcast(updateIntent);
                break;
        }
    }
}
