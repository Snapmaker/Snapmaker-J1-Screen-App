package fabscreen.platform.base.service;

import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.remote.RemoteClient;
import fabscreen.platform.base.service.remote.RemoteFileController;
import io.reactivex.Observable;

public interface IRemote {
    Observable<Integer> getRemoteConnectedObservable();

    RemoteFileController getRemoteFilController();

    Observable<ResponseStructure> disConnect();

    void ClearConnection();

    RemoteClient getNowClient();
}
