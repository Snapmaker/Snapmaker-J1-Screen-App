package fabscreen.features.settings.j1.attendance;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class J1SettingsVibrationCompensationViewModel extends BaseViewModel {

    public static final int FREQ_MAX = 100;
    public static final int FREQ_MIN = 0;
    private final MachineController mMachineController;
    private final BehaviorSubject<Boolean> mEnableStateSubj = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Integer> mFreqSubj = BehaviorSubject.create();

    public J1SettingsVibrationCompensationViewModel() {
        mMachineController = getServiceContainer().getService(IMachine.class).getMachineController();
        watchVibrationCompensationConfigChanges();
    }

    private void watchVibrationCompensationConfigChanges() {
        // Already subscribed in former page.
        mMachineController.watchVibrationCompensationConfig()
                .as(bindToLifecycle())
                .subscribe(config -> {
                    mEnableStateSubj.onNext(config.getEnabled());
                    mFreqSubj.onNext((int) config.getFrequencyConfig().getFrequency());
                }, LogHelper::log);
    }

    public Observable<Boolean> setCompensationEnabled(boolean enabled) {
        return mMachineController.setVibrationCompensationEnabled(enabled).map(ResponseStructure::isSuccess);
    }

    public void setCompensationFreq(String freq) {
        int inputFreq;
        try {
            inputFreq = Integer.parseInt(freq);
            mFreqSubj.onNext(inputFreq);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        /*mMachineController.saveVibrationCompensationFreq(0, (float) inputFreq)
                .doOnNext(response -> refreshFreq())
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log); */
    }

    private void refreshFreq() {
        mMachineController.getVibrationCompensationFreq(1)
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        /*mFreqSubj.onNext(response.dataProp.getValue().intValue());*/
                    }
                });
    }

    public Observable<Boolean> getEnableStateObservable() {
        return mEnableStateSubj.distinctUntilChanged();
    }

    public Observable<Integer> getFreqObservable() {
        return mFreqSubj.distinctUntilChanged();
    }
}
