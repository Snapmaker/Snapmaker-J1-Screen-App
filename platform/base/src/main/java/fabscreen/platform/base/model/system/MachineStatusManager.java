package fabscreen.platform.base.model.system;

import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.subjects.BehaviorSubject;

@Deprecated
public class MachineStatusManager {
    private static SubjectHolder<Boolean> mConnectedHolder = new SubjectHolder<>(BehaviorSubject.createDefault(false));
    private static SubjectHolder<Integer> mSeriesHolder = new SubjectHolder<>(BehaviorSubject.createDefault(0));
    private static SubjectHolder<DeprecatedMachineInfo> mMachineInfoHolder = new SubjectHolder<>(BehaviorSubject.createDefault(DeprecatedMachineInfo.getDefaultInstance()));


    public static SubjectHolder<Integer> getSeriesHolder() {
        return mSeriesHolder;
    }

    public static SubjectHolder<Boolean> getConnectedStatus() {
        return mConnectedHolder;
    }

    public static SubjectHolder<DeprecatedMachineInfo> getMachineInfoHolder() {
        return mMachineInfoHolder;
    }

}
