package fabscreen.features.settings.a350.advanced._3dp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class SettingsAdvanced3DPFragment extends BaseFragment {
    private static final int ITEM_CALIBRATION_GRID = 1;
    @BindView(R2.id.btn_settings_advanced_auto_calibration)
    Button mBtnAutoCalibrationSwitch;
    @BindView(R2.id.btn_settings_advanced_fast_calibration)
    Button mBtnFastCalibrationSwitch;
    @BindView(R2.id.btn_settings_advanced_heated_leveling)
    Button mBtnHeatedLevelingSwitch;
    @BindView(R2.id.view_settings_advanced_fast_calibration_background)
    Button mBtnFastCalibrationBackground;
    @BindView(R2.id.tv_settings_advance_fast_calibration_desc)
    TextView mTvFastCalibrationDesc;
    @BindView(R2.id.lv_advanced_3dp_item_list)
    ListView mLv3dpAdvanceList;
    private BehaviorSubject<Integer> mCalibrationModeSubject = BehaviorSubject.createDefault(0);

    public static SettingsAdvanced3DPFragment getInstance() {
        return new SettingsAdvanced3DPFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_advanced_3dp;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.settings_print_settings_3dp);

        // show fast calibration option if auto calibration is on
        mCalibrationModeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(mode -> {
//                    mBtnFastCalibrationSwitch.setVisibility(mode == 0 ? Button.VISIBLE : Button.GONE);
//                    mTvFastCalibrationDesc.setVisibility(mode == 0 ? TextView.VISIBLE : TextView.GONE);
//                    mBtnFastCalibrationBackground.setVisibility(mode == 0 ? TextView.VISIBLE : TextView.GONE);
                    mBtnAutoCalibrationSwitch.setActivated(mode == 0);
                });

        int calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationMode();
        mCalibrationModeSubject.onNext(calibrationMode);

        boolean fastCalibrationOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPFastCalibrationOn();
        mBtnFastCalibrationSwitch.setActivated(fastCalibrationOn);

        boolean isHeatedLeveling = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationHeatedLevelingOn();
        mBtnHeatedLevelingSwitch.setActivated(isHeatedLeveling);

        // Advanced list
        List<Integer> items = new ArrayList<Integer>() {{
            if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId != Constants.MACHINE_MODEL_SNAPMAKER_A150) {
                add(ITEM_CALIBRATION_GRID);
            }
        }};

        ItemAdapter itemAdapter = new ItemAdapter(getContext());
        itemAdapter.setItem(items);

        mLv3dpAdvanceList.setAdapter(itemAdapter);

    }

    @OnClick(R2.id.btn_settings_advanced_auto_calibration)
    void onClickCalibrationMode() {
        playNormalClickSound();
        mCalibrationModeSubject.onNext(1 - mCalibrationModeSubject.getValue());
        Logger.i("Setting 3DP calibration mode " + mCalibrationModeSubject.getValue());

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().set3DPCalibrationMode(mCalibrationModeSubject.getValue());
    }

    @OnClick(R2.id.btn_settings_advanced_fast_calibration)
    void onClickFastCalibration() {
        playNormalClickSound();
        boolean fastCalibrationOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPFastCalibrationOn();
        fastCalibrationOn = !fastCalibrationOn;

        Logger.i("Setting fast calibration " + fastCalibrationOn);

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().set3DPFastCalibrationOn(fastCalibrationOn);
        mBtnFastCalibrationSwitch.setActivated(fastCalibrationOn);
    }

    @OnClick(R2.id.btn_settings_advanced_heated_leveling)
    void onClickHeatedLeveling() {
        playNormalClickSound();
        boolean isHeatedLevelingOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationHeatedLevelingOn();
        isHeatedLevelingOn = !isHeatedLevelingOn;
        Logger.i("Setting heated leveling " + isHeatedLevelingOn);

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().set3DPCalibrationHeatedLevelingOn(isHeatedLevelingOn);
        mBtnHeatedLevelingSwitch.setActivated(isHeatedLevelingOn);
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

            switch (item) {
                case ITEM_CALIBRATION_GRID: {
                    button.setText(R.string.settings_3dp_calibration_grid);
                    button.setOnClickListener(v -> {
                        if (getActivity() != null) {
                            ((SettingsActivity) getActivity()).goto3DPCalibrationGrid();
                        }
                    });
                    break;
                }
            }

            return convertView;
        }
    }
}
