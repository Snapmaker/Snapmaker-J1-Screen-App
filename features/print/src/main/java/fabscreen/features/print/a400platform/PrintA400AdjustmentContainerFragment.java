package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.core.ui.common.leftsection.A400RightSectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.A400RightSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;

public class PrintA400AdjustmentContainerFragment extends A400RightSectionAndDetailContainerFragment {

    @BindView(R2.id.tv_print_setting_name)
    TextView mTvName;

    public static Fragment newInstance() {
        return new PrintA400AdjustmentContainerFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_right_section_and_detail_container;
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        List<SectionItem> items = new ArrayList<>();
        MachineInfo value = getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
        switch (value.workType) {
            case FDM:
//                items.add(new SectionItem("Extruder", R.drawable.select_a400_print_setting_right_z_move, PrintJ1AdjustmentExtruderFragment.newInstance()));
//                items.add(new SectionItem("Heated Bed", R.drawable.select_a400_print_setting_right_z_move, PrintA400AdjustmentHeatedBedFragment.newInstance()));
                items.add(new SectionItem(getString(R.string.a400_print_print_setting_left_z_offset), R.drawable.select_a400_print_setting_right_z_move, PrintA400AdjustmentLeftZOffsetFragment.newInstance()));
                items.add(new SectionItem(getString(R.string.a400_print_print_setting_right_z_offset), R.drawable.select_a400_print_setting_right_z_move, PrintA400AdjustmentRightZOffsetFragment.newInstance()));
                items.add(new SectionItem(getString(R.string.a400_print_print_setting_part_cooling_fan), R.drawable.select_a400_print_setting_fan, PrintA400AdjustmentFanSpeedFragment.newInstance()));
                items.add(new SectionItem(getString(R.string.a400_print_print_setting_left_flow_rate), R.drawable.select_a400_print_setting_left_extruder, PrintA400AdjustmentFlowRateFragment.newInstance(PrintA400AdjustmentFlowRateFragment.LEFT_NOZZLE_TYPE)));
                items.add(new SectionItem(getString(R.string.a400_print_print_setting_right_flow_rate), R.drawable.select_a400_print_setting_right_extruder, PrintA400AdjustmentFlowRateFragment.newInstance(PrintA400AdjustmentFlowRateFragment.RIGHT_NOZZLE_TYPE)));

                break;
            case LASER:
                break;
            case CNC:
                break;
            case NONE:
                break;
        }
        items.add(new SectionItem(getString(R.string.a400_print_print_setting_part_work_speed), R.drawable.selelct_a400_print_setting_work_pacing, PrintA400AdjustmentWorkSpeedFragment.newInstance()));

        if (value.isEnclosureAvailable) {
            items.add(new SectionItem("Enclosure", R.drawable.select_a400_print_setting_enclosure, PrintA400AdjustmentEnclosureControlFragment.newInstance()));
        }
        if (value.isAirPurifierAvailable) {
            items.add(new SectionItem("Air Purifier", R.drawable.select_a400_print_setting_air_purifier, PrintA400AdjustmentAirPurifierFragment.newInstance()));
        }
        return items;
    }

    @Override
    protected A400RightSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new A400RightSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return null;
    }

    @OnClick(R2.id.btn_close)
    public void onClickBack() {
        back();
    }

    public void setModelTitle(String name) {
        if (!TextUtils.isEmpty(name)) {
            mTvName.setText(name);
        }
    }

}
