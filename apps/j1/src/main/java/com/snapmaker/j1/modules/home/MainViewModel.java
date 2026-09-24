package com.snapmaker.j1.modules.home;

import fabscreen.platform.base.BaseMainViewModel;
import io.reactivex.Observable;

public class MainViewModel extends BaseMainViewModel {
    @Override
    protected Observable<Boolean> checkModuleVersions() {
        // J1 don't care module version
        return Observable.just(true);
    }

    @Override
    protected long getOccupiedSpaceInMegaByte() {
        return 200;
    }
}
