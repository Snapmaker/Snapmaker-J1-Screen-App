package fabscreen.platform.base.service.remote.proxy;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.protocol.IProtocol;
import fabscreen.platform.base.service.remote.RemoteConnectionController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.ByteString;

public class ScreenProxy implements IScreenProxy {
    private final MachineConnectionController mMachineConnectionController;
    private final RemoteConnectionController mRemoteConnectionController;
    private final BehaviorSubject<Integer> mConnectedSubject;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public ScreenProxy(MachineConnectionController machineConnectionController, RemoteConnectionController remoteConnectionController, BehaviorSubject<Integer> connectedSubject) {
        mMachineConnectionController = machineConnectionController;
        mRemoteConnectionController = remoteConnectionController;
        mConnectedSubject = connectedSubject;
        observeData();
    }

    private void observeData() {
        mDisposable.add(mMachineConnectionController.getProxyDataObservable()
                .subscribe(bytes -> passData(bytes[5] & 0xff, bytes), LogHelper::log));
//        mDisposable.add(mRemoteConnectionController.getProxyDataObservable()
//                .subscribe(bytes -> passData(bytes[5] & 0xff, bytes), LogHelper::log));
    }

    private void passData(int toWhom, byte[] bytes) {
//        Logger.d("---FDT--- passData to %d data:%s", toWhom, ByteString.of(bytes).hex());
        if (mConnectedSubject.getValue() != 2) {
            Logger.d("TCP Unauthenticated data，Connected：%d data:%s", mConnectedSubject.getValue(), ByteString.of(bytes).hex());
            return;
        }
        if (toWhom == IProtocol.CommunicationId.CONTROLLER) {
            mMachineConnectionController.proxySendRaw(bytes);
        } else if (toWhom == IProtocol.CommunicationId.LUBAN) {
            mRemoteConnectionController.proxySendRaw(bytes);
        }
    }

    @Override
    public void destroy() {
        mDisposable.clear();
    }
}
