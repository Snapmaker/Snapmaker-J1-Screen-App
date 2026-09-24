package fabscreen.platform.core.ui.presenter;


import android.view.View;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class HeatedBedWidgetPresenter extends SetValueRulerWidgetPresenter {
    public HeatedBedWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void bind(View view) {
        super.bind(view);

        setPrecision(0);

        mTvTitle.setText(R.string.print_heated_bed_temp);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_temperature);
        mRvRuler.setMaxValue(100);
    }


    public void connectPrintSettings() {
        int initialValue = (int) ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideInitialHeatedBedTemperature();
        setTargetValue(initialValue);

        Disposable sub = getTargetValueObservable()
                .skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideInitialHeatedBedTemperature(value));
        addDisposable(sub);
    }

    public void connectPrint() {
        mTvValueCurrent.setVisibility(View.VISIBLE);
        mTvValueSlash.setVisibility(View.VISIBLE);

        // update current / target temp.
        Disposable sub = MachineStatusManager.getMachineInfoHolder().getObservable()
                .throttleLast(Constants.THROTTLE_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineStatus -> {
                    setCurrentValue(machineStatus.bedTemperature);
                });
        addDisposable(sub);

        // send target temp.
        sub = getTargetValueObservable()
                .skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    if (!ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getInitialM190Flag()) {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideInitialHeatedBedTemperature(value);
                    }
                    DeprecatedMachineInfo machineInfo = MachineStatusManager.getMachineInfoHolder().getValue();
                    if (value != machineInfo.bedTargetTemperature) {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideHeatedBedTemperature(value);
                    }
                });
        addDisposable(sub);
    }

    public void connectMachineStatus() {
        mTvValueCurrent.setVisibility(View.VISIBLE);
        mTvValueSlash.setVisibility(View.VISIBLE);

        // init target temp.
        DeprecatedMachineInfo machineInfo0 = MachineStatusManager.getMachineInfoHolder().getValue();
        setTargetValue(machineInfo0.bedTargetTemperature);

        // update current / target temp.
        Disposable sub = MachineStatusManager.getMachineInfoHolder().getObservable()
                .debounce(Constants.THROTTLE_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineStatus -> {
                    setCurrentValue(machineStatus.bedTemperature);
                });
        addDisposable(sub);

        // send target temp.
        sub = getTargetValueObservable()
                .skip(1) // skip initial value
                .debounce(200, TimeUnit.MILLISECONDS)
                .flatMap(value -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M140 S" + value))
                .subscribe(value -> { /**/ });
        addDisposable(sub);
    }

    @Override
    protected String formatValue(float value) {
        return String.format(Locale.US, "%d", (int) value);
    }
}
