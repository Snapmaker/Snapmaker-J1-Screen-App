package fabscreen.platform.base.lib.network;

import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState;
import static fabscreen.platform.base.lib.network.AccessPoint.NULL_ACCESS_POINT;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Keep;

import com.orhanobut.logger.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Actually, NetworkController is not a lib class
 * Low Priority
 */

@Deprecated
public class NetworkController implements INetwork, IServiceIdentifier {

    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Context mContext;
    private NetworkManager mNetworkManager;
    private BehaviorSubject<AccessPoint> mActiveAccessPointSubject = BehaviorSubject.createDefault(NULL_ACCESS_POINT);
    private List<AccessPoint> mAllAPs = new ArrayList<>();
    private BehaviorSubject<List<AccessPoint>> mAllAPsSubject = BehaviorSubject.create();
    private final IPreferences mPreferences;

    @Keep
    public NetworkController() {
        mContext = ServiceContainer.getInstance().getService(IAppService.class).getAppContext();
        mPreferences = ServiceContainer.getInstance().getService(IPreferences.class);
        mNetworkManager = new NetworkManager(mContext);
        checkNetworkPeriodically();
        watchAPListChanges();
        startScan();
    }

    private void connectLastConnectedIfNotConnectedToWiFi() {
        if (getActiveAccessPointImmediately() == NULL_ACCESS_POINT) {
            String lastConnectedApSSID = mPreferences.getHelper().getLastConnectedApSSID();
            String lastConnectedApPwd = mPreferences.getHelper().getLastConnectedApPwd();
            if (lastConnectedApSSID.isEmpty()) return;
            for (AccessPoint ap : mAllAPs) {
                if (lastConnectedApSSID.equals(ap.getSSID())) {
                    if (ap.isEncrypted()) {
                        ap.setPassword(lastConnectedApPwd);
                    }
                    Logger.d("fab wifi connecting to last connected: %1$s, %2$s", lastConnectedApSSID, lastConnectedApPwd);
                    connect(ap);
                    break;
                }
            }
        }
    }

    private void watchAPListChanges() {
        mDisposables.add(mNetworkManager.watchAccessPointList()
                .subscribe(APList -> {
                    List<AccessPoint> newAccessPoints = new ArrayList<>();
                    for (AccessPoint newAP : APList) {
                        if (newAP.getSSID().isEmpty()) {
                            continue;
                        }
                        for (AccessPoint oldAP : mAllAPs) {
                            // temporary fix for connected ap not show at top, need refactor wifi.
                            if (newAP.getConnectState() == ConnectState.CONNECTED) {
                                continue;
                            }

                            if (newAP.getSSID().equals(oldAP.getSSID()) && oldAP.getConnectState() != ConnectState.CONNECTED) {
                                newAP.setConnectState(oldAP.getConnectState());
                            }

                        }
                        newAccessPoints.add(newAP);
                    }
                    mAllAPs.clear();
                    mAllAPs.addAll(newAccessPoints);
                    mAllAPsSubject.onNext(mAllAPs);
                    connectLastConnectedIfNotConnectedToWiFi();
                }, LogHelper::log));
    }

    private void checkNetworkPeriodically() {
        mDisposables.add(Observable.interval(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> checkNetworkStatus(), LogHelper::log));
    }

    public Observable<Intent> watchSupplicantStateChange() {
        return mNetworkManager.getSupplicantStateObservable();
    }

    public void dispose() {
        mDisposables.dispose();
    }

    /**
     * Get network (Wi-Fi) enable state.
     */
    public boolean isWifiEnabled() {
        return mNetworkManager.isWifiEnabled();
    }

    /**
     * Enable or disable Wi-Fi.
     * <p>
     * Applications must have the {@link android.Manifest.permission#CHANGE_WIFI_STATE}
     * permission to toggle wifi.
     *
     * @return a Single indicates whether the operation is successful.
     */
    public Single<Boolean> setWifiEnabled(boolean enabled) {
        return mNetworkManager.setWifiEnabled(enabled);
    }

    public AccessPoint getActiveAccessPointImmediately() {
        return mNetworkManager.getActiveAccessPoint(mContext);
    }

