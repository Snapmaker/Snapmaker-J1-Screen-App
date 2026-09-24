package fabscreen.features.settings.a350.about;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.Constants;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.WelcomeNameViewModel;
import fabscreen.platform.base.view.FabConfirm;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SettingsAboutFragment extends BaseFragment {
    @BindView(R2.id.iv_settings_about_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_settings_about_product_name)
    TextView mTvProduct;
    @BindView(R2.id.tv_about_product_name)
    TextView mTvProductName;
    @BindView(R2.id.tv_about_product_size)
    TextView mTvProductSize;
    @BindView(R2.id.tv_about_update_package_version)
    TextView mTvUpdatePackageVersion;
    @BindView(R2.id.tv_about_app_version)
    TextView mTvApplicationVersion;
    @BindView(R2.id.tv_about_controller_version)
    TextView mTvControllerVersion;
    @BindView(R2.id.tv_about_ip_address)
    TextView mTvIPAddress;
    private WelcomeNameViewModel mViewModel;
    private int mCoverClicks = 0;

    public static SettingsAboutFragment getInstance() {
        return new SettingsAboutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.settings_about_machine);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_about;
    }

    @Override
    protected WelcomeNameViewModel getViewModel() {
        return getViewModelProvider().get(WelcomeNameViewModel.class);
    }

    private void initView() {
        String machineName = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineName();
        mTvProduct.setText(machineName);

        mViewModel.getNameObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(name -> {
                    mTvProduct.setText(name);
                });

        int machineModel = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId;
        float x = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float y = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float z = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        switch (machineModel) {
            case Constants.MACHINE_MODEL_SNAPMAKER_A150: {
                mIvCover.setImageResource(R.drawable.pic_machine_model_a150_320x160);
                mTvProductName.setText(R.string.settings_snapmaker2_a150);
                break;
            }
            case Constants.MACHINE_MODEL_SNAPMAKER_A250: {
                mIvCover.setImageResource(R.drawable.pic_machine_model_a250_320x160);
                mTvProductName.setText(R.string.settings_snapmaker2_a250);
                break;
            }
            case Constants.MACHINE_MODEL_SNAPMAKER_A350: {
                mIvCover.setImageResource(R.drawable.pic_machine_model_a350_320x160);
                mTvProductName.setText(R.string.settings_snapmaker2_a350);
                break;
            }
            default:
                break;
        }
        mTvProductSize.setText(String.format("%s × %s × %s mm", x, y, z));

        // Package version
        String packageVersion = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLastUpdatePackageVersion();
        Logger.d("Package version %s", packageVersion);
        mTvUpdatePackageVersion.setText(packageVersion);

        // Controller version
        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    Logger.d("Controller version %s", machineInfo.controllerFWVersion);
                    mTvControllerVersion.setText(machineInfo.controllerFWVersion);
                }, LogHelper::log);

        if (getContext() != null) {
            mTvApplicationVersion.setText(BaseApplication.getInstance().getAppVersionName());
            Logger.d("Touch screen version %s", BaseApplication.getInstance().getAppVersionName());
        }

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
                            addressString = sAddr;
                            mTvIPAddress.setText(addressString);
                        }
                    }
                }

                if (addressString.isEmpty()) {
                    mTvIPAddress.setText(R.string.settings_network_not_connected);
                }
            }
        } catch (SocketException e) {
            LogHelper.log(e);
        }
    }

    @OnClick(R2.id.btn_settings_about_change_name)
    void onClickChangeName() {
        playNormalClickSound();
        AboutActivity activity = (AboutActivity) getActivity();
        if (activity != null) {
            activity.gotoChangeName();
        }
    }

    @OnClick(R2.id.iv_settings_about_cover)
    void onClickCover() {
        playNormalClickSound();
        mCoverClicks++;

        if (mCoverClicks % 5 == 0) {
            FabConfirm.create(getContext())
                    .setDescription(R.string.experiment_developer_mode_notice)
                    .setConfirm(R.string.all_ok, (dialog, which) -> {
                        Logger.i("Enter developer mode.");
                        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setDebugFlag(true);
                        dialog.dismiss();
                    })
                    .show();
        } else {
            Logger.i("Exit developer mode.");
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setDebugFlag(false);
        }
    }
}
