package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.VerticalSpaceItemDecoration;
import fabscreen.platform.core.ui.common.selection.CalibrationModeSelectionAdapter;
import fabscreen.platform.core.ui.common.selection.CalibrationModeSelectionItem;

public class A400LaserSetZModeSelectFragment extends BaseFragment {
    protected final List<CalibrationModeSelectionItem> mSectionItems = new ArrayList<>();
    protected List<CalibrationModeSelectionItem> items = new ArrayList<>();
    protected IPreferences.Helper helper;
    @BindView(R2.id.rv_sections)
    RecyclerView mRvSections;
    @BindView(R2.id.li_calibration_right_select)
    LinearLayout mlySections;

    private MachineInfo mMachineInfo;

    public static Fragment newInstance() {
        return new A400LaserSetZModeSelectFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();

        initView();
    }

    private void initView() {
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineInfo = machine.getMachineInfoSubjectHolder().getValue();
        if (mSectionItems.size() > 0) {
            mSectionItems.clear();
            items.clear();
        }
        mlySections.setVisibility(View.GONE);
        mSectionItems.addAll(getSections());
        mRvSections.setLayoutManager(new LinearLayoutManager(requireContext()));
        CalibrationModeSelectionAdapter adapter = new CalibrationModeSelectionAdapter(mSectionItems);
        mRvSections.setAdapter(adapter);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration(DimensUtils.dp2px(12));
        mRvSections.addItemDecoration(decoration);
        adapter.setOnSectionSelectedListener(this::onSectionSelected);
        adapter.setSelection(helper.getLaserPrintZOriginModel());
        onSectionSelected(helper.getLaserPrintZOriginModel());
    }


    private void onSectionSelected(int position) {
        //save Perent
        helper.setLaserPrintZOriginMode(position);
    }

    private List<CalibrationModeSelectionItem> getSections() {
        if (mMachineInfo.isRotaryAvailable) {
            items.add(new CalibrationModeSelectionItem(R.string.a400_laser_print_four_axis_input_diameter_title, R.string.a400_laser_print_four_axis_input_diameter_contennt));
            items.add(new CalibrationModeSelectionItem(R.string.a400_laser_print_four_axis_touchmaterial_title, R.string.a400_laser_print_four_axis_touchmaterial_contnent));
            items.add(new CalibrationModeSelectionItem(R.string.a400_laser_print_four_axis_manual_focus_title, R.string.a400_laser_print_four_axis_manual_focus_content));
        } else {
            items.add(new CalibrationModeSelectionItem(R.string.automatic_thickness_measurement_title, R.string.automatic_thickness_measurement_content));
            items.add(new CalibrationModeSelectionItem(R.string.enter_material_thickness_title, R.string.enter_material_thickness_content));
            items.add(new CalibrationModeSelectionItem(R.string.bonding_material_surface_title, R.string.bonding_material_surface_content));
            items.add(new CalibrationModeSelectionItem(R.string.manual_focus_title, R.string.manual_focus_content));
        }
        return items;
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_laser_setz_selection;
    }

    @OnClick(R2.id.tv_air_purifier_error_power_off_calibration_right_select_exit)
    public void onClickExit() {
        playNormalClickSound();
        onExit();
    }

    private void onExit() {
        if (getActivity() != null)
            getActivity().finish();
    }
}
