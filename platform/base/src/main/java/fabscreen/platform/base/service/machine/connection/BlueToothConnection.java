package fabscreen.platform.base.service.machine.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.IntentFilter;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.receiver.BluetoothDiscoverReceiver;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class BlueToothConnection implements IConnection {
    private static final UUID BLUETOOTH_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // unique identifier

    // bluetooth
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothDiscoverReceiver mBluetoothReceiver;
    private BluetoothSocket mBluetoothSocket;
    private String mBluetoothAddress;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private final BehaviorSubject<Boolean> mConnectedStatusSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<InputStream> mInputStreamBehaviorSubject = BehaviorSubject.create();
    private Subject<byte[]> mWriteSubject;
    private Disposable mWriteDisposable;
    private Disposable subscribe;

    public BlueToothConnection(String bluetoothAddress, Context context) {
        mBluetoothAddress = bluetoothAddress;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        mBluetoothAdapter.startDiscovery();
        // register bluetooth receiver
        mBluetoothReceiver = new BluetoothDiscoverReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(mBluetoothReceiver, intentFilter);

        subscribe = Observable.timer(10, TimeUnit.SECONDS).observeOn(Schedulers.newThread()).subscribe(i -> connect());
    }


    public void connect() {
        if (isConnected()) {
            disConnect();
        }
        Logger.d("---FDT--- mBluetoothAddress " + mBluetoothAddress);
        try {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothAddress);
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(BLUETOOTH_MODULE_UUID);
            mBluetoothSocket.connect();
            mWriteSubject = PublishSubject.<byte[]>create().toSerialized();
            mWriteDisposable = mWriteSubject
                    .subscribeOn(Schedulers.io())
                    .subscribe(bytes -> mBluetoothSocket.getOutputStream().write(bytes));
            mInputStreamBehaviorSubject.onNext(mBluetoothSocket.getInputStream());
            mConnectedStatusSubject.onNext(true);
        } catch (Exception e) {
//            e.printStackTrace();
            Logger.w("BlueToothConnection: Connect fail " + e);
            mConnectedStatusSubject.onNext(false);
            if (subscribe != null) subscribe.dispose();
            subscribe = Observable.timer(10, TimeUnit.SECONDS).observeOn(Schedulers.newThread()).subscribe(i -> connect());
//            AndroidSchedulers.mainThread().scheduleDirect(this::connect, 10000, TimeUnit.MILLISECONDS);
        }

    }

    public void setAddress(String address) {
        mBluetoothAddress = address;
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

    private void disConnect() {
// Throw all the existing connection and subscription
        if (isConnected()) {
            Logger.d("主动 disConnect");
            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.close();
                    mBluetoothSocket = null;
                } catch (IOException e) {
                    mConnectedStatusSubject.onNext(false);
                    e.printStackTrace();
                }
            }

            if (mInputStreamBehaviorSubject.getValue() != null) {
                try {
                    mInputStreamBehaviorSubject.getValue().close();
                } catch (IOException e) {
                    mConnectedStatusSubject.onNext(false);
                    e.printStackTrace();
                }
            }

            if (mWriteDisposable != null) {
                mWriteDisposable.dispose();
                mWriteDisposable = null;
            }
            mConnectedStatusSubject.onNext(false);
        }
    }

    private boolean isConnected() {
        return mBluetoothSocket != null;
    }
}
