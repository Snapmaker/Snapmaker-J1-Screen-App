package fabscreen.platform.base.service;

import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState;

import android.content.Intent;

import java.io.File;
import java.util.List;

import fabscreen.platform.base.lib.network.AccessPoint;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface INetwork {

    boolean isWifiEnabled();

    Single<Boolean> setWifiEnabled(boolean enabled);

    void startScan();

    void stopScan();

    boolean connect(AccessPoint accessPoint);

    @Deprecated
    void setAPConnectStateAndRefresh(String ssid, ConnectState state);

    ConnectState getAPConnectState(String SSID);

    @Deprecated
    boolean isAuthenticatingError(Intent intent);

    void removeOrDisableWifiConfig(String SSID);

    void removeOrDisableAllWifi();

    @Deprecated
    void dispose();// TODO: 2022/1/13 remove?

    @Deprecated
    void savePasswordToFile(String wifiInfoJson, File file);

    @Deprecated
    String readWifiInfoFromFile(File file);


    AccessPoint getActiveAccessPointImmediately();

    Observable<AccessPoint> getActiveNetworkObservable();

    Observable<Intent> watchSupplicantStateChange();

    AccessPoint getConnectingOrConnectedAP();

    Observable<List<AccessPoint>> watchAccessPointList();

    String getMacAddress();
}
