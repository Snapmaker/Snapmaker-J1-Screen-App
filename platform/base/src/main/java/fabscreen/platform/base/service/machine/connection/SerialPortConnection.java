package fabscreen.platform.base.service.machine.connection;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.lib.serialport.SerialPort;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class SerialPortConnection implements IConnection {
    // A400
    private static final String A400_SERIAL_DEVICE = "/dev/ttyMSM2";
    // J1 & A350
    private static final String J1_SERIAL_DEVICE = "/dev/ttyHSL1";

    private static String SERIAL_DEVICE;
    SerialPort mSerialPort;
    private final BehaviorSubject<Boolean> mConnectedStatusSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<InputStream> mInputStreamBehaviorSubject = BehaviorSubject.create();
    private Scheduler.Worker mConnectionWorker;
    private Disposable mWriteDisposable;
    private Subject<byte[]> mWriteSubject;
    private final String mSerialPortPath;

    public SerialPortConnection() {
        mSerialPortPath = ServiceContainer.getInstance().getService(IAppService.class).getApp().getSerialPortPath();
        connect();
    }

    public void connect() {
        if (isConnected()) {
            disconnect();
        }

        mConnectionWorker = Schedulers.io().createWorker();
        mConnectionWorker.schedule(() -> {
            try {
                mSerialPort = new SerialPort(new File(mSerialPortPath), 115200);
                mConnectedStatusSubject.onNext(true);
                // Pass InputStream to protocol to read packet.
                mInputStreamBehaviorSubject.onNext(mSerialPort.getInputStream());
            } catch (IOException | InterruptedException | UnsatisfiedLinkError e) {
                e.printStackTrace();
                Logger.w("SerialPortConnection: Connect fail");
                mConnectedStatusSubject.onNext(false);
            }
        });

        mWriteSubject = PublishSubject.<byte[]>create().toSerialized();
        mWriteDisposable = mWriteSubject
                .subscribeOn(Schedulers.io())
                .subscribe(bytes -> mSerialPort.getOutputStream().write(bytes));
    }

    @Override
    public Observable<InputStream> getInputStreamObservable() {
        return mInputStreamBehaviorSubject.hide();
    }

    @Override
    public void write(byte[] data) {
        if (mWriteSubject != null) {
            mWriteSubject.onNext(data);
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

    // Temporary set public for factory app testing.
    public void disconnect() {
        if (isConnected()) {
            mConnectionWorker.dispose();
            mConnectionWorker = null;
            //todo need child thread?
            mSerialPort.close();
            mSerialPort = null;
            mWriteDisposable.dispose();
            mConnectedStatusSubject.onNext(false);
        }
    }

    private boolean isConnected() {
        return mSerialPort != null;
    }
}
