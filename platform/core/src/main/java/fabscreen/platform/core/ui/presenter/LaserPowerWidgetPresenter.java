package fabscreen.platform.core.ui.presenter;

import android.view.View;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.core.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class LaserPowerWidgetPresenter extends SetValueRulerWidgetPresenter {
    public LaserPowerWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void bind(View view) {
        super.bind(view);
        mTvTitle.setText(R.string.print_laser_power);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_percentage);
        mRvRuler.setMaxValue(100);
        mRvRuler.setUnit(0.5f);
    }

    public void connectPrintSettings() {
        float initialValue = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideLaserPower();
        setTargetValue(initialValue);

        Disposable sub = getTargetValueObservable()
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideLaserPower(value));
        addDisposable(sub);
    }

    public void connectPrint() {
        // init power
        // MachineStatus machineStatus0 = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineStatus();
        // setTargetValue((float) machineStatus0.laserPower);

        // tricky part here, we use override power as initial value, while
        // the power from machine status is always 0.5% for focusing.
        // float initialValue = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideLaserPower();
        // setTargetValue(initialValue);

        // change override power
        Disposable sub = getTargetValueObservable()
                .skip(1) // skip initial value
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideLaserPower(value);
                });
        addDisposable(sub);
    }

    public void connectPrint(float power) {
        setTargetValue(power);

        Disposable sub = getTargetValueObservable()
                .skip(1) // skip initial value
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideLaserPower(value);
                });
        addDisposable(sub);
    }

    public void connectControl() {
        float initialValue = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserControlPower();
        setTargetValue(initialValue);

        // Save the changed value to preferences
        Disposable sub = getTargetValueObservable()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserControlPower(value));
        addDisposable(sub);
    }
}
