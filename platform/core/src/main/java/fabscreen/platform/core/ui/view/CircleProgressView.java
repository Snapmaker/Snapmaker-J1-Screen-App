package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.base.view.CircularProgressView;

public class CircleProgressView extends CircularProgressView {
    public CircleProgressView(Context context) {
        super(context);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initialize() {
        super.initialize();
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO: Input Max/min processing exceeds the 270 range
        float progressAngle = mPercentage / 100 * 270;
        canvas.drawArc(mRectF, 135, 270, false, mBackgroundPaint);
        canvas.drawArc(mRectF, 135, progressAngle, false, mTintPaint);

    }

}

