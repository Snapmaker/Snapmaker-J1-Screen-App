package fabscreen.features.settings.language;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a400.A400SettingsActivity;
import fabscreen.features.settings.j1.J1SettingsActivity;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.BaseFragment;

public class S30SettingsLanguageFragment extends BaseFragment {
    @BindView(R2.id.lv_settings_language)
    ListView mLvLanguageList;
    @BindView(R2.id.view_settings_top_bar)
    RelativeLayout mRlTopBar;
    private SettingsLanguageAdapter mAdapter;
    private ArrayList<LanguageItem> mLanguages;

    public static S30SettingsLanguageFragment newInstance() {
        return new S30SettingsLanguageFragment();
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
        return R.layout.fragment_s30_settings_language;
    }

    void initView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mLvLanguageList.getLayoutParams();
        params.topMargin = requireActivity() instanceof A400SettingsActivity ? 0 : (int) DimensUtils.dp2px(12);
        mLvLanguageList.setDividerHeight(requireActivity() instanceof A400SettingsActivity ? (int) DimensUtils.dp2px(16) : 0);
        mRlTopBar.setVisibility(requireActivity() instanceof A400SettingsActivity ? View.VISIBLE : View.GONE);
        // init languages
        mLanguages = new ArrayList<>();
        mLanguages.add(new LanguageItem(Locale.SIMPLIFIED_CHINESE, R.string.setting_language_simplified_chinese, R.string.setting_center_language_simplified_chinese_native_spell));
        mLanguages.add(new LanguageItem(Locale.ENGLISH, R.string.setting_language_english, R.string.setting_center_language_english_native_spell));
        mLanguages.add(new LanguageItem(Locale.JAPANESE, R.string.setting_language_japanese, R.string.setting_center_language_japanese_native_spell));
        mLanguages.add(new LanguageItem(Locale.GERMAN, R.string.setting_language_german, R.string.setting_center_language_german_native_spell));

//        mLanguages.add(new LanguageItem(Locale.FRENCH, R.string.setting_language_Français, R.string.setting_center_clanguage_Français));

        Locale currentLanguage = ((BaseActivity) requireActivity()).getCurrentLanguage();
        mAdapter = new SettingsLanguageAdapter(requireActivity());

        mAdapter.setOnLanguageItemClickListener(item -> {
            if (!item.locale.equals(currentLanguage)) {
                playNormalClickSound();
                setSelectedLanguage(item.locale);
                ((BaseActivity) requireActivity()).setLanguage(item.locale);
            }
        });

        mAdapter.setItems(mLanguages);
        mLvLanguageList.setAdapter(mAdapter);

        // set current language selected
        setSelectedLanguage(currentLanguage);
        mAdapter.notifyDataSetChanged();
    }

    void setSelectedLanguage(Locale locale) {
        if (mLanguages == null) return;

        for (LanguageItem l : mLanguages) {
            l.selected = l.locale.equals(locale);
        }
    }

    public interface OnLanguageSelectedListener {
        void onSelectedItem(LanguageItem item);
    }

    public static class LanguageItem {
        public Locale locale;
        public boolean selected;
        public int languageName;
        public int languageSubhead;

        public LanguageItem(Locale locale, int languageName, int languageSubhead) {
            this.locale = locale;
            this.selected = false;
            this.languageName = languageName;
            this.languageSubhead = languageSubhead;
        }
    }

    static class SettingsLanguageAdapter extends BaseAdapter {
        private List<LanguageItem> mItems;
        private OnLanguageSelectedListener mListener;
        private Activity mActivity;

        public SettingsLanguageAdapter(Activity activity) {
            mActivity = activity;
        }

        public void setOnLanguageItemClickListener(OnLanguageSelectedListener listener) {
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
                int itemLayout;
                if (mActivity instanceof A400SettingsActivity) {
                    itemLayout = R.layout.item_a400_language;
                } else {
                    itemLayout = R.layout.item_j1_language;
                }
                convertView = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            }

            TextView tvLanguageName = convertView.findViewById(R.id.tv_language_name);
            TextView tvLanguageSpell = convertView.findViewById(R.id.tv_language_spell);
            LanguageItem item = getItem(position);
            ImageView ivTick = convertView.findViewById(R.id.iv_tick);

            if (mActivity instanceof A400SettingsActivity) {
                tvLanguageName.setTextColor(item.selected ? 0xffffffff : 0xffC9C9C9);
            }
            if (mActivity instanceof J1SettingsActivity) {
                tvLanguageName.setText(item.languageName);
                tvLanguageSpell.setText(item.languageSubhead);
            } else {
                tvLanguageName.setText(item.locale.getDisplayLanguage());
                tvLanguageSpell.setText(item.locale.getDisplayLanguage(item.locale));
            }
            ivTick.setVisibility(item.selected ? View.VISIBLE : View.INVISIBLE);
            convertView.setActivated(item.selected);
            convertView.setOnClickListener(v -> mListener.onSelectedItem(item));
            return convertView;
        }
    }

}
