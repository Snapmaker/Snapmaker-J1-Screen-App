package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_ABS;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_CUSTOM;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_PETG;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_PLA;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.VerticalSpaceItemDecoration;
import fabscreen.platform.core.ui.common.selection.CalibrationModeSelectionAdapter;
import fabscreen.platform.core.ui.common.selection.CalibrationModeSelectionItem;

public class A400CalibrationLevelingXYSelectionFragment extends BaseFragment {
    protected final List<CalibrationModeSelectionItem> mSectionItems = new ArrayList<>();
    protected List<CalibrationModeSelectionItem> items = new ArrayList<>();
    protected IPreferences.Helper helper;
    @BindView(R2.id.rv_sections)
    RecyclerView mRvSections;
    @BindView(R2.id.li_calibration_right_select)
    LinearLayout mlySections;

    @BindView(R2.id.tv_a400_calibration_leveling_election_title)
    TextView mTvXYCalibrationSelectionTitle;

    @BindView(R2.id.rl_calibration_leveling_xy_selection_left_printing_temperature)
    RelativeLayout mRlLeftPrintingTemperature;
    @BindView(R2.id.et_calibration_leveling_xy_selection_left_printing_temperature_title)
    EditText mEdLeftPrintingTemperature;
    int mLeftPrintingTemperature;
    @BindView(R2.id.rl_calibration_leveling_xy_selection_right_printing_temperature)
    RelativeLayout mRlRightPrintingTemperature;
    @BindView(R2.id.et_calibration_leveling_xy_selection_right_printing_temperature)
    EditText mEdRightPrintingTemperature;
    int mRightPrintingTemperature;
    @BindView(R2.id.rl_calibration_leveling_xy_selection_left_standby_temperature)
    RelativeLayout mRlLeftStandbyTemperature;
    @BindView(R2.id.et_calibration_leveling_xy_selection_left_standby_temperature)
    EditText mEdLeftStandbyTemperature;
    int mLeftStandbyTemperature;
    @BindView(R2.id.rl_calibration_leveling_xy_selection_right_standby_temperature)
    RelativeLayout mRlRightStandbyTemperature;
    @BindView(R2.id.et_calibration_leveling_xy_selection_right_standby_temperature_)
    EditText mEdRightStandbyTemperature;
    int mRightStandbyTemperature;
    @BindView(R2.id.rl_calibration_leveling_xy_selection_bed_printing_temperature)
    RelativeLayout mRlBedPrintingTemperature;
    @BindView(R2.id.et_calibration_leveling_xy_selection_bed_printing_temperature)
    EditText mEdBedPrintingTemperature;
    int mBedPrintingTemperature;

    public static Fragment newInstance() {
        return new A400CalibrationLevelingXYSelectionFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
        initView();
    }

    private void initView() {
        if (mSectionItems.size() > 0) {
            mSectionItems.clear();
            items.clear();
        }
        mlySections.setVisibility(View.VISIBLE);
        mSectionItems.addAll(getSections());
        mRvSections.setLayoutManager(new LinearLayoutManager(requireContext()));
        CalibrationModeSelectionAdapter adapter = new CalibrationModeSelectionAdapter(mSectionItems);
        mRvSections.setAdapter(adapter);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration(DimensUtils.dp2px(12));
        mRvSections.addItemDecoration(decoration);
        adapter.setSelection(helper.getA400BevelingXYMaterialSelection());
        onSectionSelected(helper.getA400BevelingXYMaterialSelection());
        adapter.setOnSectionSelectedListener(this::onSectionSelected);
    }


