package fabscreen.features.welcome.a400;

import java.util.Locale;

public class LanguageItem {
    public Locale locale;
    public String name;

    public LanguageItem(Locale locale, String name) {
        this.locale = locale;
        this.name = name;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
