package fabscreen.features.settings.a350.experiment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class ExperimentBluetoothFragment extends BaseFragment {
    private final static String TAG = ExperimentBluetoothFragment.class.getSimpleName();

    private final static int STATUS_IDLE = 0;
    private final static int STATUS_BLUETOOTH_OFF = 1;
    private final static int STATUS_BLUETOOTH_ON = 2;
    private final static int STATUS_BLUETOOTH_CONNECTING = 3;
    private final static int STATUS_BLUETOOTH_CONNECTED = 4;

    @BindView(R2.id.btn_experiment_bluetooth_open)
    Button mBtnBluetoothOpen;
    @BindView(R2.id.btn_experiment_bluetooth_request_without_flash_light)
    Button mBtnRequestWithoutFlash;
    @BindView(R2.id.btn_experiment_bluetooth_request)
    Button mBtnRequest;

    @BindView(R2.id.btn_experiment_bluetooth_wb)
    Button mBtnAutoWb;
    @BindView(R2.id.btn_experiment_bluetooth_camera_lighting)
    Button mBtnFlashLight;

    @BindView(R2.id.tv_experiment_bluetooth_status)
    TextView mTvBluetoothStatus;
    @BindView(R2.id.tv_experiment_bluetooth_connected_name)
    TextView mTvBluetoothName;

    @BindView(R2.id.iv_experiment_bluetooth_pic)
    ImageView mIvPic;

    private BehaviorSubject<Integer> mBluetoothStatusSubject = BehaviorSubject.createDefault(0);
    private PublishSubject<Bitmap> mBitmapSubject = PublishSubject.create();
    private BehaviorSubject<Boolean> mAutoWhiteBalanceSubject = BehaviorSubject.createDefault(true);
    private BehaviorSubject<Boolean> mCameraLightingStatus = BehaviorSubject.createDefault(false);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("Bluetooth Demo");

        if (getContext() == null) return;

        // check permission
        if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        // status changed
        mBluetoothStatusSubject
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mBtnRequest.setEnabled(status == STATUS_BLUETOOTH_CONNECTED);
                    mBtnRequestWithoutFlash.setEnabled(status == STATUS_BLUETOOTH_CONNECTED);
                    mBtnBluetoothOpen.setSelected(status != STATUS_BLUETOOTH_OFF);

                    switch (status) {
                        case STATUS_BLUETOOTH_OFF:
                            mTvBluetoothStatus.setText("OFF");
                            new Handler().postDelayed(this::openBluetooth, 500);
                            break;
                        case STATUS_BLUETOOTH_ON:
                            mTvBluetoothStatus.setText("ON");
                            break;
                        case STATUS_BLUETOOTH_CONNECTING:
                            mTvBluetoothStatus.setText("Connecting");
                            break;
                        case STATUS_BLUETOOTH_CONNECTED:
                            mTvBluetoothStatus.setText("Connected");
//                            mTvBluetoothName.setText(ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().getBluetoothName());
                        case STATUS_IDLE:
                        default:
                            break;
                    }
                });

        // bitmap capture
        mBitmapSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(bitmap -> {
                    File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), "test.jpg");

                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    mIvPic.setVisibility(View.VISIBLE);
                    mIvPic.setImageBitmap(bitmap);
                });

        // auto wb
        mAutoWhiteBalanceSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isEnable -> mBtnAutoWb.setSelected(isEnable));

        // flashlight status
        mCameraLightingStatus
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> mBtnFlashLight.setSelected(status));

        // check bluetooth is open
        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().isConnected()) {
            mBluetoothStatusSubject.onNext(STATUS_BLUETOOTH_ON);
        } else {
            mBluetoothStatusSubject.onNext(STATUS_BLUETOOTH_OFF);
        }

        // pull bluetooth controller status
//        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().getBluetoothConnectedObservable()
//                .as(bindToLifecycle())
//                .subscribe(connected -> {
//                    if (connected) {
//                        mBluetoothStatusSubject.onNext(STATUS_BLUETOOTH_CONNECTED);
//                    } else {
//                        AndroidSchedulers.mainThread().scheduleDirect(() -> {
//                            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().updateConnectionStatus();
//                        }, 5000, TimeUnit.MILLISECONDS);
//                    }
//                }, Throwable::printStackTrace);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment_bluetooth;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void setPhotoQuality() {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setPhotoQuality(31)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    Log.e(TAG, "set photo quality " + success);
                });
    }

    private void setPhotoSize() {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController()
                .setPhotoQuality(0x0c)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    Log.e(TAG, "set photo size " + success);
                });
    }

    private void checkWhiteBalance() {
//        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController()
//                .checkCameraAutoWhiteBalanceActivated()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(isEnabled -> {
//                    Log.e(TAG, "Auto WB is " + (isEnabled ? "opened" : "closed"));
//                    mAutoWhiteBalanceSubject.onNext(isEnabled);
//                    mBtnAutoWb.setSelected(isEnabled);
//                });
    }

    @OnClick(R2.id.btn_experiment_bluetooth_open)
    void openBluetooth() {
        playNormalClickSound();
//        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().isEnabled()) {
//            Log.e(TAG, "Bluetooth turning off");
//            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setEnabled(false);
//            mBluetoothStatusSubject.onNext(STATUS_BLUETOOTH_OFF);
//        } else {
//            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setEnabled(true);
//            Log.e(TAG, "Bluetooth turning on");
//            mBluetoothStatusSubject.onNext(STATUS_BLUETOOTH_ON);
//        }
    }

    @OnClick(R2.id.btn_experiment_bluetooth_wb)
    void onClickAutoWb() {
        playNormalClickSound();
//        boolean isEnabled = mAutoWhiteBalanceSubject.getValue();
//        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().isConnected()) {
//            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController()
//                    .setAutoWhiteBalanceStatus(!isEnabled)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .as(bindToLifecycle())
//                    .subscribe(success -> {
//                        if (success) {
//                            mAutoWhiteBalanceSubject.onNext(!isEnabled);
//                        } else {
//                            FabAlert.alert(getContext(), "set Auto WB failed.");
//                        }
//                    });
//        } else {
//            FabAlert.alert(getContext(), "Bluetooth is not connected!");
//        }
    }

    @OnClick(R2.id.btn_experiment_bluetooth_camera_lighting)
    void onClickCameraLighting() {
        playNormalClickSound();
        if (mBluetoothStatusSubject.getValue() == STATUS_BLUETOOTH_CONNECTED) {
            boolean status = !mCameraLightingStatus.getValue();
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController()
                    .setCameraLighting(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        if (success) {
                            mCameraLightingStatus.onNext(status);
                        } else {
                            FabAlert.alert(getContext(), "Set camera lighting failed");
                        }
                    }, Throwable::printStackTrace);
        } else {
            Log.e(TAG, "Bluetooth is not connected.");
        }
    }

    @OnClick(R2.id.iv_experiment_bluetooth_pic)
    void onClickPic() {
        playNormalClickSound();
        mIvPic.setVisibility(View.GONE);
    }

    @OnClick(R2.id.btn_experiment_bluetooth_request)
    void request() {
        playNormalClickSound();
//        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto()
//                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive())
//                .as(bindToLifecycle())
//                .subscribe(bitmap -> mBitmapSubject.onNext(bitmap), Throwable::printStackTrace);
    }
}
