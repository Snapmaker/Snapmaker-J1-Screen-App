package fabscreen.features.settings.a400;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a400.about.A400SettingsAboutMachineFragment;
import fabscreen.features.settings.a400.maintenance.index.A400SettingsMaintenanceFragment;
import fabscreen.features.settings.a400.moduleassistant.A400ModuleAssistantFragment;
import fabscreen.features.settings.a400.remote.A400SettingsRemoteFragment;
import fabscreen.features.settings.a400.terms.A400SettingsTermsFragment;
import fabscreen.features.settings.a400.update.A400SettingsFirmwareUpdateFragment;
import fabscreen.features.settings.language.S30SettingsLanguageFragment;
import fabscreen.features.settings.wifi.A400SettingsWifiFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.common.leftsection.A400LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;

public class A400SettingsContainerFragment extends SectionAndDetailContainerFragment {

    @BindView(R2.id.tap_bar_developer)
    Button mBtnDeveloper;

    private long mTime = 0;
    private int mTouchCount = 0;
    private boolean isDeveloper;

    public static Fragment newInstance(int destination) {
        A400SettingsContainerFragment containerFragment = new A400SettingsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("destination", destination);
        containerFragment.setArguments(bundle);
        return containerFragment;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_section_and_detail_container;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnDeveloper.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        List<SectionItem> items = new ArrayList<>();
        items.add(new SectionItem("Wi-Fi", R.drawable.select_a400_wifi, A400SettingsWifiFragment.newInstance()));
        items.add(new SectionItem("Language", R.drawable.select_a400_language, S30SettingsLanguageFragment.newInstance()));
        items.add(new SectionItem("Firmware Update", R.drawable.select_a400_update, A400SettingsFirmwareUpdateFragment.newInstance()));
        items.add(new SectionItem("Module Assistant", R.drawable.select_a400_replace_model, A400ModuleAssistantFragment.newInstance()));
        items.add(new SectionItem("About Machine", R.drawable.select_a400_about_machine, A400SettingsAboutMachineFragment.newInstance()));
        items.add(new SectionItem(getString(R.string.setting_remote_connection), R.drawable.select_a400_remote, A400SettingsRemoteFragment.newInstance()));
        items.add(new SectionItem("Maintenance", R.drawable.select_a400_maintenance, A400SettingsMaintenanceFragment.newInstance()));
        items.add(new SectionItem("Terms and Conditions", R.drawable.select_a400_security, A400SettingsTermsFragment.newInstance()));
        return items;
    }

    @Override
    protected LeftSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new A400LeftSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return "Settings";
    }

    @Override
    protected int getDefaultSelection() {
        switch (requireArguments().getInt("destination")) {
            case SETTINGS_ABOUT:
                return 4;
            case SETTINGS_MODULE_ASSISTANT:
                return 3;
            default:
                return 0;
        }
    }

    //Tap five times in a row to open developer or close developer
    @OnClick(R2.id.tap_bar_developer)
    public void onClickDeveloper() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - mTime < 500) {
            mTouchCount += 1;
        } else {
            mTouchCount = 1;
        }
        mTime = currentTime;
        if (mTouchCount >= 5) {
            isDeveloper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineDeveloper();

            DecisionDialog.create(requireContext())
                    .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                    .setType(DecisionDialog.TIP_TYPE)
                    .setContent(isDeveloper ? "关闭开发者模式?" : "开启开发者模式？")
                    .setCanceledOnTouchOutSide(true)
                    .setFirstTv(isDeveloper ? "关闭" : "开启", R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineDeveloper(!isDeveloper);
                            if (requireActivity() instanceof A400SettingsActivity) {
                                ((A400SettingsActivity) requireActivity()).setDeveloperState(!isDeveloper);
                            }
                        }
                    }).show();
        }
    }


}
