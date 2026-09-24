package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import fabscreen.platform.core.R;

public class CustomArcSeekBar extends View {
    private static final int INVALID_PROGRESS_VALUE = -1;
    // The initial rotational offset -90 means we start at 12 o'clock
    private final int mAngleOffset = -90;

    /**
     * The Drawable for the seek arc thumbnail
     */
    private Drawable mThumb;

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mMaxProgress = 100;

    /**
     * The Current value that the SeekArc is set to
     */
    private int mProgress = 0;

    /**
     * The width of the progress line for this SeekArc
     */
    private int mProgressWidth = 4;

    /**
     * The Width of the background arc for the SeekArc
     */
    private int mArcWidth = 2;

    /**
     * The Angle to start drawing this Arc from
     */
    private int mStartAngle = 0;

    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private int mSweepAngle = 360;

    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private int mRotation = 0;

    /**
     * Give the SeekArc rounded edges
     */
    private boolean mRoundedEdges = false;

    /**
     * Enable touch inside the SeekArc
     */
    private boolean mTouchInside = true;

    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    private boolean mClockwise = true;


    /**
     * is the control enabled/touchable
     */
    private boolean mEnabled = true;

    // Internal variables
    private int mArcRadius = 0;
    private float mProgressSweep = 0;
    private final RectF mArcRect = new RectF();
    private Paint mArcPaint;
    private Paint mHeatingProgressPaint;
    private Paint mNoHeatingProgressPaint;
    private boolean mIsHeating = true;
    private Paint mPointerPaint;
    private int mTranslateX;
    private int mTranslateY;
    private int mThumbXPos;
    private int mThumbYPos;
    private double mTouchAngle;
    private float mTouchIgnoreRadius;
    private OnSeekArcChangeListener mOnSeekArcChangeListener;

    private int arcStartColor = Color.parseColor("#3D3F4C");
    private int arcEndColor = Color.parseColor("#2D2F40");
    private int progressStartColor = Color.parseColor("#F56A00");
    private int progressEndColor = Color.parseColor("#FFAB00");
    private int noHeatingStartColor = Color.parseColor("#595A66");
    private int noHeatingEndColor = Color.parseColor("#595A66");

    public interface OnSeekArcChangeListener {


        void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar);

