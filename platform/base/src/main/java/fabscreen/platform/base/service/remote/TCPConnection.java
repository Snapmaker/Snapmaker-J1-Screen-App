package fabscreen.platform.base.service.remote;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.machine.connection.IConnection;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class TCPConnection implements IConnection {
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final BehaviorSubject<Boolean> mConnectionStatusSubject = BehaviorSubject.create();
    private BehaviorSubject<InputStream> mInputStreamSubject = BehaviorSubject.create();
    private Subject<byte[]> mWriteSubject;

    private final Socket mSocket;
    Scheduler.Worker worker;
    private boolean mTempRefreshState = false;
    InputStream in;
    private boolean isTry = false;

    public TCPConnection(Socket socket) {
        mSocket = socket;
        initConnection();
    }

    private void initConnection() {
        try {
            initWrite(mSocket);
            initRead(mSocket);
        } catch (IOException e) {
            LogHelper.log(e);
            Logger.d("TCP connection exception!");
            refreshConnection(true);
        }

    }

    private void refreshConnection(boolean refreshState) {
        if (refreshState) {
            if (isTry) {
                return;
            } else {
                isTry = true;
                mDisposable.add(Observable.timer(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            if (!mSocket.isClosed() && mSocket.isConnected() && !(mSocket.isInputShutdown() || mSocket.isOutputShutdown())) {
                                initConnection();
                            } else {
                                disConnection();
                            }
                            isTry = false;
                        }));
            }

        } else if (!mTempRefreshState) {
            mTempRefreshState = true;
            refreshConnection(true);
        }
    }

    private void initRead(Socket socket) throws IOException {
        mInputStreamSubject = BehaviorSubject.create();
        // FIXME: 2022/4/11 is connection really established?
        mConnectionStatusSubject.onNext(true);
        PipedOutputStream outputStream = new PipedOutputStream();
        in = socket.getInputStream();
        mInputStreamSubject.onNext(new PipedInputStream(outputStream));
        byte[] buffer = new byte[81920];
        if (worker != null && !worker.isDisposed()) worker.dispose();
        worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            try {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            } catch (SocketException e1) {
                try {
                    in.close();
                    in = socket.getInputStream();
                } catch (Exception ignored) {
                    Logger.e(" initRead inputStream closed. " + e1);
                    disConnection();
                }
            } catch (IOException e) {
                Logger.d("initRead:" + e);
                try {
                    in.close();
                } catch (Exception ignored) {

                }
                // read == -1, client shut down the sockeacat.
                refreshConnection(false);
            }
        });
    }

    private void initWrite(Socket socket) throws IOException {
        if (mWriteSubject != null && !mWriteSubject.hasComplete()) mWriteSubject.onComplete();
        mWriteSubject = PublishSubject.<byte[]>create().toSerialized();
        OutputStream outputStream = socket.getOutputStream();
        mDisposable.add(
                mWriteSubject
                        .observeOn(Schedulers.io())
                        .subscribe(bytes -> {
                            try {
                                outputStream.write(bytes);
                            } catch (SocketException e1) {
                                try {
                                    outputStream.close();
                                } catch (Exception ignored) {
                                }
                                Logger.e("---FDT--- initWrite outputStream is close " + e1);
                                disConnection();
                            } catch (IOException e) {
                                try {
                                    outputStream.close();
                                } catch (Exception ignored) {

                                }
                                LogHelper.log(e);
                                refreshConnection(false);
                            }
                        }, LogHelper::log));
    }

    @Override
    public void write(byte[] data) {
        if (mWriteSubject == null) return;
        mWriteSubject.onNext(data);
    }

    @Override
    public Observable<Boolean> getConnectionStatusObservable() {
        return mConnectionStatusSubject.hide();
    }

    @Override
    public Observable<InputStream> getInputStreamObservable() {
        return mInputStreamSubject.hide();
    }

    @Override
    public void disConnection() {
        mDisposable.clear();
        try {
            if (in != null) {
                in.close();
            }
            if (!mSocket.isClosed() && mSocket.isConnected()) {
                mSocket.shutdownOutput();
                mSocket.shutdownInput();
                mSocket.close();
            }
        } catch (Exception e) {
        }
        if (mWriteSubject != null) {
            mWriteSubject.onComplete();
            mWriteSubject = null;
        }
        if (mInputStreamSubject != null) {
            mInputStreamSubject.onComplete();
            mInputStreamSubject = null;
        }
        if (worker != null) {
            worker.dispose();
            worker = null;
        }
        mConnectionStatusSubject.onNext(false);
    }
}