    private void onSectionSelected(int position) {
        //save Perent
        helper.setA400BevelingXYMaterialSelection(position);
        switch (position) {
            case A400_LEVELING_XY_CALIBRATION_PLA:
                mTvXYCalibrationSelectionTitle.setText(R.string.all_material_PLA);
                updateView(false, 210, 210, 150, 150, 60);
                break;
            case A400_LEVELING_XY_CALIBRATION_PETG:
                mTvXYCalibrationSelectionTitle.setText(R.string.all_material_PETG);
                updateView(false, 230, 230, 150, 150, 80);
                break;
            case A400_LEVELING_XY_CALIBRATION_ABS:
                mTvXYCalibrationSelectionTitle.setText(R.string.all_material_ABS);
                updateView(false, 235, 235, 150, 150, 80);
                break;
            case A400_LEVELING_XY_CALIBRATION_CUSTOM:
                mTvXYCalibrationSelectionTitle.setText(R.string.calibration_a400_xy_mode_custom);
                updateView(true,
                        helper.getA400LevelingXYCalibrationLeftPrintingTemperature(),
                        helper.getA400LevelingXYCalibrationRightPrintingTemperature(),
                        helper.getA400LevelingXYCalibrationLeftStandbyTemperature(),
                        helper.getA400LevelingXYCalibrationRightStandbyTemperature(),
                        helper.getA400LevelingXYCalibrationBedPrintingTemperature());
                break;
            default:
                break;
        }

    }

    private void updateView(boolean enable, int leftPrintingTemperature, int rightPrintingTemperature, int leftStandbyTemperature, int rightStandbyTemperature, int bedPrintingTemperature) {
        mEdLeftPrintingTemperature.setEnabled(enable);
        mEdRightPrintingTemperature.setEnabled(enable);
        mEdLeftStandbyTemperature.setEnabled(enable);
        mEdRightStandbyTemperature.setEnabled(enable);
        mEdBedPrintingTemperature.setEnabled(enable);

        mEdLeftPrintingTemperature.setText(leftPrintingTemperature + "");
        mEdRightPrintingTemperature.setText(rightPrintingTemperature + "");
        mEdLeftStandbyTemperature.setText(leftStandbyTemperature + "");
        mEdRightStandbyTemperature.setText(rightStandbyTemperature + "");
        mEdBedPrintingTemperature.setText(bedPrintingTemperature + "");
    }

    private List<CalibrationModeSelectionItem> getSections() {
        items.add(new CalibrationModeSelectionItem(R.string.all_material_PLA, 0));
        items.add(new CalibrationModeSelectionItem(R.string.all_material_PETG, 0));
        items.add(new CalibrationModeSelectionItem(R.string.all_material_ABS, 0));
        items.add(new CalibrationModeSelectionItem(R.string.all_material_CUSTOM, 0));
        return items;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_leveling_xy_selection;
    }

    @OnClick(R2.id.tv_air_purifier_error_power_off_calibration_right_select_exit)
    public void onClickExit() {
        playNormalClickSound();
        onExit();
    }

    @OnClick(R2.id.bt_calibration_right_select_cancel)
    public void onClickCancel() {
        playNormalClickSound();
        onExit();
    }

    @OnClick(R2.id.bt_calibration_right_select_determine)
    public void onClickDetermine() {
        playNormalClickSound();
        onDetermine();

        onExit();
    }

    @OnTextChanged(value = R2.id.et_calibration_leveling_xy_selection_bed_printing_temperature, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTemperatureChange(CharSequence sequence) {
        try {
            int mTemperature = Integer.parseInt(sequence.toString());
            if (mTemperature > 80) {
                mTemperature = 80;
                mEdBedPrintingTemperature.setText(mTemperature + "");
            }
        } catch (Exception ignored) {

        }
    }

    private void onDetermine() {
        int a400LevelingBedCalibrationMode = helper.getA400BevelingXYMaterialSelection();
        Logger.d("---FDT--- onDetermine" + a400LevelingBedCalibrationMode);
        if (a400LevelingBedCalibrationMode == A400_LEVELING_XY_CALIBRATION_CUSTOM) {
            helper.setA400LevelingXYCalibrationLeftPrintingTemperature(Integer.parseInt(mEdLeftPrintingTemperature.getText().toString()));
            helper.setA400LevelingXYCalibrationRightPrintingTemperature(Integer.parseInt(mEdRightPrintingTemperature.getText().toString()));
//            helper.setA400LevelingXYCalibrationLeftStandbyTemperature(Integer.parseInt(mEdLeftStandbyTemperature.getText().toString()));
//            helper.setA400LevelingXYCalibrationRightStandbyTemperature(Integer.parseInt(mEdRightStandbyTemperature.getText().toString()));
            helper.setA400LevelingXYCalibrationBedPrintingTemperature(Integer.parseInt(mEdBedPrintingTemperature.getText().toString()));
        }
    }

    private void onExit() {
        if (getActivity() != null)
            getActivity().finish();
    }
}