        void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar);
    }

    public CustomArcSeekBar(Context context) {
        this(context, null);
    }

    public CustomArcSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomArcSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomArcSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings

        int thumbHalfheight = 0;
        int thumbHalfWidth = 0;
        mThumb = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close, null);
        // Convert progress width to pixels for current density
        mProgressWidth = (int) (mProgressWidth * density);


        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CustomArcSeekBar, defStyle, 0);

            Drawable thumb = a.getDrawable(R.styleable.CustomArcSeekBar_thumb);
            if (thumb != null) {
                mThumb = thumb;
            }


            thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
            thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
            mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);

            mMaxProgress = a.getInteger(R.styleable.CustomArcSeekBar_max, mMaxProgress);
            mProgress = a.getInteger(R.styleable.CustomArcSeekBar_progress, mProgress);
            mProgressWidth = (int) a.getDimension(
                    R.styleable.CustomArcSeekBar_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.CustomArcSeekBar_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.CustomArcSeekBar_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.CustomArcSeekBar_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.CustomArcSeekBar_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.CustomArcSeekBar_roundEdge,
                    mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.CustomArcSeekBar_touchInside,
                    mTouchInside);
            mClockwise = a.getBoolean(R.styleable.CustomArcSeekBar_clockwise,
                    mClockwise);
            mEnabled = a.getBoolean(R.styleable.CustomArcSeekBar_enabled, mEnabled);

            arcStartColor = a.getColor(R.styleable.CustomArcSeekBar_arcStartColor, arcStartColor);
            arcEndColor = a.getColor(R.styleable.CustomArcSeekBar_arcEndColor, arcEndColor);
            progressStartColor = a.getColor(R.styleable.CustomArcSeekBar_progressStartColor, progressStartColor);
            progressEndColor = a.getColor(R.styleable.CustomArcSeekBar_progressEndColor, progressEndColor);
            noHeatingStartColor = a.getColor(R.styleable.CustomArcSeekBar_noHeatingStartColor, noHeatingStartColor);
            noHeatingEndColor = a.getColor(R.styleable.CustomArcSeekBar_noHeatingEndColor, noHeatingEndColor);

            a.recycle();
        }

        mProgress = (mProgress > mMaxProgress) ? mMaxProgress : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mProgressSweep = (float) mProgress / (mMaxProgress - 0) * mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mArcPaint = new Paint();
        mArcPaint.setColor(arcStartColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        //mArcPaint.setAlpha(45);
        mNoHeatingProgressPaint = new Paint();
        mNoHeatingProgressPaint.setColor(noHeatingStartColor);
        mNoHeatingProgressPaint.setAntiAlias(true);
        mNoHeatingProgressPaint.setStyle(Paint.Style.STROKE);
        mNoHeatingProgressPaint.setStrokeWidth(mProgressWidth);


        mHeatingProgressPaint = new Paint();
        mHeatingProgressPaint.setColor(progressStartColor);
        mHeatingProgressPaint.setAntiAlias(true);
        mHeatingProgressPaint.setStyle(Paint.Style.STROKE);
        mHeatingProgressPaint.setStrokeWidth(mProgressWidth);

        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerPaint.setColor(Color.WHITE);


        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mHeatingProgressPaint.setStrokeCap(Paint.Cap.ROUND);
            mNoHeatingProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Matrix matrix = new Matrix();
        matrix.setRotate(90, w / 2, h / 2);
        SweepGradient HeatingProgressSweepGradient = new SweepGradient(w / 2, h / 2, new int[]{progressStartColor, progressEndColor}, new float[]{0, 1});
        SweepGradient NoHeatingProgressSweepGradient = new SweepGradient(w / 2, h / 2, new int[]{noHeatingStartColor, noHeatingEndColor}, new float[]{0, 1});
        SweepGradient ArcPaintSweepGradient = new SweepGradient(w / 2, h / 2, new int[]{arcStartColor, arcEndColor, arcStartColor}, new float[]{0, 0.5f, 1f});
        HeatingProgressSweepGradient.setLocalMatrix(matrix);
        NoHeatingProgressSweepGradient.setLocalMatrix(matrix);
        ArcPaintSweepGradient.setLocalMatrix(matrix);

        mHeatingProgressPaint.setShader(HeatingProgressSweepGradient);
        mNoHeatingProgressPaint.setShader(NoHeatingProgressSweepGradient);
        mArcPaint.setShader(ArcPaintSweepGradient);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!mClockwise) {
            canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());
        }

        // Draw the arcs
        final int arcStart = mStartAngle + mAngleOffset + mRotation;
        final int arcSweep = mSweepAngle;

        canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
        canvas.drawArc(mArcRect, arcStart, mProgressSweep, false,
                mIsHeating ? mHeatingProgressPaint : mNoHeatingProgressPaint);

        canvas.drawCircle(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos, mProgressWidth * 0.4f, mPointerPaint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int min = Math.min(width, height);
        float top = 0;
        float left = 0;
        int arcDiameter = 0;

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);

        arcDiameter = min - getPaddingLeft();
        mArcRadius = arcDiameter / 2;
        top = height / 2 - (arcDiameter / 2);
        left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

        setTouchInSide(mTouchInside);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onStartTrackingTouch();
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    private void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this);
        }
    }

    private void updateOnTouch(MotionEvent event) {
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if (ignoreTouch) {
            return;
        }
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }
        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        //invert the x-coord if we are rotating anti-clockwise
        x = (mClockwise) ? x : -x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mMaxProgress) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;
    }

    private float valuePerDegree() {
        return (float) mMaxProgress / mSweepAngle;
    }

    private void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
    }

    private void updateProgress(int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        progress = Math.min(progress, mMaxProgress);
        progress = Math.max(progress, 0);
        mProgress = progress;

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, progress, fromUser);
        }

        mProgressSweep = (float) progress / mMaxProgress * mSweepAngle;

        updateThumbPosition();

        invalidate();
    }

    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        mOnSeekArcChangeListener = l;
    }

    public void setProgress(int progress) {
        updateProgress(progress, false);
    }

    public int getProgress() {
        return mProgress;
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mHeatingProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public int getArcRotation() {
        return mRotation;
    }

    public void setArcRotation(int mRotation) {
        this.mRotation = mRotation;
        updateThumbPosition();
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        updateThumbPosition();
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
        updateThumbPosition();
    }

    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mHeatingProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mHeatingProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
        }
    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreRadius = mArcRadius
                    - Math.min(thumbHalfWidth, thumbHalfheight);
        }
    }

    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }

    public boolean isClockwise() {
        return mClockwise;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public int getProgressColor() {
        return mHeatingProgressPaint.getColor();
    }

    public void setProgressColor(int color) {
        mHeatingProgressPaint.setColor(color);
        invalidate();
    }

    public int getArcColor() {
        return mArcPaint.getColor();
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
        invalidate();
    }

    public int getMax() {
        return mMaxProgress;
    }

    public void setMax(int max) {
        mMaxProgress = max;
    }

    public void setHeating(boolean isHeating) {
        if (mIsHeating != isHeating) {
            mIsHeating = isHeating;
            invalidate();
        }
    }
}
