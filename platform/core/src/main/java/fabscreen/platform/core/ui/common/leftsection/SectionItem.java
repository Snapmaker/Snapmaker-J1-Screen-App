package fabscreen.platform.core.ui.common.leftsection;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

public class SectionItem {

    public String title;
    public @DrawableRes
    int iconRes;
    public int lineCount;
    public boolean showBadge;
    public Fragment fragment;

    public SectionItem(Context context, @StringRes int title, Fragment fragment) {
        this(context.getString(title), 0, fragment, false);
    }

    /**
     * @param lineCount Line count of title.
     */
    public SectionItem(Context context, @StringRes int title, Fragment fragment, int lineCount) {
        this(context.getString(title), 0, fragment, false, lineCount);
    }

    public SectionItem(Context context, @DrawableRes int icon, @StringRes int title, Fragment fragment) {
        this(context.getString(title), icon, fragment, false);
    }

    public SectionItem(String title, Fragment fragment) {
        this(title, 0, fragment, false);
    }

    public SectionItem(String title, @DrawableRes int icon, Fragment fragment) {
        this(title, icon, fragment, false);
    }

    public SectionItem(String title, @DrawableRes int icon, Fragment fragment, boolean showBadge) {
        this(title, icon, fragment, showBadge, 2);
    }

    public SectionItem(String title, @DrawableRes int icon, Fragment fragment, boolean showBadge, int lineCount) {
        this.title = title;
        this.iconRes = icon;
        this.showBadge = showBadge;
        this.fragment = fragment;
        this.lineCount = lineCount;
    }
}
