package fabscreen.platform.core.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.legacy.remote.SessionManager;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.view.FabFullScreenDialog;
import fabscreen.platform.base.view.FabScreenDialog;
import fabscreen.platform.base.view.ToastHelper;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

// TODO: temporary class
// enter/leave methods should be moved to Router
// listens should be moved to FabApplication?
public class ViewSub {
    FabScreenDialog fabFullScreenDialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Context mContext;
    private boolean mHomeFlag = false;

    public ViewSub() {


        listenAlways();

    }

    @Nullable
    public Context getContext() {
        return mContext;
    }

    @Nullable
    private Resources getResources() {
        if (mContext == null) {
            return null;
        }
        return mContext.getResources();
    }

    void listenAlways() {
        Disposable sub;

        sub = MachineStatusManager.getConnectedStatus().getObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connected -> {
                    if (!connected) {
                        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEmergencyStopAvailable)
                            return;
                        // Lost connection
                        Logger.w("Loss connection from machine.");
                        FabFullScreenDialog.create(getContext())
                                .setIcon(R.drawable.pic_dialog_warning_72x72)
                                .setTitle(R.string.all_warning)
                                .setMessage(R.string.warning_machine_not_responding)
                                .setPositive(R.string.all_reconnect, (dialog, which) -> {
                                    // Reconnect
                                    Logger.i("Try to reconnect machine...");
                                    // TODO:
                                    ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController().reconnect();
                                    dialog.dismiss();
                                })
                                .setNegative(R.string.all_no, (dialog, which) -> {
                                    // Do nothing (to be defined)
                                    Logger.i("Reconnect has been canceled.");
                                    dialog.dismiss();
                                })
                                .show();
                    }
                });
        compositeDisposable.add(sub);

        sub = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getRemoteStateObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(remoteState -> {
                    if (remoteState == SessionManager.State.STATE_ACTIVE && !ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getRemotePageFlag()) {
                        ServiceContainer.getInstance().getService(IRouter.class).routeToRemotePage().start(mContext);
                    }
                }, LogHelper::log);
        compositeDisposable.add(sub);

        sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineInfo -> {
                    if (machineInfo.isEmergencyStopAvailable) {
                        Logger.d("Emergency Stop button triggered.");
                        ServiceContainer.getInstance().getService(IRouter.class).routeToMain().start(getContext(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }
                });
        compositeDisposable.add(sub);
    }

    public void enter(Context context) {
        mContext = context;
    }

    public void leave(Context context) {
        // do nothing
        // mContext = null;
    }

    public boolean getHomeFlag() {
        return mHomeFlag;
    }

    public void setHomeFlag(boolean flag) {
        mHomeFlag = flag;
    }

    public void setObservableUSBAttach(Observable<Boolean> fileManagerStateObservable) {
        Disposable disposable = fileManagerStateObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(isAttach -> {
                    new ToastHelper.Builder()
                            .setDrawable(isAttach ? R.drawable.ic_usb_detected : R.drawable.ic_usb_unplugged)
                            .setMessage(isAttach ? R.string.toast_usb_device_detected : R.string.toast_usb_device_unpuggled)
                            .build()
                            .showToast(mContext);
                });
    }

    public void listenerHeaderSecurityStatus(Observable<SSTPPacketContent.HeaderSecurity> headerSecurityObservable) {
        Disposable sub = headerSecurityObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(headerSecurity -> {
                    Logger.d(headerSecurity);
                    Context context = getContext();
                    // watch out context not ready
                    if (context == null) return;
                    // TODO: 2022/1/17 refactor :  do we need to set this anymore?
//                    ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().setLaser10WErrorState(headerSecurity.status);
                    if (fabFullScreenDialog == null || !fabFullScreenDialog.isAlive()) {
                        fabFullScreenDialog = FabScreenDialog.create(context);
                    }

                    if ((headerSecurity.status & SSTPPacketContent.HeaderSecurity.HEADER_ROLL_ABNORMAL_ANGLE) != 0) {
                        fabFullScreenDialog.setIcon(R.drawable.pic_dialog_warning_72x72)
                                .setTitle(R.string.laser_10W_dialog_not_placed_correctly)
                                .setDescription(R.string.laser_10W_dialog_not_placed_correctly_desc)
                                .setConfirm(R.string.all_ok, (dialog, which) -> {
                                    dialog.dismiss();
                                });
                        fabFullScreenDialog.show();
                    } else if ((headerSecurity.status & SSTPPacketContent.HeaderSecurity.HEADER_TEMPERATURE_ANOMALY) != 0) {
                        fabFullScreenDialog.setIcon(R.drawable.pic_dialog_warning_72x72)
                                .setTitle(R.string.laser_10W_dialog_temperature_too_high)
                                .setDescription(R.string.laser_10W_dialog_temperature_too_high_desc)
                                .setConfirm(R.string.all_ok, (dialog, which) -> {
                                    dialog.dismiss();
                                });
                        fabFullScreenDialog.show();
                    } else {
                        fabFullScreenDialog.dismiss();
                        fabFullScreenDialog = null;
                    }
                });
        compositeDisposable.add(sub);
    }
}
