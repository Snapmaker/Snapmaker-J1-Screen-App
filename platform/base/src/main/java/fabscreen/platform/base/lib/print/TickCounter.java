package fabscreen.platform.base.lib.print;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.Preferences;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class TickCounter {
    private IPreferences preferences;
    private Disposable disposable;
    private BehaviorSubject<Integer> countSubject = BehaviorSubject.createDefault(0);
    private int totalTicks = 0;
    private int tick = 0;

    public TickCounter() {
        preferences = ServiceContainer.getInstance().getService(IPreferences.class);
        countSubject.onNext(0);
    }

    public int getCount() {
        return countSubject.getValue();
    }

    public Observable<Integer> getCountObservable() {
        return countSubject.hide();
    }

    private void startInterval() {
        if (disposable != null) {
            disposable.dispose();
        }

        disposable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.computation())
                .subscribe(sequence -> {
                    tick += 1;

                    countSubject.onNext(totalTicks + tick);

                    if (tick % 60 == 0) {
                        save();
                    }
                });
    }


    public void load() {
        totalTicks = preferences.getHelper().getPrintElapsedTime();
        tick = 0;
        countSubject.onNext(totalTicks);
    }

    /**
     * Save elapsed time, so we can recover it when power-loss.
     */
    public void save() {
        preferences.getHelper().setPrintElapsedTime(totalTicks + tick);
    }

    public void reset() {
        totalTicks = 0;
        tick = 0;
        countSubject.onNext(0);
    }

    public void start() {
        startInterval();
    }

    public void stop() {
        totalTicks += tick;
        tick = 0;

        if (disposable != null) {
            disposable.dispose();
        }
    }
}
