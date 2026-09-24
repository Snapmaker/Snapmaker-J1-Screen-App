package fabscreen.features.settings.a350.advanced.laser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a350.index.SettingsActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;

public class SettingsAdvancedLaserFragment extends BaseFragment {
    private static final int ITEM_MODIFY_FOCUS = 1;
    private static final int ITEM_CAMERA_CALIBRATION = 2;
    @BindView(R2.id.btn_settings_advanced_calibration)
    Button mBtnCalibrationSwitch;
    @BindView(R2.id.btn_settings_advanced_light)
    Button mBtnLight;
    @BindView(R2.id.lv_advanced_laser_item_list)
    ListView mLvLaserAdvancedList;
    private boolean mIsRotaryAvailable;

    public static SettingsAdvancedLaserFragment getInstance() {
        return new SettingsAdvancedLaserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsRotaryAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_advanced_laser;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.settings_advance_laser);
        int calibrationMode = getCalibrationMode();
        mBtnCalibrationSwitch.setActivated(calibrationMode == 0);

        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        mBtnLight.setActivated(cameraLightOn);

        // Advanced list
        List<Integer> items = new ArrayList<Integer>() {{
            add(ITEM_MODIFY_FOCUS);
            // Camera Calibration is not available with rotary module for now.
            if (!mIsRotaryAvailable) {
                add(ITEM_CAMERA_CALIBRATION);
            }
        }};

        ItemAdapter itemAdapter = new ItemAdapter(getContext());
        itemAdapter.setItem(items);

        mLvLaserAdvancedList.setAdapter(itemAdapter);
    }

    @OnClick(R2.id.btn_settings_advanced_light)
    void onClickLight() {
        playNormalClickSound();
        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        cameraLightOn = !cameraLightOn;

        Logger.i("Setting camera light mode " + cameraLightOn);

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserCameraLightOn(cameraLightOn);
        mBtnLight.setActivated(cameraLightOn);
    }

    @OnClick(R2.id.btn_settings_advanced_calibration)
    void onClickCalibrationMode() {
        playNormalClickSound();
        int calibrationMode = getCalibrationMode();

        calibrationMode = 1 - calibrationMode;

        Logger.i("Setting Laser calibration mode " + calibrationMode);

        setCalibrationMode(calibrationMode);
        mBtnCalibrationSwitch.setActivated(calibrationMode == 0);
    }

    private int getCalibrationMode() {
        if (mIsRotaryAvailable) {
            return ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaser4AxisCalibrationMode();
        } else {
            return ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCalibrationMode();
        }
    }

    private void setCalibrationMode(int calibrationMode) {
        if (mIsRotaryAvailable) {
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaser4AxisCalibrationMode(calibrationMode);
        } else {
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserCalibrationMode(calibrationMode);
        }
    }

    private class ItemAdapter extends BaseAdapter {
        private Context mContext;
        private List<Integer> mItems;

        ItemAdapter(Context context) {
            this.mContext = context;
        }

        public void setItem(List<Integer> items) {
            this.mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Integer getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_settings_item, parent, false);
            }

            int item = getItem(position);

            Button button = convertView.findViewById(R.id.btn_settings_item_name);
            TextView tvDesc = convertView.findViewById(R.id.tv_settings_item_desc);

            // TODO
            tvDesc.setText(null);

            // icon
            ImageView ivIcon = convertView.findViewById(R.id.iv_settings_item_icon);

            switch (item) {
                case ITEM_MODIFY_FOCUS: {
                    ivIcon.setImageResource(R.drawable.ic_settings_laser_focus_20x20);
                    button.setText(R.string.laser_calibration_modify);
                    button.setOnClickListener(v -> {
                        if (getActivity() != null) {
                            ((SettingsActivity) getActivity()).startLaserFocusModificationFragment();
                        }
                    });
                    break;
                }
                case ITEM_CAMERA_CALIBRATION: {
                    ivIcon.setImageResource(R.drawable.ic_settings_camera_calibration_20x20);
                    button.setText(R.string.settings_camera_calibration);
                    button.setOnClickListener(v -> {
                        // open camera calibration
                        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().isConnected()) {
                            SettingsActivity activity = (SettingsActivity) getContext();
                            if (activity != null) {
                                activity.gotoCameraCalibrationStep1();
                            }
                        } else {
                            FabAlert.alert(getContext(), R.string.laser_camera_alert_camera_not_connected);
                        }
                    });
                    break;
                }
                default:
                    break;
            }

            return convertView;
        }
    }
}