    /**
     * Only use for home wifi status display.
     */
    public Observable<AccessPoint> getActiveNetworkObservable() {
        return mActiveAccessPointSubject.distinctUntilChanged();
    }

    public AccessPoint getConnectingOrConnectedAP() {
        return mNetworkManager.getConnectingOrConnectedAP();
    }

    private boolean same(AccessPoint a, AccessPoint b) {
        return (a == NULL_ACCESS_POINT && b == NULL_ACCESS_POINT)
                || (a != NULL_ACCESS_POINT && b != NULL_ACCESS_POINT && a.getSSID().equals(b.getSSID()));
    }

    private void checkNetworkStatus() {
        if (!this.isWifiEnabled()) {
            // Home wifi status need to be refreshed when wifi disabled.
            mActiveAccessPointSubject.onNext(NULL_ACCESS_POINT);
            return;
        }

        final AccessPoint activeAccessPoint = mNetworkManager.getActiveAccessPoint(mContext);
        if (!same(activeAccessPoint, mActiveAccessPointSubject.getValue())) {
            mActiveAccessPointSubject.onNext(activeAccessPoint);
        }
    }

    public Observable<List<AccessPoint>> watchAccessPointList() {
        return mAllAPsSubject.hide();
    }

    /**
     * Try connect to specified access point.
     *
     * @return {@code true} if the operation succeeded.
     */
    public boolean connect(AccessPoint accessPoint) {
        return mNetworkManager.connectAccessPoint(accessPoint);
    }

    public void startScan() {
        mNetworkManager.startScan(mContext);
    }

    public void stopScan() {
        mNetworkManager.stopScan(mContext);
    }

    /**
     * The target AP(with the specific SSID)'s connect state will be set to the given state.
     * By default, if SSID is null, APs with given state will be set to IDLE, but when forceOthersIdle
     * is true, all APs will be set to IDLE.
     *
     * @param SSID  The target AP's SSID.
     * @param state The given state.
     */
    public void setAPConnectStateAndRefresh(String SSID, ConnectState state) {
        for (int i = 0; i < mAllAPs.size(); i++) {
            AccessPoint tempAP = mAllAPs.get(i);
            if (SSID == null) {
                if (tempAP.getConnectState() != ConnectState.IDLE) {
                    if (tempAP.getConnectState() == state) {
                        tempAP.setConnectState(ConnectState.IDLE);
                    }
                }
            } else {
                if (SSID.equals(tempAP.getSSID())) {
                    tempAP.setConnectState(state);
                } else {
                    // An AP is confirmed to connect, all other APs should go IDLE.
                    if (tempAP.getConnectState() == state || state == ConnectState.CONFIRMED) {
                        tempAP.setConnectState(ConnectState.IDLE);
                    }
                }
            }
        }
        mAllAPsSubject.onNext(mAllAPs);
    }

    public ConnectState getAPConnectState(String SSID) {
        if (SSID == null) return null;
        for (int i = 0; i < mAllAPs.size(); i++) {
            AccessPoint tempAP = mAllAPs.get(i);
            if (SSID.equals(tempAP.getSSID())) {
                return tempAP.getConnectState();
            }
        }
        return null;
    }

    public boolean isAuthenticatingError(Intent intent) {
        return mNetworkManager.isAuthenticatingError(intent);
    }

    public void removeOrDisableWifiConfig(String SSID) {
        mNetworkManager.removeOrDisableConfigBySSID(SSID);
    }

    public void removeOrDisableAllWifi() {
        mNetworkManager.removeAllConfiguration();
    }

    /**
     * Save key-value pairs in a json file.
     */
    public void savePasswordToFile(String jsonInfo, File file) {
        try {
            FileUtils.writeByteArrayToFile(file, jsonInfo.getBytes());
        } catch (IOException e) {
            LogHelper.log(e);
        }
    }

    /**
     * Get password from local file by ssid.
     *
     * @return Json string stores ssid-password pairs.
     */
    public String readWifiInfoFromFile(File file) {
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                LogHelper.log(e);
            }
            return "";
        }
    }

    @Override
    public String getMacAddress() {
        return mNetworkManager.getMacAddress();
    }

    public enum ConnectResult {
        SUCCESS,
        FAIL_WRONG_PASSWORD,
        FAIL_TIMEOUT,
        FAIL_OTHER
    }
}
