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

public class NozzleWidgetPresenter extends SetValueRulerWidgetPresenter {
    public NozzleWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void bind(View view) {
        super.bind(view);

        mTvTitle.setText(R.string.print_nozzle_temp);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_temperature);
        mRvRuler.setMaxValue(275);
    }

    /**
     * connect print settings.
     * - current: N/A
     * - target: ruler value (initially use settings value)
     */
    public void connectPrintSettings() {
        int initialValue = (int) ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideInitialNozzleTemperature();
        setTargetValue(initialValue);

        Disposable sub = getTargetValueObservable()
                .skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideInitialNozzleTemperature(value));
        addDisposable(sub);
    }

    /**
     * Connect printing.
     * - current: machine status
     * - target: ruler value
     */
    public void connectPrint() {
        mTvValueCurrent.setVisibility(View.VISIBLE);
        mTvValueSlash.setVisibility(View.VISIBLE);

        // update current / target temp.
        Disposable sub = MachineStatusManager.getMachineInfoHolder().getObservable()
                .throttleLast(Constants.THROTTLE_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineStatus -> {
                    setCurrentValue(machineStatus.leftNozzleTemperature);
                });
        addDisposable(sub);

        // send target temp.
        sub = getTargetValueObservable()
                .skip(1) // skip initial value
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    if (!ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getInitialM109Flag()) {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideInitialNozzleTemperature(value);
                    }
                    DeprecatedMachineInfo machineInfo = MachineStatusManager.getMachineInfoHolder().getValue();
                    if (value != machineInfo.leftNozzleTargetTemperature) {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideNozzleTemperature(value);
                    }
                });
        addDisposable(sub);
    }

    /**
     * connect machine status
     * - current: current nozzle temp.
     * - target: ruler value (initially use machine status target value)
     */
    public void connectMachineStatus() {
        mTvValueCurrent.setVisibility(View.VISIBLE);
        mTvValueSlash.setVisibility(View.VISIBLE);

        // init target temp.
        DeprecatedMachineInfo machineInfo0 = MachineStatusManager.getMachineInfoHolder().getValue();
        setCurrentValue(machineInfo0.leftNozzleTemperature);
        setTargetValue(machineInfo0.leftNozzleTargetTemperature);

        // update current / target temp.
        Disposable sub = MachineStatusManager.getMachineInfoHolder().getObservable()
                .throttleLast(Constants.THROTTLE_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineStatus -> {
                    setCurrentValue(machineStatus.leftNozzleTemperature);
                });
        addDisposable(sub);

        // send target temp.
        sub = getTargetValueObservable()
                .skip(1) // skip initial value
                .debounce(200, TimeUnit.MILLISECONDS)
                .flatMap(value -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M104 S" + value))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> { /**/ });
        addDisposable(sub);
    }

    @Override
    protected String formatValue(float value) {
        return String.format(Locale.US, "%d", (int) value);
    }
}
