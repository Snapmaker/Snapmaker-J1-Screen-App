package fabscreen.features.settings.a400.moduleassistant.replacehotend;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.base.BaseProgressFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ReplaceHotendProcessFragment extends BaseProgressFragment {

    private ReplaceHotendViewModel mViewModel;

    public static Fragment newInstance() {
        return new ReplaceHotendProcessFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_replace_hotend_process;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceHotendViewModel.class);
        initView();
    }

    private void initView() {
        showSetHeatingTemp();
        mViewModel.getReplaceProcessObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshReplaceView, LogHelper::log);
    }

    private void refreshReplaceView(ReplaceHotendViewModel.ReplaceProcess process) {
        switch (process) {
            case ON_HEATING_START:
                showHeating();
                break;
            case ON_HEATED:
                showUnloading();
                break;
            case ON_NOZZLE_CLEARED:
                showCoolingDown();
                break;
            case ON_NOZZLE_COOLED:
                showDoReplace();
                break;
            case ON_RESTART_BEGIN:
                showRestarting();
                break;
            case ON_SUCCESS:
                goComplete();
                break;
        }
    }

    @OnClick(R2.id.iv_close)
    void onCloseClicked() {
        playNormalClickSound();
        DecisionDialog.create(requireContext())
                .setDialogStatus(2, true, false, true, false)
                .setType(DecisionDialog.WARMING_TYPE)
                .setPic(R.drawable.ic_yellow_warn)
                .setTitle("Stop Replacement")
                .setContent("The nozzle replacement is not completed yet. Do you want to stop it?")
                .setFirstTv("Cancel", R.color.select_dialog_left_text_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv("Stop", R.color.select_dialog_yellow_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requireActivity().finish();
                    }
                })
                .show();
    }

    private void goComplete() {
        if (requireActivity() instanceof ReplaceHotendActivity) {
            ((ReplaceHotendActivity) requireActivity()).goToComplete();
        }
    }

    private void showSetHeatingTemp() {
        setMainTitle("Replace Hot End");
        setSubTitle("Set Nozzle Heating Temp.(1/6)");
        setProgress(1, 6);
        Bundle bundle = new Bundle();
        bundle.putInt(ReplaceHotendTempDashboardFragment.KEY_OPERATION, ReplaceHotendTempDashboardFragment.SET_TEMP);
        replaceFragment(ReplaceHotendTempDashboardFragment.class, bundle);
    }

    private void showHeating() {
        setSubTitle("Heat Nozzle (2/6)");
        setProgress(2, 6);
        replaceFragment(ReplaceHotendHeatingFragment.class, null);
    }

    private void showUnloading() {
        setSubTitle("Manual Unloading (3/6)");
        setProgress(3, 6);
        Bundle bundle = new Bundle();
        bundle.putInt(ReplaceInProgressFragment.KEY_OPERATION, ReplaceInProgressFragment.UNLOADING);
        replaceFragment(ReplaceInProgressFragment.class, bundle);
    }

    private void showCoolingDown() {
        setSubTitle("Nozzle Cooldown (4/6)");
        setProgress(4, 6);
        Bundle bundle = new Bundle();
        bundle.putInt(ReplaceHotendTempDashboardFragment.KEY_OPERATION, ReplaceHotendTempDashboardFragment.COOLING_DOWN);
        replaceFragment(ReplaceHotendTempDashboardFragment.class, bundle);
    }

    private void showDoReplace() {
        setSubTitle("Replace Hot End (5/6)");
        setProgress(5, 6);
        Bundle bundle = new Bundle();
        bundle.putInt(CommonIntroFragment.KEY_OPERATION, CommonIntroFragment.REPLACE);
        replaceFragment(CommonIntroFragment.class, bundle);
    }

    private void showRestarting() {
        setSubTitle("Initialize (6/6)");
        setProgress(6, 6);
        Bundle bundle = new Bundle();
        bundle.putInt(ReplaceInProgressFragment.KEY_OPERATION, ReplaceInProgressFragment.RESTARTING);
        replaceFragment(ReplaceInProgressFragment.class, bundle);
    }

    private void replaceFragment(Class<? extends BaseFragment> fragmentClass, Bundle args) {
        getChildFragmentManager().beginTransaction().replace(R.id.fcv_replace_process, fragmentClass, args).commit();
    }
}
