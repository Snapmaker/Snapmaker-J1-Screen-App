package fabscreen.features.machinetools.control.j1;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseViewModel;

import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.AIR_PURIFIER;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.CNC;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.DRIER;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.ELECTRIC_MACHINE;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.ENCLOSURE;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.FILAMENT;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.HEATED_BED;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.JOG;
import static fabscreen.features.machinetools.control.j1.J1ControlSectionsFragment.SectionItem.LASER;

public class J1ControlSectionsViewModel extends BaseViewModel {
    private final ArrayList<SectionItem> mItems = new ArrayList<>();
    private final IMachine mMachineService;

    public J1ControlSectionsViewModel() {
        mMachineService = getServiceContainer().getService(IMachine.class);
    }

    @NonNull
    public List<SectionItem> getControlSections() {
        // TODO: 2022/2/24 SeriesId may not init.
        MachineInfo info = mMachineService.getMachineInfoSubjectHolder().getValue();
        int seriesId = info.seriesId;
        if (seriesId == IMachine.MachineSeries.J) {
            mItems.add(new SectionItem(FILAMENT, "材料", true));
            mItems.add(new SectionItem(JOG, "移动"));
            mItems.add(new SectionItem(HEATED_BED, "热床"));
            mItems.add(new SectionItem(ELECTRIC_MACHINE, "电机"));
        } else {
            IMachine.WorkType workType = info.workType;
            mItems.add(new SectionItem(JOG, "Jog", true));
            if (workType == IMachine.WorkType.FDM) {
                mItems.add(new SectionItem(FILAMENT, "Filament"));
                mItems.add(new SectionItem(HEATED_BED, "Heated Bed"));
            } else if (workType == IMachine.WorkType.LASER) {
                mItems.add(new SectionItem(LASER, "Laser"));
            } else if (workType == IMachine.WorkType.CNC) {
                mItems.add(new SectionItem(CNC, "CNC"));
            }

            if (info.isEnclosureAvailable) {
                mItems.add(new SectionItem(ENCLOSURE, "Enclosure"));
            }

            if (info.isAirPurifierAvailable) {
                mItems.add(new SectionItem(AIR_PURIFIER, "Air Purifier"));
            }

            if (info.isDryBoxAvailable) {
                mItems.add(new SectionItem(DRIER, "Drier"));
            }
        }
        return mItems;
    }
}
