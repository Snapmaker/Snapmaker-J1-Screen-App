package fabscreen.platform.base.view.debugtool;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.connection.mock.DebugModule;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.DeviationStructure;
import fabscreen.platform.base.service.machine.structure.MachineFault;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_AIR_PURIFIER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_DRY_BOX;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_EMERGENCY_BUTTON;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_S20;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_2;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_2_5;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ROTARY_MODULE;

public class MockMachineActivity extends BaseActivity {
    IMachine mMachine;
    IPreferences mPreferences;

    @BindView(R2.id.switch_debug_mode)
    Switch mDebugMode;
    @BindView(R2.id.sw_factory_usb_off)
    Switch mSwUSBFactoryMode;
    @BindView(R2.id.sp_debug_machine_changed)
    Spinner mSpMachineChanged;
    @BindView(R2.id.sp_changed_machine_model)
    Spinner mSpMachineModel;

    @BindView(R2.id.bt_debug_module_add)
    Button mBtModuleAdd;
    @BindView(R2.id.rv_module_list)
    RecyclerView mRvModuleList;
    @BindView(R2.id.tv_temp_show)
    TextView mTvshow;
    @BindView(R2.id.tv_temp_sv)
    ScrollView mSvshow;
    @BindView(R2.id.tv_firm_versions)
    TextView mTvFirmVersions;
    @BindView(R2.id.tv_ip_address)
    TextView mTvIPAddress;
    @BindView(R2.id.sp_debug_camera_demo)
    Spinner mSpDebug;

    int newMachineSeriesId;
    int newMachineModelId;
    int newCameraModelId;
    ILaserCameraController laserCameraController;
    List<DebugModule> debugModules = new ArrayList<DebugModule>();
    AlertDialog alertDialog;
    MockModuleAdapter adapter;
    private IAppService mApp;
    private IPreferences.Helper mPrefHelper;

    @SuppressLint("AutoDispose")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_machine_);
        ButterKnife.bind(this);

        mSpDebug.setSelection(7);
        mPrefHelper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mPreferences = ServiceContainer.getInstance().getService(IPreferences.class);
        mApp = ServiceContainer.getInstance().getService(IAppService.class);

        displayVersions();

        mDebugMode.setChecked(mMachine.getMockModeEnabled());
        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController() != null)
            laserCameraController = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController();
        mDebugMode.setOnCheckedChangeListener((buttonView, isChecked) -> mMachine.setMockModeEnabled(isChecked));

        mSwUSBFactoryMode.setChecked(mPreferences.getHelper().getUSBFactoryModeOn());
        mSwUSBFactoryMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPreferences.getHelper().setUSBFactoryModeOn(isChecked);
            ServiceContainer.getInstance().getService(IAppService.class).restart();
        });

        if (!mMachine.getMockModeEnabled()) {
            mSpMachineChanged.setEnabled(false);
            mSpMachineModel.setEnabled(false);
            mBtModuleAdd.setEnabled(false);
            mSvshow.setVisibility(View.VISIBLE);
            mTvshow.setVisibility(View.VISIBLE);
            mRvModuleList.setVisibility(View.INVISIBLE);
        } else {
            mSpMachineChanged.setEnabled(true);
            mSpMachineModel.setEnabled(true);
            mBtModuleAdd.setEnabled(true);
            mSpMachineChanged.setSelection(mPreferences.getHelper().getDebugMachineSeries());
            mSpMachineModel.setSelection(mPreferences.getHelper().getDebugMachineModel());
            Set<String> debugModuleList = mPreferences.getHelper().getDebugModuleList();
            if (debugModuleList != null) {
                for (String s : debugModuleList) {
                    DebugModule debugModule = new DebugModule(s);
                    debugModules.add(debugModule);
                }
            }
            adapter = new MockModuleAdapter(debugModules);
            adapter.setOnDeleteModuleListener(position -> {
                debugModules.remove(position);
                adapter.setDebugModuleList(debugModules);
                adapter.notifyDataSetChanged();
            });
            mRvModuleList.setLayoutManager(new LinearLayoutManager(this));
            mRvModuleList.setAdapter(adapter);
            mTvshow.setVisibility(View.INVISIBLE);
            mSvshow.setVisibility(View.INVISIBLE);
            mRvModuleList.setVisibility(View.VISIBLE);
        }


        ArrayList<DebugModule> tempDebugModules = new ArrayList<>();
        tempDebugModules.add(new DebugModule(ADDON_HEATED_BED_S20, 0));
        tempDebugModules.add(new DebugModule(HEAD_3DP, 0));
        tempDebugModules.add(new DebugModule(HEAD_CNC, 0));
        tempDebugModules.add(new DebugModule(HEAD_LASER, 0));
        tempDebugModules.add(new DebugModule(LINEAR_2, 0));
        tempDebugModules.add(new DebugModule(ADDON_ENCLOSURE, 0));
        tempDebugModules.add(new DebugModule(ROTARY_MODULE, 0));
        tempDebugModules.add(new DebugModule(ADDON_AIR_PURIFIER, 0));
        tempDebugModules.add(new DebugModule(ADDON_EMERGENCY_BUTTON, 0));
        tempDebugModules.add(new DebugModule(LINEAR_2_5, 0));
        tempDebugModules.add(new DebugModule(HEAD_LASER_10W, 0));
        tempDebugModules.add(new DebugModule(HEAD_3DP_DOUBLE_EXTRUDER, 0));
        tempDebugModules.add(new DebugModule(HEAD_CNC_200W, 0));
        tempDebugModules.add(new DebugModule(ADDON_DRY_BOX, 0));
        String[] strings = new String[tempDebugModules.size()];
        for (int i = 0; i < tempDebugModules.size(); i++) {
            strings[i] = tempDebugModules.get(i).getModuleName();
        }
        alertDialog = new AlertDialog
                .Builder(this)
                .setItems(strings, (dialog, which) -> {
                    int moduleId = tempDebugModules.get(which).moduleId;
                    int index = 0;
                    for (int i = 0; i < debugModules.size(); i++) {
                        if (debugModules.get(i).moduleId == moduleId) {
                            index++;
                        }
                    }
                    debugModules.add(new DebugModule(moduleId, index));
                    adapter.setDebugModuleList(debugModules);
                    adapter.notifyDataSetChanged();
                }).create();

