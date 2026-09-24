package fabscreen.platform.base.lib.hook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("JavaReflectionMemberAccess")
public class HookHelper {
    public static void hook(Context context, View view) {
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method method = View.class.getDeclaredMethod("getListenerInfo");
            method.setAccessible(true);
            Object mListenerInfo = method.invoke(view);
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.view.View$ListenerInfo");
            @SuppressLint("DiscouragedPrivateApi")
            Field field = clz.getDeclaredField("mOnClickListener");
            field.setAccessible(true);
            View.OnClickListener onClickListenerInstance = (View.OnClickListener) field.get(mListenerInfo);
            View.OnClickListener hookedOnClickListener = new ProxyOnClickListener(onClickListenerInstance);
            field.set(mListenerInfo, hookedOnClickListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
