package fabscreen.features.machinetools.calibration.j1Platform;

import java.util.ArrayList;
import java.util.List;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.HelpBean;

public class J1FilamentFullWidthViewModel extends BaseViewModel {

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

    @Override
    protected void onCleared() {
        getServiceContainer().getService(IMachine.class).getMachineController().shutdownWorkingParts();
        super.onCleared();
    }
}