//        ServiceContainer.getInstance().getService(IRemote.class).getRemoteFilController()
//                .getRequestFileObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(file -> {
//                    mTvshow.setText(file.getName());
//                }, LogHelper::log);


        // check ip address
        String addressString = "";
        try {
            List<NetworkInterface> interfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaceList) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String sAddr = address.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (isIPv4) {
                            addressString = sAddr + "\napp版本" + mPrefHelper.getLastUpdatePackageVersion();
                            mTvIPAddress.setText(addressString);
                        }
                    }
                }

                if (addressString.isEmpty()) {
                    mTvIPAddress.setText("no connected" + "\napp版本" + mPrefHelper.getLastUpdatePackageVersion());
                }
            }
        } catch (SocketException e) {
            LogHelper.log(e);
        }

    }

    private void displayVersions() {
        MachineInfo machineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        String screenVersion = mApp.getApp().getAppVersionName();
        String screenBuildTime = mApp.getApp().getBuildTime();
        String controllerVersion = null;
        if (machineInfo != null) {
            controllerVersion = machineInfo.controllerFWVersion;
        }
        mTvFirmVersions.setText("屏幕版本：" + screenVersion + " | " + screenBuildTime + "\n主控版本：" + controllerVersion);
    }


    @OnClick(R2.id.bt_debug_module_add)
    public void onAddModule() {
        alertDialog.show();
    }

    @OnItemSelected(R2.id.sp_debug_machine_changed)
    public void setSpMachineSeriesIdChanged(Spinner spinner, int position) {
        newMachineSeriesId = position;
    }

    @OnItemSelected(R2.id.sp_changed_machine_model)
    public void setSpMachineModelIdChanged(Spinner spinner, int position) {
        newMachineModelId = position;
    }

    @OnItemSelected(R2.id.sp_debug_camera_demo)
    public void setSpCameraIdChanged(Spinner spinner, int position) {
        newCameraModelId = position;
    }


    @OnClick(R2.id.bt_confirm)
    public void onConfirm() {
        Set<String> strings = new ArraySet<String>();
        for (int i = 0; i < debugModules.size(); i++) {
            DebugModule debugModule = debugModules.get(i);
            strings.add(debugModule.toString());

        }
        mMachine.setMockMachineSeriesModel(newMachineSeriesId, newMachineModelId, strings);
    }

    Observable<ResponseStructure> responseStructureObservable = null;
    Disposable subscribe;
    int mIndex = 0;

    @SuppressLint("AutoDispose")
    @OnClick(R2.id.bt_temp)
    public void onTemp() {
        if (subscribe != null && !subscribe.isDisposed()) subscribe.dispose();
        switch (newCameraModelId) {
            case 0:
                subscribe = laserCameraController.requestCapturePhoto()
                        .flatMap(success -> laserCameraController.watchPhotoReceive())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            Drawable drawable = new BitmapDrawable(bitmap);
                            mTvshow.setBackground(drawable);
                            saveJpg(bitmap, mIndex++ + ".jpg");
                            subscribe.dispose();
                        }, LogHelper::log);
                break;
            case 1:
                switch (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType) {
                    case FDM:
                        responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(false);
                        break;
                    case LASER:
                        responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().exitCalibration(false);
                        break;
                    case CNC:
                        responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getCNCController().exitCalibration(false);
                        break;
                }
                subscribe = responseStructureObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(
                        responseStructureObservable -> Logger.d("response clash " + responseStructureObservable)
                        , LogHelper::log);
                break;
            case 2:
                List<DeviationStructure> deviationStructures = new ArrayList<>();
                deviationStructures.add(new DeviationStructure(1, 0, 24f));
                deviationStructures.add(new DeviationStructure(1, 1, 0f));
                deviationStructures.add(new DeviationStructure(1, 2, 0f));
                subscribe = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().setExtruderOffset(0, deviationStructures)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseStructure -> {
                            mTvshow.setText(responseStructure.toString());
                        }, LogHelper::log);
                break;
            case 3:
                subscribe = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().getExtruderOffset(0)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseStructure -> {
                            ArrayProp<DeviationStructure> dataProp = (ArrayProp<DeviationStructure>) responseStructure.dataProp;
                            List<DeviationStructure> value = dataProp.getValue();
                            StringBuilder str = new StringBuilder();
                            for (DeviationStructure d : value
                            ) {
                                str.append(d.toString());
                            }
                            mTvshow.setText(str.toString());
                        }, LogHelper::log);
                break;
            case 4:
                subscribe = ServiceContainer.getInstance().getService(IMachine.class).getErrorController().queryException()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseStructure -> {
                            BaseStructure baseStructure = new BaseStructure() {
                                @Override
                                protected void init() {
                                    addProp("exceptionInfos", new ArrayProp<>(new MachineFault()));
                                    addProp("machineBehaviorStates", new ArrayProp<>(new UInt8Prop(-1)));
                                }
                            };

                            BaseStructure baseStructure1 = (BaseStructure) responseStructure.dataProp;
                            List<MachineFault> exceptionInfos = ((ArrayProp<MachineFault>) baseStructure1.getProp("exceptionInfos")).getValue();
                            List<UInt8Prop> machineBehaviorStates = ((ArrayProp<UInt8Prop>) baseStructure1.getProp("machineBehaviorStates")).getValue();
                            StringBuilder str = new StringBuilder();
                            str.append("NowException:\n");
                            for (MachineFault machineFault : exceptionInfos) {
                                str.append(machineFault.toString()).append("\n");
                            }
                            str.append("\nmachineBehavior: ");
                            for (UInt8Prop uInt8Prop : machineBehaviorStates) {
                                str.append(uInt8Prop.getValue().toString()).append(" ");
                            }
                            mTvshow.setText(str.toString());
                        }, LogHelper::log);
                break;
            case 5:
                subscribe = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().restartMachine()
                        .flatMap(responseStructureObservable -> Observable.timer(10, TimeUnit.SECONDS))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseStructure -> mApp.restart(), LogHelper::log);

                break;
            case 6:
                goBluetoothInterfaceTest();
                break;
            case 7:
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupFlag(true);
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetup3DP(true);
                ServiceContainer.getInstance().getService(IAppService.class).restart();
                break;
            case 8:
                replaceFragment(R.id.fcv_debug, ScreenSaverSettingsFragment.newInstance());
                break;
            default:
                mTvshow.setText("啥玩意 " + newCameraModelId);
        }
    }

    private void goBluetoothInterfaceTest() {
        replaceFragment(R.id.fcv_debug, BTDebugFragment.newInstance());
    }

    @OnClick(R2.id.top_bar_back)
    public void onBack() {
        finish();
    }

    @OnClick(R2.id.btn_temp_factory)
    void onClickFactory() {
        Logger.d("Closing USB and SerialPort... prepare to start factory app.");
//        ServiceContainer.getInstance().getService(IFileManagerService.class).closeFabUsbDevices();
        ServiceContainer.getInstance().getService(IMachine.class).getConnectionController().forceCloseConnection();
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName comp = new ComponentName("com.ido.qcomtest", "com.ido.qcomtest.MainActivity");
        mIntent.setComponent(comp);
        mIntent.setAction("android.intent.action.VIEW");
        startActivity(mIntent);
        ServiceContainer.getInstance().getService(IAppService.class).restart();
    }


    private void saveJpg(Bitmap cropped, String name) {
        try {
            File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getFilesDir(), name);
            FileOutputStream out = new FileOutputStream(file);
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            Logger.d("saveJpg: " + e);
        }
    }

    private File copyPrintFile() {
        InputStream is = getResources().openRawResource(R.raw.a400_xy);
        File file = null;
        try {
            file = new File(getCacheDir().getAbsoluteFile() + "/calibrationXY.gcode");
            if (file.exists()) {
                file.delete();
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
        } catch (Exception e) {
            file = null;
            LogHelper.log(e);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {

            }
        }
        return file;
    }

//    @OnClick(R2.id.run_cmd)
//    void onRunCmdClick() {
//        Logger.d("Uninstalling updating...");
//        FabPackageManager.uninstall(getApplicationContext(), "com.snapmaker.updating", () -> {
//            Logger.d("Old updating uninstalled, installing new updating...");
//            FabPackageManager.install(getApplicationContext(), getResources().openRawResource(R.raw.fabscreen_updating_1_8));
//        });
//    }
//
//    @OnClick(R2.id.run_cmd1)
//    void onRunCmd1Click() {
//        FabPackageManager.uninstall(getApplicationContext(), "com.snapmaker.updating", () -> {
//
//        });
//    }
}
