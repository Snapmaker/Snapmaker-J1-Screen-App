package fabscreen.features.settings.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;
import fabscreen.platform.core.ui.viewmodel.WifiConnectionViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class SettingsWifiFragment extends BaseFragment {
    @BindView(R2.id.switch_wifi)
    SwitchCompat mSwitchWifi;
    @BindView(R2.id.rv_ap_list)
    RecyclerView mRvApList;

    protected WifiConnectionViewModel mViewModel;
    private final List<AccessPoint> mApList = new ArrayList<>();
    private boolean wifiEnabled;

    private final ActivityResultLauncher<String> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initView();
                }
            });

    private APListAdapter mAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(WifiConnectionViewModel.class);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mRvApList.scrollToPosition(0);
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initView();
        } else {
            requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initView() {
        initSwitch();
        initAPList();

        mViewModel.getSearchStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(searchState -> {
                    switch (searchState) {
                        case OFF:
                            mAdapter.setSearchOff(true);
                            break;
                        case SEARCHING:
                            // show progress
                            mAdapter.setShowEmpty(false);
                            mAdapter.setShowScanning(true);
//                            mRvApList.setVisibility(View.GONE);
                            break;
                        case IDLE:
                            // no break here is intended
                            mAdapter.setSearchOff(false);
                        case SEARCH_DONE:
                            // show result
                            mAdapter.setShowEmpty(false);
                            mAdapter.setShowScanning(false);
                            break;

                        case SEARCH_DONE_EMPTY:
                            // show empty
                            mAdapter.setShowScanning(false);
                            mAdapter.setShowEmpty(true);
                            break;
                    }
                });

        mViewModel.getConnectResultObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handleConnectResult, LogHelper::log);
    }

    private void handleConnectResult(NetworkController.ConnectResult result) {
        if (result != NetworkController.ConnectResult.SUCCESS) {
            new SuperToastHelper.Builder()
                    .setDrawable(R.drawable.icon_tips_error_80x80)
                    .setMessage(getString(result == NetworkController.ConnectResult.FAIL_WRONG_PASSWORD ? R.string.all_wifi_dialog_connect_failed_wrong_password : R.string.all_wifi_dialog_connect_failed))
                    .build()
                    .showToast(requireContext());
        }
    }

    private void initAPList() {
        mAdapter = getAPListAdapter(mApList);
        mRvApList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRvApList.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ap -> {
            playNormalClickSound();
            mViewModel.setSelected(ap);
            if (ap.isEncrypted()) {
                goPassword(ap);
            } else {
                mViewModel.connect();
                mRvApList.scrollToPosition(0);
            }
        });

        mViewModel.getAPListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aps -> {
                    mApList.clear();
                    mApList.addAll(aps);
                    mAdapter.notifyDataSetChanged();
                }, LogHelper::log);
    }

    protected abstract APListAdapter getAPListAdapter(List<AccessPoint> list);

    protected abstract void goPassword(AccessPoint ap);

    @Override
    public void onResume() {
        super.onResume();
        requestPermission();
    }

    private void initSwitch() {
        wifiEnabled = mViewModel.isWifiEnabled();
        mSwitchWifi.setChecked(wifiEnabled);
        if (wifiEnabled) {
            mViewModel.startScanNetwork();
        }
        mSwitchWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playSwitchSound();
            mViewModel.switchWifi(isChecked);
        });
    }
}
