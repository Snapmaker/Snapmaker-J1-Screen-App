package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R;

public class GradientCircularProgressBar extends View {
    private float mIndicatorWidth;
    private @ColorInt
    int mStartColor;
    private @ColorInt
    int mEndColor;
    private int mProgress;
    private int mMaxProgress;
    private RectF mRectF;
    private Paint mPaint;


    public GradientCircularProgressBar(Context context) {
        this(context, null);
    }

    public GradientCircularProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientCircularProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GradientCircularProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs, defStyleAttr, defStyleRes);
        initDrawTools();
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.GradientCircularProgressBar, defStyleAttr, defStyleRes);
        mIndicatorWidth = ta.getDimension(R.styleable.GradientCircularProgressBar_gcpb_indicator_width, DimensUtils.dp2px(6));
        mStartColor = ta.getColor(R.styleable.GradientCircularProgressBar_gcpb_start_color, 0xFFFFAB00);
        mEndColor = ta.getColor(R.styleable.GradientCircularProgressBar_gcpb_end_color, 0xFFF56A00);
        mProgress = ta.getInt(R.styleable.GradientCircularProgressBar_gcpb_progress, 30);
        mMaxProgress = ta.getInt(R.styleable.GradientCircularProgressBar_gcpb_max_progress, 100);
        ta.recycle();
    }

    private void initDrawTools() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mIndicatorWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectF = new RectF();
        mRectF.left = mIndicatorWidth / 2;
        mRectF.top = mIndicatorWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Make 0 at 12 o'clock
        canvas.rotate(-90, getWidth() / 2f, getHeight() / 2f);
        // Calculate and add the start offset to avoid wrong round cap color.
        float angleOffset = calculateAngleOffset();
        changePaintGradientShader(mProgress);
        canvas.drawArc(mRectF, 0 + angleOffset, 360 * mProgress / (float) mMaxProgress, false, mPaint);
    }

    /**
     * Calculate the offset of the start to add.
     * <p>
     * <img src="https://dsm01pap003files.storage.live.com/y4mWK5SC8wfarK7Em3X-bEAamSCZaM5AJ7NdcTL_wlQukxIrKsssXynFVuE_nrSASkPpd-V04DVTZKBwPWOr1HSj8Q4a7uYEWyvHt3dn-Z0MyKDMPhmUBtAvaP4ATd0x4F3CGBpeyMObBXqwT4D46AqMFAGdmRaKHoLzWxxPBFo-hc0fB28dI5aHhpR5D8JghZa?width=660&height=563&cropmode=none" width="660" height="563" />
     * <p>
     *
     * Refers to <a href="https://blog.csdn.net/weixin_42473228/article/details/121795128">article</a>.
     */
    private float calculateAngleOffset() {
        float capRadius = mIndicatorWidth / 2;
        float bgRadius = getWidth() / 2f;
        return (float) Math.toDegrees(Math.asin(capRadius / (bgRadius - capRadius)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.right = w - mIndicatorWidth / 2;
        mRectF.bottom = h - mIndicatorWidth / 2;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    private void changePaintGradientShader(int progress) {
        int[] colors;
        float[] positions;
        if (progress <= 90) {
            colors = new int[]{mStartColor, mEndColor};
            positions = new float[]{0f, mProgress / (float) mMaxProgress};
        } else {
            colors = new int[]{mStartColor, mEndColor, mStartColor};
            positions = new float[]{0f, mProgress / (float) mMaxProgress - 0.03f, mProgress / (float) mMaxProgress};
        }
        SweepGradient sweepGradient = new SweepGradient(getWidth() / 2f, getHeight() / 2f, colors, positions);
        mPaint.setShader(sweepGradient);
    }
}
