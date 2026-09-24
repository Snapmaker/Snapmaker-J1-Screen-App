package fabscreen.platform.base.service;

import android.content.Context;

public interface ILanguage {
    void setLanguage(Context context, int language);

    int getCurrentLanguage();
}
