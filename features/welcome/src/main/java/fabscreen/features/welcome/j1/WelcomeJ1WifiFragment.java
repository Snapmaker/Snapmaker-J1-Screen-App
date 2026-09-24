package fabscreen.features.welcome.j1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;
import fabscreen.platform.core.ui.common.wifi.adapter.J1APListAdapter;
import fabscreen.platform.core.ui.viewmodel.WifiConnectionViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class WelcomeJ1WifiFragment extends BaseFragment {
    @BindView(R2.id.switch_wifi)
    SwitchCompat mSwitchWifi;
    @BindView(R2.id.rv_ap_list)
    RecyclerView mRvApList;
    @BindView(R2.id.progress_welcome_wifi)
    CircularProgressIndicator mProgress;
    @BindView(R2.id.tv_wifi_sencond_title)
    TextView mTvSecondTitle;
    @BindView(R2.id.tv_welcome_j1_wifi_skip)
    TextView mTvSkip;

    private WifiConnectionViewModel mViewModel;
    private final List<AccessPoint> mApList = new ArrayList<>();
    private Timer mTimer;
    private TimerTask mTask;
    private APListAdapter mAdapter;

    private final ActivityResultLauncher<String> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initView();
                }
            });

    public static Fragment newInstance() {
        return new WelcomeJ1WifiFragment();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        mProgress.setVisibility(View.GONE);
        mTvSecondTitle.setVisibility(View.GONE);

    }

    @Override
    public void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initView();
        } else {
            requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }


    @SuppressLint({"NotifyDataSetChanged", "AutoDispose"})
    private void initView() {

        mViewModel.getWifiConnectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(connect -> {
                    mTvSkip.setText(connect ? R.string.all_next : R.string.all_skip);
                }, LogHelper::log);

        //用户在WiFi页面关了wifi进来这里，这边要强制打开wifi
        boolean wifiEnabled = mViewModel.isWifiEnabled();
        if (!wifiEnabled) {
            mViewModel.switchWifi(true);
        }

        mViewModel.startScanNetwork();

        if (mTimer == null) {
            mTimer = new Timer();
            mTask = new TimerTask() {
                @Override
                public void run() {
                    mViewModel.startScanNetwork();
                }
            };

            mTimer.schedule(mTask, 10000, 10000);
        }

        mAdapter = new J1APListAdapter(mApList);
        mRvApList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRvApList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ap -> {
            playNormalClickSound();
            mViewModel.setSelected(ap);
            if (ap.isEncrypted()) {
                if (requireActivity() instanceof WelcomeJ1Activity) {
                    ((WelcomeJ1Activity) requireActivity()).goToEnterPassword(ap.getSSID());
                }
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

    @Override
    protected WifiConnectionViewModel getViewModel() {
        return getActivityScopeViewModel(WifiConnectionViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_j1_wifi;
    }

    @OnClick(R2.id.tv_welcome_j1_wifi_skip)
    void onClickSkip() {
        playNormalClickSound();
        if (!mViewModel.isConnected()) {
            DecisionDialog.create(getActivity())
                    .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                    .setContent(R.string.j1_welcome_dailog_skip_wifi_content)
                    .setType(DecisionDialog.TIP_TYPE)
                    .setContentColor(R.color.palette_grey_french)
                    .setFirstTv(R.string.all_cancel, R.color.select_dialog_grey_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setSecondTv(R.string.all_skip, R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            destroyTimer();
                            ((WelcomeJ1Activity) requireActivity()).goToComplete();
                        }
                    }).show();
        } else {
            ((WelcomeJ1Activity) requireActivity()).goToComplete();
            destroyTimer();
        }
    }

    public void destroyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    @Override
    protected void back() {
        playNormalClickSound();
        destroyTimer();
        if (getActivity() != null) {
            Logger.d("Route: Back from " + getClass().getSimpleName());
            getActivity().onBackPressed();
        }
    }

    @OnClick(R2.id.top_bar_back)
    void onClickBack() {
        playNormalClickSound();
        back();
    }
}
