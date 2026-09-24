package fabscreen.features.welcome.a400;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.features.welcome.s20.WelcomeLanguageFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.ILanguage;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class A400WelcomeLanguageFragment extends BaseFragment {
    @BindView(R2.id.rv_language)
    RecyclerView mRvLanguage;
    private List<LanguageItem> mLanguageList;
    private A400LanguageAdapter mLanguageAdapter;
    private int mSelectPosition = 0;


    public static A400WelcomeLanguageFragment newInstance() {
        return new A400WelcomeLanguageFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Locale currentLanguage = ((BaseActivity) requireActivity()).getCurrentLanguage();
        mLanguageList = new ArrayList<>();
        mLanguageList.add(new LanguageItem(Locale.SIMPLIFIED_CHINESE, "中文"));
        mLanguageList.add(new LanguageItem(Locale.ENGLISH, "English"));
//        mLanguageList.add("Deutsch");
//        mLanguageList.add("日本語");
//        mLanguageList.add("Français");
//        mLanguageList.add("Español");
//        mLanguageList.add("한국어");
//        mLanguageList.add("Italiano");
//        mLanguageList.add("");
//        mLanguageList.add("Pycckий");
//        mLanguageList.add("Українська");
//        mLanguageList.add("");
        mLanguageAdapter = new A400LanguageAdapter(mLanguageList);
        mRvLanguage.setAdapter(mLanguageAdapter);
        mRvLanguage.setLayoutManager(new GridLayoutManager(requireContext(), mLanguageList.size() >= 4 ? 4 : mLanguageList.size()));
        for (int i = 0; i < mLanguageList.size(); i++) {
            if (mLanguageList.get(i).getLocale().equals(currentLanguage)) {
                mSelectPosition = i;
            }
        }
        mLanguageAdapter.selectPosition(mSelectPosition);
        mLanguageAdapter.setOnItemClickListener(new A400LanguageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                playNormalClickSound();
                mSelectPosition = position;
                mLanguageAdapter.selectPosition(position);
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupLanguage(true);
                ((BaseActivity) requireActivity()).setLanguage(mLanguageList.get(mSelectPosition).getLocale());
            }
        });

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_welcome_language;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_welcome_set_language_next)
    void onClickNext() {
        playNormalClickSound();

        if (getActivity() != null) {
            ((A400WelcomeActivity) getActivity()).startHelloFragment();
        }
    }
}