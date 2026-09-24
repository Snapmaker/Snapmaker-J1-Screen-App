package fabscreen.platform.base.service.machine.connection;

import java.io.InputStream;

import io.reactivex.Observable;

public interface IConnection {

    Observable<Boolean> getConnectionStatusObservable();

    Observable<InputStream> getInputStreamObservable();

    void write(byte[] data);

    void disConnection();
}
