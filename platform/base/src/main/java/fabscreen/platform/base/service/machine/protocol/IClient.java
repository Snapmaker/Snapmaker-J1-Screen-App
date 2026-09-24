package fabscreen.platform.base.service.machine.protocol;

import androidx.annotation.Nullable;

import fabscreen.platform.base.service.machine.IStructure;
import io.reactivex.Observable;

public interface IClient {
    <T extends IStructure> Observable<T> request(int commandSet, int commandId, int receiverId, @Nullable IStructure requestBody, T responseStruct);

    <T extends IStructure> Observable<T> watch(int commandSet, int commandId, T responseStruct);

    void onResponse(IProtocol.Packet packet);

    void unWatch(int commandSet, int commandId);
}
