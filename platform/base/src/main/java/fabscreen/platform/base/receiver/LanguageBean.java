package fabscreen.platform.base.receiver;

import java.util.Locale;

public class LanguageBean {

    public int code;
    public Locale locale;

    public LanguageBean(Locale locale, int code) {
        this.code = code;
        this.locale = locale;
    }



}
