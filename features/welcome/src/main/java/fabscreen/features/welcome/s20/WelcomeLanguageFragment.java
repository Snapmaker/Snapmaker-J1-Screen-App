package fabscreen.features.welcome.s20;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.ILanguage;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class WelcomeLanguageFragment extends BaseFragment {
    @BindView(R2.id.btn_language_english)
    Button mBtnLanguageEnglish;
    @BindView(R2.id.btn_language_german)
    Button mBtnLanguageGerman;
    @BindView(R2.id.btn_language_simplified_chinese)
    Button mBtnLanguageSimplifiedChinese;
    @BindView(R2.id.btn_language_french)
    Button mBtnLanguageFrench;
    @BindView(R2.id.btn_language_japanese)
    Button mBtnLanguageJapanese;
    @BindView(R2.id.btn_language_korean)
    Button mBtnLanguageKorean;
    @BindView(R2.id.btn_language_italian)
    Button mBtnLanguageItalian;
    private List<Button> mButtons;
    private BehaviorSubject<Integer> mSelectedLanguageSubject = BehaviorSubject.createDefault(0);

    public static WelcomeLanguageFragment newInstance() {
        return new WelcomeLanguageFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButtons = new ArrayList<>();
        mButtons.add(mBtnLanguageEnglish);
        mButtons.add(mBtnLanguageGerman);
        mButtons.add(mBtnLanguageSimplifiedChinese);
        mButtons.add(mBtnLanguageFrench);
        mButtons.add(mBtnLanguageJapanese);
        mButtons.add(mBtnLanguageKorean);
        mButtons.add(mBtnLanguageItalian);

        // init button events
        for (int i = 0; i < mButtons.size(); i++) {
            int position = i;
            Button button = mButtons.get(position);
            button.setOnClickListener(v -> {
                mSelectedLanguageSubject.onNext(position);
            });
        }

        mSelectedLanguageSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(language -> {
                    for (int i = 0; i < mButtons.size(); i++) {
                        mButtons.get(i).setSelected(i == language);
                    }
                });

        mSelectedLanguageSubject.onNext(ServiceContainer.getInstance().getService(ILanguage.class).getCurrentLanguage());

        // hide language buttons if copywriting is not ready.
        mBtnLanguageItalian.setVisibility(Button.GONE);
        mBtnLanguageKorean.setVisibility(Button.GONE);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_language;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_welcome_set_language_next)
    void onClickNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(ILanguage.class).setLanguage(getContext(), mSelectedLanguageSubject.getValue());
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupLanguage(true);
        if ("com.snapmaker.fabscreen".equals(getActivity().getApplication().getPackageName())) {
//            ServiceContainer.getInstance().getService(IRouter.class).routeToFabscreenHome().start(getContext(), Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
//            ServiceContainer.getInstance().getService(IRouter.class).routeToS30Home().start(getContext(), Intent.FLAG_ACTIVITY_NEW_TASK);
        }

    }
}
