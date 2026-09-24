package fabscreen.features.welcome.j1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.MultiLanguageManager;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.BaseFragment;

public class WelcomeJ1LanguageFragment extends BaseFragment {

    @BindView(R2.id.top_bar_back)
    Button mBtnback;
    @BindView(R2.id.rv_welcome_j1_language)
    RecyclerView mRvLanguage;

    private WelcomeLanguageAdapter mLanguageAdapter;
    private List<LanguageItem> mLanguages;
    private int mSelectLanguage = MultiLanguageManager.LANGUAGE_SIMPLIFIED_CHINESE;

    private ArrayList<Locale> mLanguageItem;

    public static Fragment newInstance() {
        return new WelcomeJ1LanguageFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_j1_language;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {
        mLanguages = new ArrayList<>();
        mLanguageItem = new ArrayList<>();

        mLanguageItem.add(Locale.SIMPLIFIED_CHINESE);
        mLanguageItem.add(Locale.ENGLISH);
        mLanguageItem.add(Locale.JAPANESE);
        mLanguageItem.add(Locale.GERMAN);

        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_SIMPLIFIED_CHINESE, "简体中文"));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_DEFAULT, "English"));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_JAPANESE, "日本語"));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_GERMAN, "Deutsch"));
//        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_FRENCH, getString(R.string.setting_language_Français)));
//        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_JAPANESE, getString(R.string.setting_language_japanese)));
        mLanguageAdapter = new WelcomeLanguageAdapter(mLanguages, requireContext());
        mRvLanguage.setAdapter(mLanguageAdapter);
        Locale currentLanguage = ((BaseActivity) requireActivity()).getCurrentLanguage();
        int index = 0;
        for (int i = 0; i < mLanguageItem.size(); i++) {
            if (mLanguageItem.get(i).equals(currentLanguage)) {
                index = i;
            }
        }
        mRvLanguage.setLayoutManager(new LinearLayoutManager(requireContext()));
        mLanguageAdapter.setOnSectionSelectedListener(position -> {
            mLanguageAdapter.setSelectPosition(position);
            mSelectLanguage = mLanguages.get(position).getLanguage();
            ((BaseActivity) requireActivity()).setLanguage(mLanguageItem.get(position));
        });
        mLanguageAdapter.setSelectPosition(index);
    }

    @OnClick(R2.id.btn_next)
    void onStartClicked() {
        playNormalClickSound();
//        ServiceContainer.getInstance().getService(ILanguage.class).setLanguage(getContext(), mSelectLanguage);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupLanguage(true);
        ((WelcomeJ1Activity) requireActivity()).goToTerms();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBtnback.setVisibility(getServiceContainer().getService(IPreferences.class).getHelper().getMachineSetup3DP() ? View.VISIBLE : View.INVISIBLE);
    }
}
