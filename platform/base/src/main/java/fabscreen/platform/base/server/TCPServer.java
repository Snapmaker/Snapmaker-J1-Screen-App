package fabscreen.platform.base.server;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class TCPServer {
    private final BehaviorSubject<Socket> mSocketSubject = BehaviorSubject.create();
    private final int mPort;
    private Scheduler.Worker worker;
    private Disposable subscribe;

    public TCPServer(int port) {
        mPort = port;
        initConnection();
    }

    private void initConnection() {
        if (worker != null && !worker.isDisposed()) {
            worker.dispose();
            worker = null;
        }
        worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(mPort, 5);
                while (true) {
                    // accept() will block until client connected.
                    mSocketSubject.onNext(serverSocket.accept());
                }
            } catch (IOException e) {
                LogHelper.log(e);
                Logger.e("TCP connection fail! retry 3 times.");
                retryConnection();
            }
        });
    }

    private void retryConnection() {
        if (subscribe != null && !subscribe.isDisposed()) {
            subscribe.dispose();
            subscribe = null;
        }
        subscribe = Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(time -> initConnection());
    }


    public Observable<Socket> getSocketSubjectObservable() {
        return mSocketSubject.hide();
    }

}
