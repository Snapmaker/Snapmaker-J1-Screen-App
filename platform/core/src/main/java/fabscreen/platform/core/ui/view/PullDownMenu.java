package fabscreen.platform.core.ui.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import fabscreen.platform.core.R;

public class PullDownMenu {
    private static PullDownMenu mMenu;
    private ListView mListView;
    private PopupWindow mWindow;

    public static PullDownMenu create(Context context, ListAdapter adapter) {
        if (mMenu != null) {
            mMenu.mWindow.dismiss();
        }
        final Activity activity = (Activity) context;

        mMenu = new PullDownMenu();

        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.widget_pull_down_menu, null);

        mMenu.mWindow = new PopupWindow(view,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);

        // view binding
        mMenu.mListView = view.findViewById(R.id.lv_menu_list);
        mMenu.mListView.setAdapter(adapter);
        return mMenu;
    }

    public PullDownMenu setOnDismiss(PopupWindow.OnDismissListener onDismissListener) {
        mMenu.mWindow.setOnDismissListener(onDismissListener);
        return mMenu;
    }

    public static PullDownMenu setElevation(int elevation) {
        mMenu.mWindow.setElevation(elevation);
        return mMenu;
    }

    public static void dismiss() {
        if (mMenu != null) {
            mMenu.mWindow.dismiss();
        }
    }

    public void showBelowView(View view, int xOff, int yOff) {
        // xoff and yoff are in pixels
        mWindow.showAsDropDown(view, xOff, yOff);
    }

    public void showDownView(View view) {
        mWindow.showAsDropDown(view);
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {

        mWindow.showAtLocation(parent, gravity, x, y);
    }

}
