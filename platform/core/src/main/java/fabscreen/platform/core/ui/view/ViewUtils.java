package fabscreen.platform.core.ui.view;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.view.bottombar.BottomBarItem;

public class ViewUtils {
    public static BottomBarItem createBottomBarItem(@StringRes int titleRes, @DrawableRes int iconRes) {
        BottomBarItem item = new BottomBarItem(titleRes);
        item.setIcon(iconRes);
        item.setNormalColor(R.color.custom_grey_0);
        item.setSelectedColor(R.color.custom_blue_600);
        item.setDisabledColor(R.color.custom_grey_600);
        return item;
    }

    public static void enableButtons(ViewGroup viewGroup, Boolean enabled) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child.isClickable()) {
                child.setEnabled(enabled);
            }
            if (child instanceof ViewGroup) {
                enableButtons((ViewGroup) child, enabled);
            }
        }
    }
}
