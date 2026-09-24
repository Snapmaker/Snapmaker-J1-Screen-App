package fabscreen.features.welcome.s20;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.core.ui.viewmodel.WifiConnectionViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static fabscreen.platform.base.lib.network.NetworkController.ConnectResult.FAIL_WRONG_PASSWORD;

public class WelcomeWifiFragment extends BaseFragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    @BindView(R2.id.lv_welcome_wifi_access_point_list)
    ListView mLvAccessPointListView;
    @BindView(R2.id.view_welcome_wifi_searching)
    View mViewSearching;
    @BindView(R2.id.view_welcome_wifi_no_found)
    View mViewNoFound;
    private WifiConnectionViewModel mViewModel;
    private WelcomeWifiListAdapter mListAdapter;
    private AlertDialog mConnectingDialog;

    public static WelcomeWifiFragment newInstance() {
        return new WelcomeWifiFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideConnectingDialog();
        // stop scanning once activity stopped
        mViewModel.stopScanNetwork();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_wifi;
    }

    @Override
    protected WifiConnectionViewModel getViewModel() {
        return getViewModelProvider().get(WifiConnectionViewModel.class);
    }

    private void initView() {
        // Init access point list
        mListAdapter = new WelcomeWifiListAdapter();
        mListAdapter.setOnItemClickListener(accessPoint -> {
            if (accessPoint.isEncrypted()) {
                mViewModel.setSelected(accessPoint);
                if (getActivity() != null) {
                    ((WelcomeActivity) getActivity()).startPasswordFragment();
                }
            } else {
                mViewModel.setSelected(accessPoint);
                connect();
            }
        });

        mLvAccessPointListView.setAdapter(mListAdapter);
        mLvAccessPointListView.setOnItemClickListener(mListAdapter);

        // state -> view (list, searching, no found)
        mViewModel.getSearchStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(searchState -> {
                    switch (searchState) {
                        case SEARCHING:
                            mLvAccessPointListView.setVisibility(View.INVISIBLE);
                            mViewSearching.setVisibility(View.VISIBLE);
                            mViewNoFound.setVisibility(View.INVISIBLE);
                            break;
                        case SEARCH_DONE:
                            mLvAccessPointListView.setVisibility(View.VISIBLE);
                            mViewSearching.setVisibility(View.INVISIBLE);
                            mViewNoFound.setVisibility(View.INVISIBLE);
                            break;
                        case SEARCH_DONE_EMPTY:
                            mLvAccessPointListView.setVisibility(View.INVISIBLE);
                            mViewSearching.setVisibility(View.INVISIBLE);
                            mViewNoFound.setVisibility(View.VISIBLE);
                            break;
                        default:
                            mLvAccessPointListView.setVisibility(View.INVISIBLE);
                            mViewSearching.setVisibility(View.INVISIBLE);
                            mViewNoFound.setVisibility(View.INVISIBLE);
                    }
                });

        // listen on access points changes
        mViewModel.getAPListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(accessPoints -> {
                    mListAdapter.setAccessPoints(accessPoints);
                    mListAdapter.notifyDataSetChanged();
                });

        // bind connect event
        mViewModel.getConnectEventObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(connectEvent -> connect());

        mViewModel.getConnectResultObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    hideConnectingDialog();
                    switch (result) {
                        case SUCCESS:
                            showConnectedDialog();
                            break;
                        case FAIL_WRONG_PASSWORD:
                        case FAIL_TIMEOUT:
                        case FAIL_OTHER:
                            showConnectFailedDialog(result);
                            break;
                    }
                });

        mViewModel.enableWiFi();
        startScanNetwork();
    }

    /**
     * Check Wi-Fi permission and then start scan network.
     */
    private void startScanNetwork() {
        if (getContext() == null) {
            return;
        }

        // Check coarse location permission
        if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If COARSE Location permission is granted, just start scan network.
            mViewModel.startScanNetwork();
        } else {
            // Otherwise request permission first, and then start scanning when permission is granted.
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Stop scanning network.
     */
    private void stopScanNetwork() {
        mViewModel.stopScanNetwork();
    }

    /**
     * Start connecting to selected access point.
     */
    private void connect() {
        if (mViewModel.getSelected() != null) {
            showConnectingDialog();

            mViewModel.connect();
        }
    }

    private void showConnectingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_primary);
            dialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_welcome_wifi_connecting, null);
        dialog.setView(view);
        dialog.show();

        mConnectingDialog = dialog;
    }

    private void hideConnectingDialog() {
        if (mConnectingDialog == null) return;
        if (mConnectingDialog.isShowing()) {
            mConnectingDialog.dismiss();
            mConnectingDialog = null;
        }
    }

    private void showConnectedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_welcome_wifi_connected, null);
        dialog.setView(view);
        dialog.show();

        AndroidSchedulers.mainThread().scheduleDirect(() -> {
            dialog.dismiss();
            this.cleanupAndExit();
        }, 3000, TimeUnit.MILLISECONDS);
    }

    private void showConnectFailedDialog(NetworkController.ConnectResult result) {

        new SuperToastHelper.Builder()
                .setDrawable(R.drawable.ic_pic_a400_error_112x112)
                .setMessage(getString(result == FAIL_WRONG_PASSWORD ? R.string.all_wifi_dialog_connect_failed_wrong_password : R.string.all_wifi_dialog_connect_failed))
                .build()
                .showToast(requireContext());
    }

    /**
     * Cleanup network resources and exit.
     */
    private void cleanupAndExit() {
        stopScanNetwork();

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupFlag(true);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @OnClick(R2.id.btn_welcome_wifi_skip)
    void onClickSkip() {
        playNormalClickSound();
        FabConfirm.create(getContext())
                .setDescription(R.string.welcome_wifi_skip_notice)
                .setConfirm(R.string.all_yes, (dialog, which) -> {
                    Logger.i("Skipping network configuration, finish machine setup.");
                    dialog.dismiss();

                    cleanupAndExit();
                })
                .setCancel(R.string.all_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mViewModel.startScanNetwork();
        }
    }
}
