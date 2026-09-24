package fabscreen.features.settings.j1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import fabscreen.features.settings.R;
import fabscreen.features.settings.common.SettingsTermsFragment;
import fabscreen.features.settings.j1.attendance.J1SettingsAttendanceFragment;
import fabscreen.features.settings.language.S30SettingsLanguageFragment;
import fabscreen.features.settings.wifi.J1SettingsWifiFragment;
import fabscreen.platform.core.ui.common.leftsection.J1LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;

public class J1SettingsContainerFragment extends SectionAndDetailContainerFragment {

    private int mDestination;
    private J1SettingsFirmwareUpdateFragment firmwareUpdateFragment;

    public static Fragment newInstance(int destination) {
        Fragment fragment = new J1SettingsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("destination", destination);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        mDestination = requireArguments().getInt("destination");
        List<SectionItem> items = new ArrayList<>();
        firmwareUpdateFragment = (J1SettingsFirmwareUpdateFragment) J1SettingsFirmwareUpdateFragment.newInstance(mDestination == SectionAndDetailContainerFragment.SETTINGS_FIRMWARE_AND_DOWN );
        items.add(new SectionItem(requireContext(), R.string.settings_firmware_update,
                firmwareUpdateFragment));
        items.add(new SectionItem("Wi-Fi", J1SettingsWifiFragment.newInstance()));
        items.add(new SectionItem(requireContext(), R.string.all_language, S30SettingsLanguageFragment.newInstance()));
        items.add(new SectionItem(requireContext(), R.string.all_about, J1SettingsAboutFragment.newInstance()));
        items.add(new SectionItem(requireContext(), R.string.j1_setting_machine_settings, J1SettingsAttendanceFragment.newInstance()));
        items.add(new SectionItem(requireContext(), R.string.all_terms_and_conditions, SettingsTermsFragment.newInstance()));
        return items;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_section_and_detail_container;
    }

    @Override
    protected LeftSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new J1LeftSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return "Settings";
    }


    @Override
    protected void setSelection(int selection) {
        super.setSelection(selection);
    }

    @Override
    protected int getDefaultSelection() {
        switch (requireArguments().getInt("destination")) {
            case SETTINGS_WIFI:
                return 1;
            case SETTINGS_LANGUAGE:
                return 2;
            case SETTINGS_ABOUT:
                return 3;
            case SETTINGS_ATTENDANCE:
                return 4;
            case SETTINGS_TERMS:
                return 5;
            default:
                return 0;
        }
    }
}
