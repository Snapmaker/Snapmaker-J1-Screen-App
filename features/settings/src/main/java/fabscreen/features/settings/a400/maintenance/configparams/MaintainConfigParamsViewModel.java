package fabscreen.features.settings.a400.maintenance.configparams;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.DeviationStructure;
import fabscreen.platform.base.service.machine.structure.FDMZOffsetStructure;
import fabscreen.platform.base.service.machine.structure.ZOffsetInfo;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class MaintainConfigParamsViewModel extends BaseViewModel {

    private final BehaviorSubject<String> mZOffset0Subj = BehaviorSubject.create();
    private final BehaviorSubject<String> mZOffset1Subj = BehaviorSubject.create();
    private final BehaviorSubject<String> mXOffsetSubj = BehaviorSubject.create();
    private final BehaviorSubject<String> mYOffsetSubj = BehaviorSubject.create();
    private final BehaviorSubject<String> mFocalLenSubj = BehaviorSubject.create();
    private final BehaviorSubject<String> mPlatformHeightSubj = BehaviorSubject.create();
    private final BehaviorSubject<String> mRotaryCenterHeightSubj = BehaviorSubject.create();

    private final IMachine mMachine;

    public MaintainConfigParamsViewModel() {
        //noinspection deprecation
        mMachine = getServiceContainer().getService(IMachine.class);
    }

    public int getHeadType() {
        return mMachine.getMachineInfoSubjectHolder().getValue().headType;
    }

    public void fetchZOffset() {
        mMachine.getFDMController().getZOffset(0)
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        FDMZOffsetStructure fdmzOffsetStructure = new FDMZOffsetStructure();
                        fdmzOffsetStructure.readBuffer(new Buffer().write(response.dataProp.toByteArray()));
                        List<ZOffsetInfo> zOffsetInfoList = fdmzOffsetStructure.getZOffsetInfoList();
                        mZOffset0Subj.onNext(String.valueOf(zOffsetInfoList.get(0).getZOffset()));
                        if (zOffsetInfoList.size() > 1) {
                            mZOffset1Subj.onNext(String.valueOf(zOffsetInfoList.get(1).getZOffset()));
                        }
                    }
                }, LogHelper::log);
    }

    @SuppressWarnings("rawtypes")
    public void fetchXYOffset() {
        mMachine.getFDMController().getExtruderOffset(0)
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        // noinspection unchecked
                        ArrayProp<DeviationStructure> dataProp = (ArrayProp) response.dataProp;
                        ArrayList<DeviationStructure> value = (ArrayList<DeviationStructure>) dataProp.getValue();
                        mXOffsetSubj.onNext(String.valueOf(value.get(0).getValue()));
                        mYOffsetSubj.onNext(String.valueOf(value.get(1).getValue()));
                    }
                });
    }

    public void fetchFocalLength() {
        mFocalLenSubj.onNext(String.valueOf(mMachine.getLaserController().getLaserToolHeadInfoValue().getLaserFocalLength()));
    }

    public void fetchPlatformHeight() {
        mPlatformHeightSubj.onNext(String.valueOf(mMachine.getLaserController().getLaserToolHeadInfoValue().getPlatformHeight()));
    }

    public void fetch4AxisCenterHeight() {
        mRotaryCenterHeightSubj.onNext(String.valueOf(mMachine.getLaserController().getLaserToolHeadInfoValue().getAxisCenterHeight()));
    }

    public void setZOffset(int extruderIndex, float value) {
        mMachine.getFDMController().setZOffset(0, extruderIndex, value)
                .as(bindToLifecycle())
                .subscribe(response -> fetchZOffset(), LogHelper::log);
    }

    public void setXOffset(float value) {
        List<DeviationStructure> offsets = new ArrayList<>();
        offsets.add(new DeviationStructure(1, 0, value));
        mMachine.getFDMController().setExtruderOffset(0, offsets);
        fetchXYOffset();
    }

    public void setYOffset(float value) {
        List<DeviationStructure> offsets = new ArrayList<>();
        offsets.add(new DeviationStructure(1, 1, value));
        mMachine.getFDMController().setExtruderOffset(0, offsets);
        fetchXYOffset();
    }

    public void setFocalLen(float value) {
        mMachine.getLaserController().setFocalLength(value)
                .as(bindToLifecycle())
                .subscribe(response -> fetchFocalLength(), LogHelper::log);
    }

    public void setPlatformHeight(float value) {
        mMachine.getLaserController().setPlatformHeight(value)
                .as(bindToLifecycle())
                .subscribe(response -> fetchPlatformHeight(), LogHelper::log);
    }

    public void setRotaryCenterHeight(float value) {
        mMachine.getLaserController().setAxisCenterHeight(value)
                .as(bindToLifecycle())
                .subscribe(response -> fetch4AxisCenterHeight(), LogHelper::log);
    }

    public Observable<String> getZOffset0Observable() {
        return mZOffset0Subj.hide();
    }

    public Observable<String> getZOffset1Observable() {
        return mZOffset1Subj.hide();
    }

    public Observable<String> getXOffsetObservable() {
        return mXOffsetSubj.hide();
    }

    public Observable<String> getYOffsetObservable() {
        return mYOffsetSubj.hide();
    }

    public Observable<String> getFocalLenObservable() {
        return mFocalLenSubj.hide();
    }

    public Observable<String> getPlatformHeightObservable() {
        return mPlatformHeightSubj.hide();
    }

    public Observable<String> getRotaryCenterHeightObservable() {
        return mRotaryCenterHeightSubj.hide();
    }

    public String getCurrentRotaryCenterHeight() {
        return mRotaryCenterHeightSubj.getValue();
    }

    public String getCurrentPlatformHeight() {
        return mPlatformHeightSubj.getValue();
    }

    public String getCurrentFocalLength() {
        return mFocalLenSubj.getValue();
    }

    public String getCurrentYOffset() {
        return mYOffsetSubj.getValue();
    }

    public String getCurrentXOffset() {
        return mXOffsetSubj.getValue();
    }

    public String getCurrentZOffset(int index) {
        return index == 0 ? mZOffset0Subj.getValue() : mZOffset1Subj.getValue();
    }
}
