package fabscreen.features.settings.a400.moduleassistant.replacehotend;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class ReplaceHotendViewModel extends BaseViewModel {

    private final IMachine mMachine;
    private final BehaviorSubject<ReplaceProcess> mReplaceProcessSubj = BehaviorSubject.create();
    private final BehaviorSubject<int[]> mUserSelectTempSubj = BehaviorSubject.createDefault(new int[]{200, 200});
    private int mCurrentTemp;

    public ReplaceHotendViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
    }

    public Observable<ReplaceProcess> getReplaceProcessObservable() {
        return mReplaceProcessSubj.hide();
    }

    public Observable<int[]> getUserSelectTempObservable() {
        return mUserSelectTempSubj.hide();
    }

    public Observable<int[][]> getNozzleTempObservable() {
        return Observable.create(emitter ->
                mMachine.getFDMController().getToolheadInfoObservable(0)
                        .as(bindToLifecycle())
                        .subscribe(response -> {
                            if (response == null) return;
                            FdmToolhead.FdmToolheadStatus dataProp = response.dataProp;
                            if (dataProp == null) return;
                            int tempL = (int) dataProp.getExtruderList().get(0).getTemperature();
                            int targetL = (int) dataProp.getExtruderList().get(0).getTargetTemperature();
                            int tempR = (int) dataProp.getExtruderList().get(1).getTemperature();
                            int targetR = (int) dataProp.getExtruderList().get(1).getTargetTemperature();
                            emitter.onNext(new int[][]{{tempL, targetL}, {tempR, targetR}});
                            // TODO: 2022/7/9 delete below
                            emitter.onNext(new int[][]{{mCurrentTemp, mCurrentTemp}, {mCurrentTemp, mCurrentTemp}});
                        }));
    }

    public void heatNozzle(int leftTemp, int rightTemp) {
        mReplaceProcessSubj.onNext(ReplaceProcess.ON_HEATING_START);
        mMachine.getFDMController().setExtruderTemperature(0, 0, leftTemp).as(bindToLifecycle()).subscribe();
        mMachine.getFDMController().setExtruderTemperature(0, 1, rightTemp).as(bindToLifecycle()).subscribe();
        mCurrentTemp = 180;
        observeHotendTemp();
    }

    private void observeHotendTemp() {
        Observable.timer(5, TimeUnit.SECONDS)
                .as(bindToLifecycle())
                .subscribe(aLong -> {
                    if (mCurrentTemp >= 180) {
                        mReplaceProcessSubj.onNext(ReplaceProcess.ON_HEATED);
                        observeFilamentStatus();
                    } else if (mCurrentTemp <= 30) {
                        mReplaceProcessSubj.onNext(ReplaceProcess.ON_NOZZLE_COOLED);
                    }
                }, LogHelper::log);
    }

    private void observeFilamentStatus() {
        Observable.timer(5, TimeUnit.SECONDS)
                .as(bindToLifecycle())
                .subscribe(aLong -> {
                    mReplaceProcessSubj.onNext(ReplaceProcess.ON_NOZZLE_CLEARED);
                    coolDownHotend();
                }, LogHelper::log);
    }

    private void coolDownHotend() {
        /*mMachine.getFDMController().stopExtruderHeat().as(bindToLifecycle()).subscribe();*/
        mCurrentTemp = 23;
        observeHotendTemp();
    }

    public void restartMainboard() {
        Observable.timer(5, TimeUnit.SECONDS)
                .doOnSubscribe(disposable -> mReplaceProcessSubj.onNext(ReplaceProcess.ON_RESTART_BEGIN))
                .as(bindToLifecycle())
                .subscribe(aLong -> mReplaceProcessSubj.onNext(ReplaceProcess.ON_SUCCESS), LogHelper::log);

        /*mMachine.getMachineController().restartMachine()
                .doOnSubscribe(disposable -> mReplaceProcessSubj.onNext(ReplaceProcess.ON_RESTART_BEGIN))
                .as(bindToLifecycle())
                .subscribe(response -> mReplaceProcessSubj.onNext(ReplaceProcess.ON_SUCCESS), LogHelper::log);*/
    }

    public String[] getReplaceHotendName() {
        return new String[]{"L 0.4mm", "R 0.4mm"};
    }

    public void setUserSelectedTempL(float temp) {
        int tempL = (int) temp;
        int tempR = mUserSelectTempSubj.getValue()[1];
        mUserSelectTempSubj.onNext(new int[]{tempL, tempR});
    }

    public void setUserSelectedTempR(float temp) {
        int tempL = mUserSelectTempSubj.getValue()[0];
        int tempR = (int) temp;
        mUserSelectTempSubj.onNext(new int[]{tempL, tempR});
    }

    public int getDefaultSelectTemL() {
        return mUserSelectTempSubj.getValue()[0];
    }

    public int getDefaultSelectTempR() {
        return mUserSelectTempSubj.getValue()[1];
    }


    enum ReplaceProcess {
        ON_HEATING_START,
        ON_HEATED,
        ON_NOZZLE_CLEARED,
        ON_NOZZLE_COOLED,
        ON_RESTART_BEGIN,
        ON_SUCCESS
    }
}
