package fabscreen.features.settings.a400.update;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.helper.ModuleIdNameMapper;
import fabscreen.platform.base.lib.update.MachineInfoLite;
import fabscreen.platform.base.lib.update.UpdateFileParser;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class UpdateSuccessViewModel extends BaseViewModel {

    private final IMachine mMachine;
    private final IAppService mAppService;
    private final Map<Integer, String> mNewVersionMap = new HashMap<>();

    public UpdateSuccessViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
        mAppService = getServiceContainer().getService(IAppService.class);
    }

    public Observable<FirmwareChanges> getUpdateChangesObservable() {
        return Observable.create((ObservableOnSubscribe<FirmwareChanges>) emitter -> {
            // In the beginning, toBeUpdatedIdList contains all modules included in the em.bin
            List<Integer> updatedHWIdList = new ArrayList<>();
            List<Integer> toBeUpdatedIdList = getToBeUpdatedList();

            MachineInfo machineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
            List<Module> currentModuleList = machineInfo.moduleList;
            for (Module module : currentModuleList) {
                Module.ModuleInfo moduleInfo = module.getModuleInfo();
                mNewVersionMap.put(moduleInfo.getModuleId(), moduleInfo.getFirmwareVersion());
                if (toBeUpdatedIdList.contains(moduleInfo.getModuleId())
                        && moduleInfo.getFirmwareVersion().equals(getLatestEMVersion())) {
                    toBeUpdatedIdList.remove(moduleInfo.getModuleId());
                }
            }

            File configDir = mAppService.getAppContext().getDir("config", Context.MODE_PRIVATE);
            MachineInfoLite oldVersions = new Gson().fromJson(FileHelper.readJSONFromFile(new File(configDir, "machineVersion")), MachineInfoLite.class);
            if (oldVersions == null) return;
            for (MachineInfoLite.ModuleInfoLite oldVersion : oldVersions.moduleVersionList) {
                String newVersion = mNewVersionMap.get(oldVersion.moduleId);
                if (newVersion != null && !newVersion.equals(oldVersion.version)) {
                    updatedHWIdList.add(oldVersion.moduleId);
                }
            }

            FirmwareChanges changes = new FirmwareChanges();
            changes.updatedHWNameList.addAll(ModuleIdNameMapper.convertIdsToNames(mAppService.getAppContext(), updatedHWIdList));
            // We assume these two always updated.
            changes.updatedHWNameList.add("Main Board");
            changes.updatedHWNameList.add("Screen");
            changes.toBeUpdatedNameList.addAll(ModuleIdNameMapper.convertIdsToNames(mAppService.getAppContext(), toBeUpdatedIdList));

            emitter.onNext(changes);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());

    }

    private String getLatestEMVersion() {
        return UpdateFileParser.parseEMVersion(new File(FileHelper.getCachedUpdateFilesDir(mAppService.getAppContext()), "em.bin"));
    }

    private List<Integer> getToBeUpdatedList() {
        // get em.bin and get indexes(module ids)
        return UpdateFileParser.parseEMBinIndexes(new File(FileHelper.getCachedUpdateFilesDir(mAppService.getAppContext()), "em.bin"));
    }

    public static class FirmwareChanges {
        public List<String> updatedHWNameList = new ArrayList<>();
        public List<String> toBeUpdatedNameList = new ArrayList<>();
    }
}
