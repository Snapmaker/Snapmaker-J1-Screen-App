package fabscreen.platform.base.service;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import androidx.annotation.Keep;

import java.util.Locale;

import fabscreen.platform.base.instantiation.IServiceIdentifier;


public class MultiLanguageManager implements ILanguage, IServiceIdentifier {
    public static final int LANGUAGE_UNKNOWN = -1;
    public static final int LANGUAGE_DEFAULT = 0;
    public static final int LANGUAGE_GERMAN = 1;
    public static final int LANGUAGE_SIMPLIFIED_CHINESE = 2;
    public static final int LANGUAGE_FRENCH = 3;
    public static final int LANGUAGE_JAPANESE = 4;
    public static final int LANGUAGE_KOREAN = 5;
    public static final int LANGUAGE_ITALIAN = 6;

    private IPreferences mPreferences;

    @Keep
    public MultiLanguageManager(IPreferences preferences) {
        mPreferences = preferences;
    }

    /**
     * applyApplicationLanguage: Reload locale with application context.
     *
     * @param context
     * @param locale
     */
    public static void applyApplicationLanguage(Context context, Locale locale) {
        Resources resources = context.getApplicationContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Set default locale
            Locale.setDefault(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);

            // Set locale and localList for Configuration
            configuration.setLocale(locale);
            configuration.setLocales(localeList);
            context.getApplicationContext().createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, dm);
    }

    // Maybe a HashMap will be better?
    public static Locale language2Locale(int language) {
        switch (language) {
            case LANGUAGE_GERMAN:
                return Locale.GERMAN;
            case LANGUAGE_SIMPLIFIED_CHINESE:
                return Locale.SIMPLIFIED_CHINESE;
            case LANGUAGE_FRENCH:
                return Locale.FRENCH;
            case LANGUAGE_JAPANESE:
                return Locale.JAPANESE;
            case LANGUAGE_KOREAN:
                return Locale.KOREAN;
            case LANGUAGE_ITALIAN:
                return Locale.ITALIAN;
            case LANGUAGE_DEFAULT:
            default:
                return Locale.ENGLISH;
        }
    }

    public static int Locale2Language(Locale locale) {
        switch (locale.getLanguage()) {
            case "de":
                return LANGUAGE_GERMAN;
            case "zh":
                return LANGUAGE_SIMPLIFIED_CHINESE;
            case "fr":
                return LANGUAGE_FRENCH;
            case "ja":
                return LANGUAGE_JAPANESE;
            case "ko":
                return LANGUAGE_KOREAN;
            case "it":
                return LANGUAGE_ITALIAN;
            case "en":
            default:
                return LANGUAGE_DEFAULT;
        }
    }

    public void setLanguage(Context context, int language) {
        setCurrentLanguage(language);
        applyApplicationLanguage(context, language2Locale(language));
    }

    public int getCurrentLanguage() {
        return mPreferences.getHelper().getUserSelectedLanguage();
    }

    private void setCurrentLanguage(int selectedLanguage) {
        mPreferences.getHelper().setUserSelectedLanguage(selectedLanguage);
    }
}
