package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.lib.fabserver.RetryWithDelay;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ReplaceModuleViewModel extends BaseViewModel {

    private final IMachine mMachine;

    private final PublishSubject<Boolean> mIsRestartSuccessSubj = PublishSubject.create();
    private PublishSubject<MachineInfo> mMachineInfoSubj;
    private final List<Module> mOldModuleList;
    private List<Module> mRemovedModuleList;
    private List<Module> mAddedModuleList;
    private List<String> mNeedCalibrateModuleNames;

    public ReplaceModuleViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
        mOldModuleList = mMachine.getMachineInfoSubjectHolder().getValue().moduleList;
        observeModuleInfo();
    }

    private void observeModuleInfo() {
        mMachine.getMachineInfoSubjectHolder().getObservable()
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    if (mMachineInfoSubj != null) {
                        mMachineInfoSubj.onNext(machineInfo);
                    }
                }, e -> {
                    LogHelper.log(e);
                    if (mMachineInfoSubj != null) {
                        mMachineInfoSubj.onError(e);
                    }
                });
    }

    public Observable<Integer> startReplaceModuleMode(boolean switchOn) {
        return mMachine.getMachineController()
                .startReplacePartsMode(switchOn)
                .map(structure -> structure.resultProp.getValue());
    }

    public Observable<Integer> restartMachine() {
        return mMachine.getMachineController()
                .restartMachine()
                .map(structure -> structure.resultProp.getValue())
                .doOnNext(result -> {
                    if (result == 0) {
                        // delay and request heartbeat
                        requestHeartbeatWithDelay();
                    }
                });
    }

    private void requestHeartbeatWithDelay() {
        Logger.d("replacement request heartbeat with delay");
        // TODO: 2022/5/14 use delay() instead
        Schedulers.io().scheduleDirect(() ->
                mMachine.getConnectionController().requestHeartbeat()
                        .retryWhen(new RetryWithDelay(4, 2000))
                        .as(bindToLifecycle())
                        .subscribe(response -> {
                            if (response.isSuccess()) {
                    /*
                     Machine rebooted and will start push heartbeat msg, which will trigger
                     MachineController to init modules.
                     */
                                watchModulesInit();
                            } else {
                                Logger.d("subscribe fail");
                            }
                        }, e -> {
                            mIsRestartSuccessSubj.onNext(false);
                            LogHelper.log(e);
                        }), 10, TimeUnit.SECONDS);
//        Observable.timer(10, TimeUnit.SECONDS)
//                .flatMap(aLong -> {
//                    Logger.d("10s-----");
//                    return mMachine.getConnectionController().requestHeartbeat();
//                })

    }

    private void watchModulesInit() {
        mMachineInfoSubj = PublishSubject.create();
        mMachineInfoSubj
                .filter(machineInfo -> {
//                    Logger.d("info before filter: %s", machineInfo);
                    return machineInfo.moduleList != null;
                })
                .timeout(30, TimeUnit.SECONDS)
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    mRemovedModuleList = findRemoved(mOldModuleList, machineInfo.moduleList);
                    mAddedModuleList = findAdded(mOldModuleList, machineInfo.moduleList);
                    mIsRestartSuccessSubj.onNext(true);
//                    mIsRestartSuccessSubj.onComplete();
                    mMachineInfoSubj.onComplete();
                }, e -> {
                    LogHelper.log(e);
                    mIsRestartSuccessSubj.onNext(false);
                });
    }

    public Observable<Boolean> getMachineRestartObservable() {
        return mIsRestartSuccessSubj.hide();
    }

    public List<String> getRemovedModuleList() {
        List<String> moduleNames = new ArrayList<>();
        if (mRemovedModuleList != null) {
            mRemovedModuleList.forEach(module -> moduleNames.add(module.getDisplayName()));
        }
        return moduleNames;
    }

    public List<String> getAddedModuleList() {
        List<String> moduleNames = new ArrayList<>();
        if (mAddedModuleList != null) {
            mAddedModuleList.forEach(module -> moduleNames.add(module.getDisplayName()));
        }
        return moduleNames;
    }

    public List<String> getNeedCalibrateModuleList() {
        if (mNeedCalibrateModuleNames == null) {
            mNeedCalibrateModuleNames = new ArrayList<>();
            if (mAddedModuleList != null) {
                for (Module module : mAddedModuleList) {
                    if (module instanceof Toolhead) {
                        mNeedCalibrateModuleNames.add(module.getDisplayName());
                    }
                }
            }
        }
        return mNeedCalibrateModuleNames;
    }

    public boolean needCalibrate() {
        return getNeedCalibrateModuleList().size() > 0;
    }

    private List<Module> findAdded(List<Module> oldModuleList, List<Module> newModuleList) {
        List<Module> added = new ArrayList<>(newModuleList);
        for (int i = 0; i < newModuleList.size(); i++) {
            Module addedModule = newModuleList.get(i);
            for (int j = 0; j < oldModuleList.size(); j++) {
                Module oldModule = oldModuleList.get(j);
                if ((oldModule.getModuleInfo().getSn() + oldModule.getModuleInfo().getModuleId() == addedModule.getModuleInfo().getSn() + addedModule.getModuleInfo().getModuleId()) && oldModule.getDisplayName().equals(addedModule.getDisplayName())) {
                    added.remove(addedModule);
                }
            }
        }
        return added;
    }

    private List<Module> findRemoved(List<Module> oldModuleList, List<Module> newModuleList) {
        List<Module> removed = new ArrayList<>(oldModuleList);
        for (int i = 0; i < oldModuleList.size(); i++) {
            Module removedModule = oldModuleList.get(i);
            for (int j = 0; j < newModuleList.size(); j++) {
                Module newModule = newModuleList.get(j);
//                Logger.d("old name # sn: %1$s # %2$s, new name # sn: %3$s # %4$s", removedModule.getDisplayName(), String.valueOf(removedModule.getModuleInfo().getSn()), String.valueOf(newModule.getModuleInfo().getSn()), newModule.getDisplayName());
                if ((newModule.getModuleInfo().getSn() + newModule.getModuleInfo().getModuleId() == removedModule.getModuleInfo().getSn() + removedModule.getModuleInfo().getModuleId()) && newModule.getDisplayName().equals(removedModule.getDisplayName())) {
                    removed.remove(removedModule);
                }
            }
        }
        return removed;
    }
}
