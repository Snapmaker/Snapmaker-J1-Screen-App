package fabscreen.features.addons.enclosure;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.addons.R;
import fabscreen.features.addons.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class EnclosureSettingsFragment extends BaseFragment {
    @BindView(R2.id.view_enclosure_settings_door_detection)
    View mViewDoorDetection;
    @BindView(R2.id.btn_enclosure_settings_door_detection)
    Button mBtnDoorDetection;
    @BindView(R2.id.btn_enclosure_settings_auto_lighting)
    Button mBtnAutoLighting;
    EnclosureViewModel mViewModel;

    public static EnclosureSettingsFragment getInstance() {
        return new EnclosureSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_enclosure);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_enclosure_settings;
    }

    @Override
    protected EnclosureViewModel getViewModel() {
        return getViewModelProvider().get(EnclosureViewModel.class);
    }

    private void initView() {
        // show up door detection settings only in laser or cnc module
        final int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        mViewDoorDetection.setVisibility(headType == Constants.FILE_TYPE_3DP ? Button.GONE : Button.VISIBLE);

        mViewModel.getEnclosureStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mBtnDoorDetection.setActivated(status.isEnclosureEnabled());
                });

        boolean isAutoLighting = mViewModel.isEnclosureAutoLighting();
        mBtnAutoLighting.setActivated(isAutoLighting);
    }

    private void showAutoLightingSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(300 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_auto_lighting, null);
        dialog.setView(view);
        dialog.show();

        // Auto Dismiss after 3 seconds
        AndroidSchedulers.mainThread().scheduleDirect(dialog::dismiss, 3000, TimeUnit.MILLISECONDS);
    }

    @OnClick(R2.id.btn_enclosure_settings_door_detection)
    void onClickDoorDetection() {
        playNormalClickSound();
        boolean isEnabled = mViewModel.isDoorDetectionEnabled();

        mViewModel.setDoorDetection(!isEnabled);
    }

    @OnClick(R2.id.btn_enclosure_settings_auto_lighting)
    void onClickAutoLighting() {
        playNormalClickSound();
        boolean autoLighting = mViewModel.isEnclosureAutoLighting();

        autoLighting = !autoLighting;
        Logger.d("Set Enclosure Auto Lighting " + autoLighting);

        mViewModel.setEnclosureAutoLighting(autoLighting);
        mBtnAutoLighting.setActivated(autoLighting);

        showAutoLightingSettingsDialog();
    }
}
