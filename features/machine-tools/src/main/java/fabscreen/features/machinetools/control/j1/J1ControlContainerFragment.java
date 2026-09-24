package fabscreen.features.machinetools.control.j1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.ControlContainerViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.core.ui.common.leftsection.J1LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;
import fabscreen.platform.core.ui.view.HelpDialog;
import fabscreen.platform.lib.LogHelper;

public class J1ControlContainerFragment extends SectionAndDetailContainerFragment {

    @BindView(R2.id.btn_top_bar_help)
    Button mBtnHelp;
    @BindView(R2.id.tv_top_bar_help)
    TextView mTvHelp;
    private ControlContainerViewModel mViewModel;

    public static Fragment newInstance() {
        return new J1ControlContainerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }


    @Override
    protected void onSectionSelected(int position, boolean isUserClick) {
        super.onSectionSelected(position, isUserClick);
        if (mBtnHelp != null) {
            mBtnHelp.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            mTvHelp.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_section_and_detail_container;
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        return mViewModel.getLeftSections();
    }

    @Override
    protected LeftSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new J1LeftSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return "Control";
    }

    @Override
    public ControlContainerViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(ControlContainerViewModel.class);
    }

    void coolDownToolHead() {
        FDMController fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        fdmController.setExtruderTemperature(0, 0, 0)
                .flatMap(success -> fdmController.setExtruderTemperature(1, 0, 0))
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    int result = responseStructure.resultProp.getValue();
                    if (result != 0) {
                        // TODO
                    }
                }, LogHelper::log);
    }

    void coolDownHeatedBed() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController()
                .getHeatedBed()
                .setZoneTargetTemperature(0, 0)
                .as(bindToLifecycle())
                .subscribe(result -> {
                    if (result.isSuccess()) {
                        Logger.d("cool down heated bed.");
                    }
                }, LogHelper::log);
    }

    @OnClick({R2.id.btn_top_bar_help, R2.id.tv_top_bar_help})
    public void onClickHelp() {
        HelpDialog.create(requireContext(), mViewModel.getHelpList()).show();
    }

    @Override
    protected void back() {
        coolDownHeatedBed();
        coolDownToolHead();
        super.back();
    }
}
