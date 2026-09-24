package fabscreen.platform.core.ui.viewmodel;

import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.lib.network.AccessPoint.ConnectState;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState.CONFIRMED;
import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState.CONNECTED;
import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState.CONNECTING;
import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState.IDLE;
import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState.SELECTED;

public class WifiConnectionViewModel extends BaseViewModel {
    private static final String WIFI_INFO_FILE_NAME = "wifi_info";
    private File mWifiInfoFile;
    private BehaviorSubject<List<AccessPoint>> mAccessPointsSubject = BehaviorSubject.createDefault(new ArrayList<>());
    private INetwork mNetworkController;
    private String mConnectingSSID;
    private boolean mIsUserSelectedConnecting;
    private Disposable mTimeoutWatcherDisposable;
    private JSONObject mSavedWifiInfo;
    private AccessPoint mUserConfirmedAP;
    private BehaviorSubject<SearchState> mSearchStateSubject = BehaviorSubject.createDefault(SearchState.IDLE);
    private PublishSubject<NetworkController.ConnectResult> mConnectResultSubject = PublishSubject.create();
    private AccessPoint mSelectedAccessPoint = null;
    private BehaviorSubject<String> mPasswordSubject = BehaviorSubject.createDefault("");
    private BehaviorSubject<PasswordTip> mPasswordTipSubject = BehaviorSubject.createDefault(PasswordTip.TIP_EMPTY);
    private PublishSubject<Boolean> mConnectEventSubject = PublishSubject.create();
    private BehaviorSubject<Boolean> mConnectWifiSubject = BehaviorSubject.createDefault(false);
    private final IMachine mMachine;
    private final IPreferences mPreferences;

    public WifiConnectionViewModel() {
        super();
        mMachine = getServiceContainer().getService(IMachine.class);
        mNetworkController = ServiceContainer.getInstance().getService(INetwork.class);
        mPreferences = ServiceContainer.getInstance().getService(IPreferences.class);

        watchAPList();
        watchSupplicantStateChange();
        pushPasswordInputTips();
        initSavedWifiInfo();
    }

    private void initSavedWifiInfo() {
        mWifiInfoFile = new File(ServiceContainer.getInstance().getService(IAppService.class).getDataDir(), WIFI_INFO_FILE_NAME);
        String wifiInfoJson = ServiceContainer.getInstance().getService(INetwork.class).readWifiInfoFromFile(mWifiInfoFile);
        mSavedWifiInfo = getWifiInfoFromJson(wifiInfoJson);
    }

    private void pushPasswordInputTips() {
        mPasswordSubject
                .map(password -> {
                    if (password.isEmpty()) {
                        return PasswordTip.TIP_EMPTY;
                    }
                    if (password.length() < 8) {
                        return PasswordTip.TIP_TOO_SHORT;
                    }
                    return PasswordTip.TIP_OK;
                })
                .as(bindToLifecycle())
                .subscribe(tip -> mPasswordTipSubject.onNext(tip));
    }

    private void watchSupplicantStateChange() {
        mNetworkController.watchSupplicantStateChange()
                .as(bindToLifecycle())
                .subscribe(this::onSupplicantChange, LogHelper::log);
    }

    private void watchAPList() {
        mNetworkController.watchAccessPointList()
                .as(bindToLifecycle())
                .subscribe(accessPoints -> {
                    mAccessPointsSubject.onNext(accessPoints);

                    if (accessPoints.isEmpty()) {
                        mSearchStateSubject.onNext(SearchState.SEARCH_DONE_EMPTY);
                    } else {
                        mSearchStateSubject.onNext(SearchState.SEARCH_DONE);
                    }
                });
    }

