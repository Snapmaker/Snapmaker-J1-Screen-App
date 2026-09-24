package fabscreen.platform.core.ui.common;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import java.lang.reflect.Field;

import fabscreen.platform.base.helper.DimensUtils;

public class A400SwitchCompat extends SwitchCompat {
    public A400SwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * https://stackoverflow.com/questions/40392990/how-to-shrink-switchcompat-width
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            Field switchWidth = SwitchCompat.class.getDeclaredField("mSwitchWidth");
            switchWidth.setAccessible(true);
            switchWidth.setInt(this, (int) DimensUtils.dp2px(114));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
