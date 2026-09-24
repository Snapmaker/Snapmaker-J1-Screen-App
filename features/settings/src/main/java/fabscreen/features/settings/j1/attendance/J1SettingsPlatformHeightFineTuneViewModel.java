package fabscreen.features.settings.j1.attendance;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class J1SettingsPlatformHeightFineTuneViewModel extends BaseViewModel {

    public static final float PLATFORM_HEIGHT_MAX_VALUE = 5.50f;
    public static final float PLATFORM_HEIGHT_MIN_VALUE = 4.70f;
    public static final float PLATFORM_HEIGHT_DEFAULT_VALUE = 5.00f;

    private final MachineController mMachineController;
    private BehaviorSubject<Float> mPlatformHeightSubject = BehaviorSubject.createDefault(-1f);
    private PublishSubject<Float> mResultInValidHintSubject = PublishSubject.create();

    public J1SettingsPlatformHeightFineTuneViewModel() {
        mMachineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        requestPlatformHeight();
    }

    public Observable<ResponseStructure> savePlatformHeight() {
        if (mPlatformHeightSubject.getValue() != null && mPlatformHeightSubject.getValue() == -1) {
            ResponseStructure responseStructure = new ResponseStructure<>();
            responseStructure.resultProp = new UInt8Prop(-255);
            return Observable.just(responseStructure);
        }
        return mMachineController.setPlatformHeight(mPlatformHeightSubject.getValue());
    }

    public void requestPlatformHeight() {
        mMachineController.getPlatformHeight()
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    BaseStructure baseStructure = (BaseStructure) structure.dataProp;
                    float height = ((FloatProp) baseStructure.getProp("machine_platform_height")).getValue();
                    mPlatformHeightSubject.onNext(height);
                });
    }

    public Observable<Float> getPlatformHeightObservable() {
        return mPlatformHeightSubject.hide();
    }

    public Observable<Float> getResultInvalidHintSubject() {
        return mResultInValidHintSubject.hide();
    }

    public void handleInputPlatformHeight(String result) {
        float value = PLATFORM_HEIGHT_DEFAULT_VALUE;
        try {
            value = Float.parseFloat(result);

            if (value > PLATFORM_HEIGHT_MAX_VALUE || value < PLATFORM_HEIGHT_MIN_VALUE) {
                value = PLATFORM_HEIGHT_DEFAULT_VALUE;
                mResultInValidHintSubject.onNext(value);
            }

            // cut
            value = (float) Math.round(value * 100) / 100;
            mPlatformHeightSubject.onNext(value);
        } catch (NumberFormatException e) {
            LogHelper.log(e);
            mPlatformHeightSubject.onNext(value);
        }

    }
}
