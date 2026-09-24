package fabscreen.platform.core.ui.view.leftsection;

import androidx.annotation.DrawableRes;

public interface ILeftSectionView {
    void setTitle(String title);

    void setTitle(int titleResId);

    void setIcon(@DrawableRes int drawableResId);

    void setShowBadge(boolean show);

    void setSelected(boolean selected);

    void setLineCount(int count);
}
