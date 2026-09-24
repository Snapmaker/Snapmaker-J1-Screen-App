package fabscreen.features.welcome.j1;

public class LanguageItem {

    private int language;
    private String languageName;

    public LanguageItem(int language, String languageName) {
        this.language = language;
        this.languageName = languageName;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

}
