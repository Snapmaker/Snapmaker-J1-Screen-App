package fabscreen.features.print.j1platform.viewmodel;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintJ1AdjustmentLightingViewModel extends BaseViewModel {
    private final MachineInfo mMachineInfo;
    private IMachine mMachine;

    public PrintJ1AdjustmentLightingViewModel() {
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();

        requestInfo();
    }

    public void subscribeEnclosure() {
        mMachine.getMachineController().getEnclosure().subscribeEnclosureInfo();
    }

    public void unsubscribeEnclosure() {
        mMachine.getMachineController().getEnclosure().unsubscribeEnclosureInfo();
    }

    public Observable<ResponseStructure> setEnclosureLedValue(int value) {
        Enclosure enclosure = mMachine.getMachineController().getEnclosure();
        if (enclosure == null) {
            ResponseStructure responseStructure = new ResponseStructure();
            responseStructure.resultProp = new UInt8Prop(-1);
            return Observable.just(responseStructure);
        } else {
            return enclosure.setEnclosureLedLevel(value).doOnNext(responseStructure -> requestInfo());
        }
    }

    public void requestInfo() {
        Enclosure enclosure = mMachine.getMachineController().getEnclosure();
        if (enclosure == null) return;

        enclosure.requestInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    // do nothing.
                }, LogHelper::log);
    }

    public boolean isJ1() {
        return mMachineInfo.seriesId == IMachine.MachineSeries.J && mMachineInfo.modelId == IMachine.MachineModel.J1;
    }
}
