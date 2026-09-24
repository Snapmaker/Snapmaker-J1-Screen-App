package fabscreen.features.print.j1platform.viewmodel;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class PrintJ1AdjustmentPerformanceViewModel extends BaseViewModel {
    public static final int PERFORMANCE_OPTION_NORMAL = 1;
    public static final int PERFORMANCE_OPTION_SILENT = 2;
    public static final int PERFORMANCE_OPTION_FAST = 3;
    private IMachine mMachine;

    private BehaviorSubject<Integer> mPerformanceOptionSubject = BehaviorSubject.createDefault(1);

    public PrintJ1AdjustmentPerformanceViewModel() {
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
    }

    public Observable<Integer> getPerformanceOptionObservable() {
        return mPerformanceOptionSubject.hide();
    }

    public Observable<ResponseStructure> getPerformanceOption() {
        return mMachine.getNewPrintController().getPrintPerformanceOption()
                .doOnNext(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    int option = (int) baseStructure.getProp("performance_option").getValue();
                    Logger.d("Print performance option " + option);
                    mPerformanceOptionSubject.onNext(option);
                });
    }

    public Observable<ResponseStructure> requestPerformanceOption(int option) {
        return mMachine.getNewPrintController().requestPrintPerformanceOption(option);
    }
}
