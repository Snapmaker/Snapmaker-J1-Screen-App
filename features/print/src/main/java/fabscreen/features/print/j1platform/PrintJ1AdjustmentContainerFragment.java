package fabscreen.features.print.j1platform;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.ui.common.leftsection.J1LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;
import fabscreen.platform.core.ui.view.HelpBean;
import fabscreen.platform.core.ui.view.HelpDialog;

import static fabscreen.platform.base.lib.print.IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP;
import static fabscreen.platform.base.lib.print.IPrintWorkspace.PRINT_MODE_NORMAL;

public class PrintJ1AdjustmentContainerFragment extends SectionAndDetailContainerFragment {
    @BindView(R2.id.btn_top_bar_help)
    Button mBtnHelp;
    @BindView(R2.id.tv_top_bar_help)
    TextView mTvHelp;

    public static Fragment newInstance() {
        return new PrintJ1AdjustmentContainerFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_section_and_detail_container;
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        List<SectionItem> items = new ArrayList<>();
        items.add(new SectionItem(getString(R.string.j1_print_adjust_extruder), PrintJ1AdjustmentExtruderFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.j1_print_adjust_heated_bed), PrintJ1AdjustmentHeatedBedFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.j1_print_adjust_z_offset), PrintJ1AdjustmentZOffsetFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.j1_print_adjust_work_speed), PrintJ1AdjustmentWorkSpeedFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.j1_print_adjust_flow_rate), PrintJ1AdjustmentFlowRateFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.j1_print_adjust_fan_speed), PrintJ1AdjustmentFanSpeedFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.j1_print_adjust_performance), PrintJ1AdjustmentPrintPerformanceFragment.newInstance()));
        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            items.add(new SectionItem(getString(R.string.j1_print_adjust_lighting), PrintJ1AdjustmentLightingFragment.newInstance()));
        }
        int printModeStatusValue = ServiceContainer.getInstance().getService(IMachine.class)
                .getNewPrintController()
                .getPrintModeStatusValue();
        if (printModeStatusValue == -1) {
            printModeStatusValue = ServiceContainer.getInstance().getService(IPrintWorkspace.class).getPrintMode();
        }
        boolean applyMultiExtruder = ServiceContainer.getInstance().getService(IPrintWorkspace.class).isApplyMultiExtruder();
        if ((printModeStatusValue == PRINT_MODE_NORMAL && !applyMultiExtruder)
                ||
                printModeStatusValue == PRINT_MODE_DUAL_EXTRUDER_BACK_UP) {
            items.add(new SectionItem(getString(R.string.j1_print_adjust_backup_mode), PrintJ1AdjustmentBackUpModeFragment.newInstance()));
        }
        return items;
    }

    @Override
    protected LeftSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new J1LeftSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return null;
    }

    @Override
    protected void onSectionSelected(int position, boolean isUserClick) {
        super.onSectionSelected(position, isUserClick);
        mBtnHelp.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        mTvHelp.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
    }

    @OnClick({R2.id.btn_top_bar_help, R2.id.tv_top_bar_help})
    public void onClickHelp() {
        playSwitchSound();
        HelpDialog.create(requireContext(), getHelpList()).show();
    }

    public List<HelpBean> getHelpList() {
        List<HelpBean> list = new ArrayList<>();
        list.add(new HelpBean(R.drawable.gif_help_content_1, R.string.j1_how_to_load_filament_step_1));
        list.add(new HelpBean(R.drawable.gif_help_content_2, R.string.j1_how_to_load_filament_step_2));
        list.add(new HelpBean(R.drawable.gif_help_content_3, R.string.j1_how_to_load_filament_step_3));
        list.add(new HelpBean(R.drawable.pic_help_content_4, R.string.j1_how_to_load_filament_step_4));
        list.add(new HelpBean(R.drawable.gif_help_content_5, R.string.j1_how_to_load_filament_step_5));
        list.add(new HelpBean(R.drawable.pic_help_content_6, R.string.j1_how_to_load_filament_step_6));
        list.add(new HelpBean(R.drawable.gif_help_content_7, R.string.j1_how_to_load_filament_step_7));
        return list;
    }

}
