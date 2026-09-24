package fabscreen.features.machinetools.control.a400.viewmodel;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class A400FilamentControlViewModel extends BaseViewModel {
    public final int A400_DEFAULT_HEATING = 200;
    public final int A400_DEFAULT_TEMPERATURE_FLUCTUATION = 3;
    private FDMController mFdmController;
    PublishSubject<FilamentTager> mTargetSubject = PublishSubject.create();

    public A400FilamentControlViewModel() {
        mFdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mTargetSubject.sample(300, TimeUnit.MILLISECONDS)
                .flatMap(filamentTager -> mFdmController.setExtruderTemperature(0, filamentTager.index, filamentTager.progress))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    if (structure.isSuccess()) {
                        Logger.d("Temperature set.");
                    } else {
                        Logger.d("Temperature set fail.");
                    }
                }, LogHelper::log);

    }


    public boolean isDoubleExtruder() {
        return mFdmController.getHeadType() == Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
    }

    public Observable<FdmToolhead.FdmToolheadStatus> getToolheadStatusObservable() {
        return mFdmController.getToolheadStatusSubjectHolder(0).getObservable();
    }

    public FdmToolhead.FdmToolheadStatus getToolheadStatusValue() {
        return mFdmController.getToolheadStatusSubjectHolder(0).getValue();
    }

    public void setTargetTemp(int index, int progress) {
        mTargetSubject.onNext(new FilamentTager(index, progress));
    }

    public Observable<Boolean> switchExtruder(int toolheadIndex, int extruderIndex) {
        return mFdmController.switchExtruder(toolheadIndex, extruderIndex).flatMap(responseStructure -> Observable.just(responseStructure.resultProp.getValue() == 0));
    }

    public Observable<ResponseStructure> FilamentMove(boolean isLoad) {
        if (isLoad) {
            return mFdmController.requestActivatedExtrusion(0, 90, 240, 0, 0);
        } else {
            return mFdmController.requestActivatedExtrusion(0, 10, 300, 100, 300);
        }
    }

    public Observable<Integer> getActivateNozzle() {
        return mFdmController.getToolheadStatusSubjectHolder().getObservable().flatMap(fdmToolheadStatus -> {
            if (fdmToolheadStatus.getExtruderList().size() > 1) {
                for (int i = 0; i < fdmToolheadStatus.getExtruderList().size(); i++) {
                    if (fdmToolheadStatus.getExtruderList().get(i).getState() == 1) {
                        return Observable.just(i);
                    }
                }
                return Observable.just(-1);
            } else {
                return Observable.just(fdmToolheadStatus.getExtruderList().get(0).getState() == 1 ? 0 : -1);
            }
        });
    }

    public void subscribeDataChange() {
        mFdmController.subscribeExtruderChange();
    }

    public void unSubscribeDataChange() {
        mFdmController.unSubscribeExtruderChange();
    }

    class FilamentTager {
        int index;
        int progress;

        public FilamentTager(int index, int progress) {
            this.index = index;
            this.progress = progress;
        }
    }
}