    private JSONObject getWifiInfoFromJson(String wifiInfoJson) {
        try {
            mSavedWifiInfo = new JSONObject(wifiInfoJson);
            return mSavedWifiInfo;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public String getSelectedPassword() {
        if (getSelected() != null) {
            return getPasswordBySsid(getSelected().getSSID());
        }
        return "";
    }

    private String getPasswordBySsid(String ssid) {
        String password = mSavedWifiInfo.optString(ssid);
        return TextUtils.isEmpty(password) ? "" : password;
    }

    private void savePassword(String ssid, String password) {
        // Do nothing if password has not changed.
        if (getPasswordBySsid(ssid).equals(password)) return;
        // Save password to mWifiObject then write to file.
        try {
            mSavedWifiInfo.put(ssid, password);
            Logger.d("Saving current Wi-Fi info config...");
            ServiceContainer.getInstance().getService(INetwork.class).savePasswordToFile(mSavedWifiInfo.toString(), mWifiInfoFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removePassword(String ssid) {
        try {
            mSavedWifiInfo.remove(ssid);
            ServiceContainer.getInstance().getService(INetwork.class).savePasswordToFile(mSavedWifiInfo.toString(), mWifiInfoFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * System broadcast received. Detail info is in the intent extra.
     *
     * @param intent Where Wi-Fi connect info( state, supplicant error, etc.) stored in.
     */
    private void onSupplicantChange(Intent intent) {
        if (intent == null) return;
        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        if (state == null) return;
        switch (state) {
            case ASSOCIATING:
            case ASSOCIATED:
                mConnectingSSID = mNetworkController.getConnectingOrConnectedAP().getSSID();
                ConnectState lastState = mNetworkController.getAPConnectState(mConnectingSSID);
                // The state is already CONNECTING, no need to set again. When do a connect, one or
                // both of ASSOCIATING and ASSOCIATE will broadcast, we only need one of them.
                if (lastState == ConnectState.CONNECTING) break;

                // If state is [USER_CONFIRMED_CONNECT] before connecting, we can mark this connect as userSelected.
                mIsUserSelectedConnecting = lastState == ConnectState.CONFIRMED;
                mNetworkController.setAPConnectStateAndRefresh(mConnectingSSID, ConnectState.CONNECTING);
                break;

            case DISCONNECTED:
                // We should distinguish user-selected and system-selected APs.
                // If this connect is userSelected, we should show toast, otherwise only state
                // will be set, no toast will be shown.
                // A connect has its lifecycle: IDLE -> SELECTED -> CONNECTING -> CONNECTED/IDLE
                // Once a connect arrives at the end of its lifecycle([DISCONNECTED/COMPLETE(IDLE/CONNECTED)]),
                // this connect is no longer userSelected, unless select again.
                ConnectState apLastState = mNetworkController.getAPConnectState(mConnectingSSID);
                if (apLastState != ConnectState.CONNECTING) return;
                boolean isAuthenticatingError = mNetworkController.isAuthenticatingError(intent);
                if (mIsUserSelectedConnecting) {
                    // We only care the last CONFIRMED AP.
                    if (mUserConfirmedAP == null) return;
                    if (mConnectingSSID != null && mConnectingSSID.equals(mUserConfirmedAP.getSSID())) {
                        if (isAuthenticatingError) {
                            mNetworkController.removeOrDisableWifiConfig(mConnectingSSID);
                            mConnectResultSubject.onNext(NetworkController.ConnectResult.FAIL_WRONG_PASSWORD);
                        } else {
                            mConnectResultSubject.onNext(NetworkController.ConnectResult.FAIL_OTHER);
                        }
                    }
                    disposeTimeoutWatcher();
                } else {
                    if (isAuthenticatingError) {
                        mNetworkController.removeOrDisableWifiConfig(mConnectingSSID);
                    }
                }
                removePassword(mConnectingSSID);
                // Refresh list AFTER notify result.
                mNetworkController.setAPConnectStateAndRefresh(mConnectingSSID, ConnectState.IDLE);
                mConnectingSSID = null;
                mIsUserSelectedConnecting = false;
                break;

            case COMPLETED:
                String connectedSSID = mNetworkController.getConnectingOrConnectedAP().getSSID();
                mNetworkController.setAPConnectStateAndRefresh(connectedSSID, ConnectState.CONNECTED);
                if (mIsUserSelectedConnecting) {
                    mConnectResultSubject.onNext(NetworkController.ConnectResult.SUCCESS);
                    disposeTimeoutWatcher();
                }
                mConnectingSSID = null;
                mIsUserSelectedConnecting = false;
                mPreferences.getHelper().setLastConnectedAPSSID(connectedSSID);
                mPreferences.getHelper().setLastConnectedAPPwd(getPasswordBySsid(connectedSSID));
                break;
        }
    }

    private void disposeTimeoutWatcher() {
        if (mTimeoutWatcherDisposable != null && !mTimeoutWatcherDisposable.isDisposed()) {
            mTimeoutWatcherDisposable.dispose();
        }
    }

    /**
     * Get network (Wi-Fi) enable state.
     */
    public boolean isWifiEnabled() {
        boolean wifiEnabled = ServiceContainer.getInstance().getService(INetwork.class).isWifiEnabled();
        if (!wifiEnabled) {
            mAccessPointsSubject.onNext(Collections.emptyList());
            mSearchStateSubject.onNext(SearchState.OFF);
        }
        return wifiEnabled;
    }

    /**
     * Enable Wi-Fi.
     * <p>
     * Applications must have the {@link android.Manifest.permission#CHANGE_WIFI_STATE}
     * permission to toggle wifi.
     *
     * @return a Single indicates whether the operation is successful.
     */
    public Single<Boolean> enableWiFi() {
        mSearchStateSubject.onNext(SearchState.IDLE);
        return ServiceContainer.getInstance().getService(INetwork.class).setWifiEnabled(true);
    }

    /**
     * Disable Wi-Fi.
     * <p>
     * Applications must have the {@link android.Manifest.permission#CHANGE_WIFI_STATE}
     * permission to toggle wifi.
     *
     * @return a Single indicates whether the operation is successful.
     */
    public Single<Boolean> disableWiFi() {
        mAccessPointsSubject.onNext(Collections.emptyList());
        mSearchStateSubject.onNext(SearchState.OFF);
        return ServiceContainer.getInstance().getService(INetwork.class).setWifiEnabled(false);
    }

    /**
     * Start scanning network.
     */
    public void startScanNetwork() {
        mSearchStateSubject.onNext(SearchState.SEARCHING);
        ServiceContainer.getInstance().getService(INetwork.class).startScan();
    }

    /**
     * refresh data
     */
    public void refreshData() {
        mSearchStateSubject.onNext(SearchState.REFRESH_DATA);
        ServiceContainer.getInstance().getService(INetwork.class).startScan();
    }


    /**
     * Stop scanning network.
     */
    public void stopScanNetwork() {
        mSearchStateSubject.onNext(SearchState.IDLE);
        ServiceContainer.getInstance().getService(INetwork.class).stopScan();
    }

    /**
     * Get all access points.
     */
    public Observable<List<AccessPoint>> getAPListObservable() {
        return mAccessPointsSubject.map(this::arrangeAPList);
    }

    /**
     * Arrange AP list to put the un-IDLE one at first place.
     */
    @NonNull
    private List<AccessPoint> arrangeAPList(List<AccessPoint> accessPoints) {
        List<AccessPoint> tempList = new ArrayList<>();
        for (AccessPoint ap : accessPoints) {
            if (ap.getConnectState() != IDLE) {
                tempList.add(0, ap);
                if (ap.getConnectState() == CONNECTED) {
                    mConnectWifiSubject.onNext(true);
                }
            } else {
                tempList.add(ap);
            }
        }
        return tempList;
    }

    /**
     * Get all available access points.
     * <p>
     * If access point is selected, then we consider it being not available for selecting.
     */
    public Observable<List<AccessPoint>> getAvailableAccessPointsObservable() {
        return Observable.combineLatest(
                getAPListObservable(),
                getNotIdleAccessPointObservable(),
                (accessPoints, accessPoint) -> {
                    List<AccessPoint> newList = new ArrayList<>();

                    for (AccessPoint accessPoint1 : accessPoints) {
                        if (!accessPoint1.getSSID().equals(accessPoint.getSSID())) {
                            newList.add(accessPoint1);
                        }
                    }

                    return newList;
                });
    }

    public Observable<SearchState> getSearchStateObservable() {
        return mSearchStateSubject.hide();
    }

    public Observable<PasswordTip> getPasswordTipObservable() {
        return mPasswordTipSubject.hide();
    }

    /**
     * Get selected access point.
     *
     * @return {AccessPoint}
     */
    public AccessPoint getSelected() {
        return mSelectedAccessPoint;
    }

    /**
     * Triggered by a user click.
     */
    public void setSelected(AccessPoint accessPoint) {
        // Back from password input page, AP'll be null.
        Logger.d("set selected ap: %s", accessPoint);
        String SSID = null;
        mSelectedAccessPoint = accessPoint;
        if (mSelectedAccessPoint != null) {
            SSID = mSelectedAccessPoint.getSSID();
        }
        mNetworkController.setAPConnectStateAndRefresh(SSID, ConnectState.SELECTED);
    }

    public void setPassword(String password) {
        mPasswordSubject.onNext(password);
    }

    public Observable<Boolean> getConnectEventObservable() {
        return mConnectEventSubject.hide();
    }

    /**
     * Use this function to notify another fragment to connect.
     */
    public void notifyConnect() {
        mConnectEventSubject.onNext(true);
    }

    /**
     * Connect to selected access point.
     *
     * @return {@code true} if the operation succeeded.
     */
    public boolean connect() {
        Logger.d("connect ap, %s", mSelectedAccessPoint);
        // "connect" clicked, selectedAP is being enabled and wait for CONNECTING/CONNECTED state.
        mNetworkController.setAPConnectStateAndRefresh(mSelectedAccessPoint.getSSID(), ConnectState.CONFIRMED);
        mUserConfirmedAP = mSelectedAccessPoint;
        watchForConnectTimeout(mUserConfirmedAP);
        if (mUserConfirmedAP.isEncrypted()) {
            String password = mPasswordSubject.getValue();
            mUserConfirmedAP.setPassword(password);
            savePassword(mUserConfirmedAP.getSSID(), password);
        }
        return ServiceContainer.getInstance().getService(INetwork.class).connect(mUserConfirmedAP);
    }

    /**
     * If an AP stay in SELECTED/CONNECTING state for more than 20s, the connect is timeout.
     * If connect() is triggered again, the watcher is setup again.
     * If system reconnect the AP, and in the 20th second it's still connecting, the connect will
     * be ignored.
     *
     * @param accessPoint for whom the connect will be watched.
     */
    private void watchForConnectTimeout(AccessPoint accessPoint) {
        disposeTimeoutWatcher();
        mTimeoutWatcherDisposable = Observable.timer(20, TimeUnit.SECONDS)
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    if (accessPoint == null) return;
                    ConnectState status = accessPoint.getConnectState();
                    if (status != ConnectState.CONNECTED && status != ConnectState.IDLE) {
                        mNetworkController.setAPConnectStateAndRefresh(accessPoint.getSSID(), ConnectState.IDLE);
                        mConnectResultSubject.onNext(NetworkController.ConnectResult.FAIL_TIMEOUT);
                    }
                }, LogHelper::log);
    }

    /**
     * Check if any access point is connected.
     *
     * @return {@code true} if connected.
     */
    public boolean isConnected() {
        return mConnectWifiSubject.getValue();
    }

    public Observable<AccessPoint> getNotIdleAccessPointObservable() {
        return mAccessPointsSubject.flatMap(accessPoints -> {
            AccessPoint selectedAP = null;
            AccessPoint userConfirmedAP = null;
            AccessPoint connectingAP = null;
            AccessPoint connectedAP = null;

            for (AccessPoint accessPoint : accessPoints) {
                ConnectState status = accessPoint.getConnectState();
                if (status == SELECTED) selectedAP = accessPoint;
                if (status == CONFIRMED) userConfirmedAP = accessPoint;
                if (status == CONNECTING) connectingAP = accessPoint;
                if (status == CONNECTED) connectedAP = accessPoint;
            }
            // visibility priority: SELECTED >USER_CONFIRMED_CONNECT > CONNECTING > CONNECTED.
            // DO NOT CHANGE THE ORDER OF THE BELOW CODES!
            if (selectedAP != null) return Observable.just(selectedAP);
            if (userConfirmedAP != null) return Observable.just(userConfirmedAP);
            if (connectingAP != null) return Observable.just(connectingAP);
            if (connectedAP != null) return Observable.just(connectedAP);
            // No SELECTED/CONNECTING/CONNECTED AP was found.
            return Observable.just(AccessPoint.NULL_ACCESS_POINT);
        });
    }

    public Observable<NetworkController.ConnectResult> getConnectResultObservable() {
        return mConnectResultSubject.hide();
    }

    public void switchWifi(boolean on) {
        if (on) {
            enableWiFi().toObservable()
                    .as(bindToLifecycle())
                    .subscribe(result -> startScanNetwork());
        } else {
            disableWiFi().toObservable()
                    .as(bindToLifecycle())
                    .subscribe(/*result -> stopScanNetwork()*/);
        }
    }

    public int getSeries() {
        return mMachine.getMachineInfoSubjectHolder().getValue().seriesId;
    }

    public boolean isJ1() {
        return mMachine.getMachineInfoSubjectHolder().getValue().productId == IMachine.Product.J1;
    }

    public Observable<Boolean> getWifiConnectObservable() {
        return mConnectWifiSubject.hide();
    }

    // Search state
    public enum SearchState {
        OFF,
        IDLE,
        SEARCHING,
        SEARCH_DONE,
        SEARCH_DONE_EMPTY,
        REFRESH_DATA
    }

    // Password
    public enum PasswordTip {
        TIP_EMPTY,
        TIP_TOO_SHORT,
        TIP_OK
    }
}
