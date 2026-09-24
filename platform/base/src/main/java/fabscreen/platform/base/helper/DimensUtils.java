package fabscreen.platform.base.helper;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DimensUtils {
    public static int dp2px(float dpValue, Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                context.getResources().getDisplayMetrics());
    }

    public static int dp2px(int dpValue, Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                Resources.getSystem().getDisplayMetrics());
    }

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }

    public static int dp2pxInt(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }
}
