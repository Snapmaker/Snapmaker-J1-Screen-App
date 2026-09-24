package fabscreen.features.settings.a350.language;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.ILanguage;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.MultiLanguageManager;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class SettingsLanguageFragment extends BaseFragment {
    @BindView(R2.id.btn_settings_language_save)
    Button mBtnSave;
    @BindView(R2.id.lv_settings_language)
    ListView mLvLanguageList;
    private SettingsLanguageAdapter mAdapter;
    private ArrayList<LanguageItem> mLanguages;

    public static SettingsLanguageFragment getInstance() {
        return new SettingsLanguageFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_language);
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_language;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    void initView() {
        disableSave();

        // init languages
        mLanguages = new ArrayList<>();
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_SIMPLIFIED_CHINESE, getString(R.string.setting_language_simplified_chinese), getString(R.string.setting_center_language_simplified_chinese_native_spell)));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_DEFAULT, getString(R.string.setting_language_english), getString(R.string.setting_center_language_english_native_spell)));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_GERMAN, getString(R.string.setting_language_german), getString(R.string.setting_center_language_german_native_spell)));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_FRENCH, getString(R.string.setting_language_french), getString(R.string.setting_center_language_french_native_spell)));
        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_JAPANESE, getString(R.string.setting_language_japanese), getString(R.string.setting_center_language_japanese_native_spell)));
//        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_KOREAN, "한국어"));
//        mLanguages.add(new LanguageItem(MultiLanguageManager.LANGUAGE_ITALIAN, "Italiano"));

        int currentLanguage = ServiceContainer.getInstance().getService(ILanguage.class).getCurrentLanguage();
        mAdapter = new SettingsLanguageAdapter(getContext());
        mAdapter.setOnSelectedLanguageListener(item -> {
            if (item.getLanguage() == currentLanguage) {
                disableSave();
            } else {
                enableSave();
            }

            // update list view
            setSelectedLanguage(item.getLanguage());
            mAdapter.notifyDataSetChanged();
        });

        mAdapter.setItems(mLanguages);
        mLvLanguageList.setAdapter(mAdapter);

        // set current language selected
        setSelectedLanguage(currentLanguage);
        mAdapter.notifyDataSetChanged();
    }

    void disableSave() {
        if (getContext() != null) {
            mBtnSave.setTextColor(ContextCompat.getColor(getContext(), R.color.custom_grey_600));
        }
        mBtnSave.setEnabled(false);
    }

    void enableSave() {
        if (getContext() != null) {
            mBtnSave.setTextColor(ContextCompat.getColor(getContext(), R.color.custom_blue_400));
        }
        mBtnSave.setEnabled(true);
    }

    int getSelectedLanguage() {
        if (mLanguages == null) return -1;
        int selectedLanguage = MultiLanguageManager.LANGUAGE_UNKNOWN;

        for (LanguageItem l : mLanguages) {
            if (l.isSelected()) {
                selectedLanguage = l.getLanguage();
                return selectedLanguage;
            }
        }
        return selectedLanguage;
    }

    void setSelectedLanguage(int language) {
        if (mLanguages == null) return;

        for (LanguageItem l : mLanguages) {
            l.setSelected(l.getLanguage() == language);
        }
    }

    @OnClick(R2.id.btn_settings_language_save)
    void onClickSave() {
        playNormalClickSound();
        int selectedLanguage = getSelectedLanguage();
        if (selectedLanguage != MultiLanguageManager.LANGUAGE_UNKNOWN) {
            ServiceContainer.getInstance().getService(IAppService.class).getMultiLanguageManager().setLanguage(getContext(), selectedLanguage);
            // TODO: Can we change language without restarting the whole app?
            ServiceContainer.getInstance().getService(IRouter.class).routeToHome().start(requireContext(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }

    public interface OnLanguageSelectedListener {
        void onSelectedItem(LanguageItem item);
    }

    public static class LanguageItem {
        private int language;
        private String languageName;
        private String centerLanguageName;
        private boolean isSelected;

        public LanguageItem(int language, String languageName, String centerLanguageName) {
            this.language = language;
            this.languageName = languageName;
            this.isSelected = false;
            this.centerLanguageName = centerLanguageName;
        }

        public int getLanguage() {
            return language;
        }

        public String getCenterLanguageName() {
            return centerLanguageName;
        }

        public String getLanguageName() {
            return languageName;
        }


        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

    }

    static class SettingsLanguageAdapter extends BaseAdapter {
        private Context mContext;
        private List<LanguageItem> mItems;
        private OnLanguageSelectedListener mListener;

        public SettingsLanguageAdapter(Context context) {
            mContext = context;
        }

        public void setOnSelectedLanguageListener(OnLanguageSelectedListener listener) {
            mListener = listener;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public LanguageItem getItem(int position) {
            return mItems.get(position);
        }

        public void setItems(ArrayList<LanguageItem> items) {
            mItems = items;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_settings_language, parent, false);
            }
            LanguageItem item = getItem(position);

            Button btnLanguage = convertView.findViewById(R.id.btn_settings_language_name);
            TextView tvCenterLanguage = convertView.findViewById(R.id.tv_center_settings_language_name);
            ImageView ivSelected = convertView.findViewById(R.id.iv_settings_language_selected);

            btnLanguage.setText(item.getLanguageName());
            tvCenterLanguage.setText(item.getCenterLanguageName());

            btnLanguage.setOnClickListener(v -> mListener.onSelectedItem(item));

            ivSelected.setVisibility(item.isSelected() ? ImageView.VISIBLE : ImageView.GONE);

            return convertView;
        }
    }
}
