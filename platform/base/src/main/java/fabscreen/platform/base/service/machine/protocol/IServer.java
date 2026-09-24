package fabscreen.platform.base.service.machine.protocol;

import fabscreen.platform.base.service.machine.IStructure;
import io.reactivex.Observable;
import io.reactivex.subjects.Subject;

public interface IServer<T> {
    /**
     * Listen to all requests.
     */
    Observable<T> listen(Subject<T> serverSubject);

    void onRequest(IProtocol.Packet p);

    void sendResponse(int commandSet, int commandId, int receiverId, int sequence, IStructure payload);
}
