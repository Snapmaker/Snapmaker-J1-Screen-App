package fabscreen.features.settings.common;

import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;

public class SettingsTermsFragment extends BaseFragment {

    public static SettingsTermsFragment newInstance() {
        return new SettingsTermsFragment();
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_security;
    }

    @OnClick(R2.id.tv_experience_program)
    void onExperienceProgramClick() {
        playNormalClickSound();
        ExperienceProgramDialogFragment.newInstance(R.string.j1_settings_user_experience_program, R.string.j1_settings_user_experience_program_content, true)
                .show(getChildFragmentManager(), "experience");
    }
}
