package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed;

import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedCalibrationInfoFragment.A400_LEVELING_BED_CALIBRATION_AUTO;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedCalibrationInfoFragment.A400_LEVELING_BED_CALIBRATION_MANUAL;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.ui.common.VerticalSpaceItemDecoration;
import fabscreen.platform.core.ui.common.selection.CalibrationModeSelectionAdapter;
import fabscreen.platform.core.ui.common.selection.CalibrationModeSelectionItem;
import fabscreen.platform.core.ui.view.PullDownMenu;

public class A400CalibrationLevelingBedSelectionFragment extends BaseFragment {
    @BindView(R2.id.rv_sections)
    RecyclerView mRvSections;
    @BindView(R2.id.li_calibration_right_select)
    LinearLayout mlySections;
    @BindView(R2.id.rl_calibration_leveling_bed_selection_temperature)
    RelativeLayout mRlTemperature;
    @BindView(R2.id.et_calibration_leveling_bed_selection_temperature)
    EditText mEtBedTemperature;
    @BindView(R2.id.tv_a400_calibration_leveling_bed_selection_title)
    TextView mTvLevelingBedSelectionTitle;
    @BindView(R2.id.tv_select_type)
    TextView mTvSelectType;
    @BindView(R2.id.rl_alibration_leveling_bed)
    RelativeLayout mRlAlibrationLevelingBed;


    protected final List<CalibrationModeSelectionItem> mSectionItems = new ArrayList<>();
    protected List<CalibrationModeSelectionItem> items = new ArrayList<>();
    protected IPreferences.Helper helper;

    private int mGrid;
    private int mTemperature;
    private int mHeadType;
    private MenuAdapter mMenuAdapter;

    public static Fragment newInstance() {
        return new A400CalibrationLevelingBedSelectionFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
        mHeadType = getServiceContainer().getService(IMachine.class).getFDMController().getHeadType();
        initView();
    }

    private void initView() {
        if (mSectionItems.size() > 0) {
            mSectionItems.clear();
            items.clear();
        }
        mGrid = gridGeoIndex(helper.getA400LevelingBedCalibrationGrid());
        ArrayList<String> menuItems = new ArrayList<>();
        menuItems.add(getString(R.string.a400_control_there_matrix_title));
        menuItems.add(getString(R.string.a400_control_five_matrix_title));
        menuItems.add(getString(R.string.a400_control_nine_matrix_title));
        mMenuAdapter = new MenuAdapter(getContext(), menuItems);
        mTvSelectType.setText(menuItems.get(mGrid));
        mMenuAdapter.setOnItemClickListener((view, position) -> {
            playNormalClickSound();
            mGrid = position;
            mTvSelectType.setText(menuItems.get(position));
            PullDownMenu.dismiss();
        });
        mlySections.setVisibility(View.VISIBLE);
        mSectionItems.addAll(getSections());
        mRvSections.setLayoutManager(new LinearLayoutManager(requireContext()));

        CalibrationModeSelectionAdapter adapter = new CalibrationModeSelectionAdapter(mSectionItems);
        mRvSections.setAdapter(adapter);

        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration(DimensUtils.dp2px(12));
        mRvSections.addItemDecoration(decoration);

        adapter.setOnSectionSelectedListener(this::onSectionSelected);

        if (mHeadType == Module.ModuleType.HEAD_3DP) {
            adapter.setSelection(0);
            onSectionSelected(0);
        } else {
            adapter.setSelection(helper.getA400LevelingBedCalibrationMode());
            onSectionSelected(helper.getA400LevelingBedCalibrationMode());
        }
    }

    private int gridGeoIndex(int a400LevelingBedCalibrationGrid) {
        switch (a400LevelingBedCalibrationGrid) {
            case 3:
                return 0;
            case 9:
                return 2;
            default:
                return 1;
        }

    }

    private void onSectionSelected(int position) {
        mTvLevelingBedSelectionTitle.setText(position == 0 ? R.string.calibration_auto_mode : R.string.calibration_manual_mode);
        helper.setA400LevelingBedCalibrationMode(position);
        if (position == 0) {
            if (mHeadType == Module.ModuleType.HEAD_3DP) {
                mRlTemperature.setVisibility(View.INVISIBLE);
                helper.setA400LevelingBedCalibrationMode(A400_LEVELING_BED_CALIBRATION_MANUAL);
            } else {
                mRlTemperature.setVisibility(View.VISIBLE);
                mEtBedTemperature.setText(String.valueOf(helper.getA400LevelingBedCalibrationBedTemperature()));
            }
        } else {
            mRlTemperature.setVisibility(View.INVISIBLE);
        }

    }

    private List<CalibrationModeSelectionItem> getSections() {
        if (mHeadType == Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER) {
            items.add(new CalibrationModeSelectionItem(R.string.calibration_auto_mode, R.string.calibration_heated_bed_leveing_auto_content));
        }
        items.add(new CalibrationModeSelectionItem(R.string.calibration_manual_mode, R.string.calibration_heated_bed_leveing_manual_content));
        return items;
    }

    @OnTextChanged(value = R2.id.et_calibration_leveling_bed_selection_temperature, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTemperatureChange(CharSequence sequence) {
        try {
            mTemperature = Integer.parseInt(sequence.toString());
            if (mTemperature > 80) {
                mTemperature = 80;
                mEtBedTemperature.setText(mTemperature + "");
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_leveling_bed_selection;
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

    @OnClick(R2.id.rl_alibration_leveling_bed)
    public void onClickCalibrationPoints() {
        playNormalClickSound();
        PullDownMenu.create(getContext(), mMenuAdapter)
                .showBelowView(mRlAlibrationLevelingBed, 0, 10);
        if (mMenuAdapter != null) {
            mMenuAdapter.setSelectPosition(mGrid);
        }
    }

    private void onDetermine() {
        int a400LevelingBedCalibrationMode = helper.getA400LevelingBedCalibrationMode();
        if (a400LevelingBedCalibrationMode == A400_LEVELING_BED_CALIBRATION_AUTO) {
            helper.setA400LevelingBedCalibrationBedTemperature(mTemperature);
        }
        helper.setA400LevelingBedCalibrationGrid(IndexToGrid(mGrid));
    }

    private int IndexToGrid(int grid) {
        switch (grid) {
            case 0:
                return 3;
            case 1:
                return 5;
            case 2:
                return 9;
            default:
                return 5;
        }
    }

    private void onExit() {
        if (getActivity() != null)
            getActivity().finish();
    }
}
