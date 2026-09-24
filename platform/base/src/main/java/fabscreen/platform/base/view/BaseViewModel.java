package fabscreen.platform.base.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;

import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.IServiceContainer;
import fabscreen.platform.base.instantiation.ServiceContainer;

//import fabscreen.libraries.core.BaseApplication;
//import fabscreen.libraries.core.ui.data.Model;

public class BaseViewModel extends AutoDisposeViewModel {

//    private final Model mModel;
//
//    protected BaseViewModel() {
//        mModel = BaseApplication.getInstance().getModel();
//    }

    protected IServiceContainer getServiceContainer() {
        return ServiceContainer.getInstance();
    }

    protected <T> AutoDisposeConverter<T> bindToLifecycle() {
        return AutoDispose.autoDisposable(this);
    }

}
