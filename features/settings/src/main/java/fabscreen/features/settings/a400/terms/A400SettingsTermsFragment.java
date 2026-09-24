package fabscreen.features.settings.a400.terms;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a400.A400SettingsActivity;
import fabscreen.platform.base.view.BaseFragment;

public class A400SettingsTermsFragment extends BaseFragment {

    @BindView(R2.id.checkBox)
    CheckBox mCheckBox;

    public static A400SettingsTermsFragment newInstance() {
        return new A400SettingsTermsFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_settings_security;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Terms and Conditions");

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playNormalClickSound();
            }
        });
    }

    @OnClick(R2.id.tv_experience_program)
    void onExperienceProgramClick() {
        playNormalClickSound();
        if (requireActivity() instanceof A400SettingsActivity) {
            ((A400SettingsActivity) requireActivity()).goToLongTextDisplay(
                    R.string.settings_terms_experience_improvement_page_title,
                    R.string.settings_terms_experience_improvement_page_content
            );
        }
    }


}
