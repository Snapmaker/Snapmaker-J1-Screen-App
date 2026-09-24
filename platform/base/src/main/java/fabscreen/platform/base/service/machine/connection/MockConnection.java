package fabscreen.platform.base.service.machine.connection;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.protocol.IProtocol;
import fabscreen.platform.base.service.machine.protocol.SACPProtocol;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.ByteString;

public class MockConnection implements IConnection {
    private final BehaviorSubject<Boolean> mConnectedStatusSubject = BehaviorSubject.createDefault(false);
    private final IProtocol mProtocol;
    private MockDataComposer mPacketComposer;
    private final IPreferences mPreferences;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final PipedOutputStream mOutputStream = new PipedOutputStream();

    public MockConnection(IProtocol protocol, IPreferences preferences) {
        mPreferences = preferences;
        mProtocol = protocol;
        connect();
    }

    public void connect() {
        Logger.d("sacp-debug: mock connecting...");
        mConnectedStatusSubject.onNext(true);
        mPacketComposer = new MockDataComposer(mProtocol, mPreferences);
        mDisposable.add(mPacketComposer.getDataSubjectHolder()
                .getObservable()
                .subscribe(bytes -> mOutputStream.write(bytes), LogHelper::log));
    }

    @Override
    public void write(byte[] data) {
        Logger.d("Mock conn: writing data: %s", ByteString.of(data));
        try {
            mockResponse(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disConnection() {
        // TODO
    }

    @Override
    public Observable<Boolean> getConnectionStatusObservable() {
        return mConnectedStatusSubject.hide();
    }

    private void mockResponse(byte[] data) throws IOException {
        IProtocol.Packet packet = mProtocol.decode(data);
        int attribute = packet.header.attribute;
        if (attribute == SACPProtocol.Attribute.ACK) {
            return;
        }
        mPacketComposer.composeResponseData(packet);
    }

    @Override
    public Observable<InputStream> getInputStreamObservable() {
        PipedInputStream inputStream = null;
        try {
            inputStream = new PipedInputStream(mOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream == null) throw new IllegalStateException("InputStream setup fail!");
        return Observable.just(inputStream);
    }
}
