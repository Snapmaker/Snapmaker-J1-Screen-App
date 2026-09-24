package fabscreen.platform.core.ui.presenter;

import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class EnclosureControlWidgetPresenter extends BasePresenter {
    private static final String TAG = EnclosureControlWidgetPresenter.class.getSimpleName();

    @BindView(R2.id.btn_widget_enclosure_led_strip)
    ActionButton mBtnLed;
    @BindView(R2.id.btn_widget_enclosure_cooling_fan)
    ActionButton mBtnFan;

    public EnclosureControlWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    public void bind(View view) {
        ButterKnife.bind(this, view);
    }

    public void connectStatus() {
        // init view
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(enclosureStatus -> {
                    mBtnLed.setEnabled(enclosureStatus.isLedOn());
                    mBtnFan.setEnabled(enclosureStatus.isFanOn());

                    mBtnLed.setActivated(enclosureStatus.isLedOn());
                    mBtnFan.setActivated(enclosureStatus.isFanOn());
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_widget_enclosure_led_strip)
    void onClickLed() {
        final boolean isLedOn = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isLedOn();
        final int value = isLedOn ? 0 : 100;

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureLedLevel(value)
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    mBtnLed.setActivated(status.isLedOn());
                }, LogHelper::log);
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_widget_enclosure_cooling_fan)
    void onClickFan() {
        final boolean isFanOn = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isFanOn();
        final int value = isFanOn ? 0 : 100;

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureFanLevel(value)
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    mBtnFan.setActivated(status.isFanOn());
                }, LogHelper::log);
        addDisposable(sub);
    }

}
