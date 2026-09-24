package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import android.content.Context;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.structure.DeviationStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class A400LevelingXYViewModel extends BaseViewModel {
    public static final int ADJUST_VOLUME = 12;
    public static final float CHANGE_AMOUNT = 0.08f;

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;
    private float AdjustX;
    private float AdjustY;

    PrintController printController;
    Context mContext;
    private IPrintWorkspace mWorkspace;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    // indicates the the machine is moving, hence buttons should be disabled temporarily.
    private BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);

    public A400LevelingXYViewModel() {
        super();
        printController = getServiceContainer().getService(IMachine.class).getPrintController();
        mContext = getServiceContainer().getService(IAppService.class).getAppContext();
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
    }

    public void startPrint() {
        mCompositeDisposable.clear();
        File printFile = copyPrintFile();
        mWorkspace.addFileToWorkspace(new FabLocalFile(printFile));
        IFile file1 = mWorkspace.getPrintFile();

        printController.reset();
        printController.setFile(file1);
//        printController.setTotalLines(mWorkspace.getFileTotalLineCount());
//        setPrintControllerListener(printController);

        // Power Panic
        mWaitingSubject.onNext(true);

        printController.start();

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getResumeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resume -> {
//                    resumeFromChangeFilament()
                });
        mCompositeDisposable.add(sub);

        // Update
        sub = Observable.interval(2, TimeUnit.SECONDS)
                .takeUntil(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_COMPLETED)
                .filter(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_PRINTING)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tick -> {
//                    updateProgress();
                }, Throwable::printStackTrace);
        mCompositeDisposable.add(sub);
    }

    private File copyPrintFile() {
        InputStream is = mContext.getResources().openRawResource(R.raw.a400_xy);
        File file = null;
        try {
            file = new File(mContext.getCacheDir().getAbsoluteFile() + "/calibrationXY.gcode");
            if (file.exists()) {
                file.delete();
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[20480];
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
        } catch (Exception e) {
            file = null;
            Logger.d("---FDT--- file Error: " + e);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {

            }
        }
        return file;
    }

    public void setAdjustX(int progress) {
        AdjustX = progress * CHANGE_AMOUNT;
    }

    public void setAdjustY(int progress) {
        AdjustY = progress * CHANGE_AMOUNT;
    }

    public Observable<ResponseStructure> setAdjust() {
        return getServiceContainer().getService(IMachine.class).getFDMController().getExtruderOffset(0)
                .flatMap(responseStructure -> {
                    ArrayProp<DeviationStructure> dataProp = (ArrayProp) responseStructure.dataProp;
                    ArrayList<DeviationStructure> value = (ArrayList<DeviationStructure>) dataProp.getValue();
                    for (DeviationStructure d : value) {
                        if (d.getAxis() == 0) d.setValue(d.getValue() + AdjustX);
                        if (d.getAxis() == 1) d.setValue(d.getValue() + AdjustY);
                    }
                    return getServiceContainer().getService(IMachine.class).getFDMController().setExtruderOffset(0, value);
                });
    }
}
