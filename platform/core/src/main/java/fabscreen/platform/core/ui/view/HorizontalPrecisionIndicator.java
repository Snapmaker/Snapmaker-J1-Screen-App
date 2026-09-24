package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import fabscreen.platform.core.R;

public class HorizontalPrecisionIndicator extends ConstraintLayout {
    protected RectF mRectF = new RectF();
    protected RectF mBgRectF = new RectF();
    protected RectF mBitmapRectF = new RectF();

    private ImageView mIvValueStatus;

    private Bitmap mBitmapLeftSideIndicator = null;
    private Bitmap mBitmapRightSideIndicator = null;

    private int mWidth;
    private int mHeight;

    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mCurrentValue;
    private float mMaxValue;
    private float mMinValue;

    private float mValidRange;
    private float mVisualRange = 1f;
    private boolean mIsOutOfVisualRange = false;

    // center point
    protected Point mCenterPoint = new Point();

    protected int mStrokeWidth = dp2px(5);
    protected int mProgressColor = Color.parseColor("#FF8A00");
    protected int mBackgroundColor = Color.parseColor("#3D3F4D");
    protected int mCenterLineColor = Color.parseColor("#737480");

    // Paint
    protected Paint mTintPaint;
    protected Paint mBgPaint;
    protected Paint mCenterLinePaint;

    private boolean mIsValueValid = false;
    private PrecisionIndicatorListener mPrecisionIndicatorListener;

    public HorizontalPrecisionIndicator(Context context) {
        this(context, null);
    }

    public HorizontalPrecisionIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalPrecisionIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HorizontalPrecisionIndicator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalPrecisionIndicator, defStyleAttr, defStyleRes);
        mCurrentValue = typedArray.getFloat(R.styleable.HorizontalPrecisionIndicator_hpi_value, 0);
        mMaxValue = typedArray.getFloat(R.styleable.HorizontalPrecisionIndicator_hpi_maxValue, 5);
        mMinValue = typedArray.getFloat(R.styleable.HorizontalPrecisionIndicator_hpi_minValue, -5);
        mProgressColor = typedArray.getColor(R.styleable.HorizontalPrecisionIndicator_hpi_indicator_color, Color.parseColor("#FF8A00"));
        mValidRange = typedArray.getFloat(R.styleable.HorizontalPrecisionIndicator_hpi_valid_range, 0.08f);

        typedArray.recycle();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void initialize(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View parentView = inflater.inflate(R.layout.view_horizontal_precision_indicator, this, true);

        mIvValueStatus = parentView.findViewById(R.id.iv_precision_indicator_info);

        mTintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTintPaint.setStyle(Paint.Style.STROKE);
        mTintPaint.setStrokeWidth(mStrokeWidth);
        mTintPaint.setStrokeCap(Paint.Cap.ROUND);
        mTintPaint.setColor(mProgressColor);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeWidth(mStrokeWidth);
        mBgPaint.setStrokeCap(Paint.Cap.ROUND);
        mBgPaint.setColor(mBackgroundColor);

        mCenterLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterLinePaint.setStyle(Paint.Style.STROKE);
        mCenterLinePaint.setStrokeWidth(dp2px(1.5f));
        mCenterLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mCenterLinePaint.setColor(mCenterLineColor);

        // init bitmap from resource
        mBitmapRightSideIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.pic_slide_indicator_right);
        mBitmapLeftSideIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.pic_slide_indicator_left);
        mBitmapWidth = mBitmapLeftSideIndicator.getWidth();
        mBitmapHeight = mBitmapLeftSideIndicator.getHeight();
        // Enable onDraw() function, ViewGroup set WILL_NOT_DRAW flag default.
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;

        Matrix matrix = new Matrix();
        matrix.setRotate(90, mCenterPoint.x, mCenterPoint.y);
        SweepGradient backgroundSweepGradient = new SweepGradient(mCenterPoint.x, mCenterPoint.y,
                new int[]{Color.parseColor("#003D3F4D"), mBackgroundColor, Color.parseColor("#003D3F4D")},
                new float[]{0, 0.5f, 1f});
        backgroundSweepGradient.setLocalMatrix(matrix);
        mBgPaint.setShader(backgroundSweepGradient);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Background precision bar
        mBgRectF.top = mCenterPoint.y-2;
        mBgRectF.bottom = mCenterPoint.y+2;
        mBgRectF.left = mCenterPoint.x - mWidth * 0.5f + 10;
        mBgRectF.right = mCenterPoint.x + mWidth * 0.5f - 10;
        canvas.drawRoundRect(mBgRectF, 5, 5, mBgPaint);

        // Draw center line
        canvas.drawLine(mCenterPoint.x, mCenterPoint.y - 16, mCenterPoint.x, mCenterPoint.y + 16, mCenterLinePaint);

        // Draw current range rect
        mRectF.top = mCenterPoint.y - 2;
        mRectF.bottom = mCenterPoint.y + 2;
        mRectF.left = (mCenterPoint.x + mCurrentValue * mWidth) - (mValidRange * mWidth * 0.5f);
        mRectF.right = (mCenterPoint.x + mCurrentValue * mWidth) + (mValidRange * mWidth * 0.5f);
        canvas.drawRoundRect(mRectF, 5, 5, mTintPaint);

        // Side indicator
        if (mIsOutOfVisualRange) {
            if (mCurrentValue > 0) {
                // right side indicator
                mBitmapRectF.top = mCenterPoint.y -  mBitmapHeight * 0.5f;
                mBitmapRectF.bottom = mCenterPoint.y + mBitmapHeight * 0.5f;
                mBitmapRectF.left = mWidth - 20 - mBitmapWidth * 0.5f;
                mBitmapRectF.right = mWidth - 20 + mBitmapWidth * 0.5f;
                if (mCurrentValue < mMaxValue - 1) {
                    canvas.drawBitmap(mBitmapRightSideIndicator, mBitmapRectF.left, mBitmapRectF.top, null);
                } else {
                    canvas.drawBitmap(mBitmapRightSideIndicator, mBitmapRectF.left - 7.5f, mBitmapRectF.top, null);
                    canvas.drawBitmap(mBitmapRightSideIndicator, mBitmapRectF.left + 7.5f, mBitmapRectF.top, null);
                }
            } else {
                // left side indicator
                mBitmapRectF.top = mCenterPoint.y -  mBitmapHeight * 0.5f;
                mBitmapRectF.bottom = mCenterPoint.y + mBitmapHeight * 0.5f;
                mBitmapRectF.left = 20 - mBitmapWidth * 0.5f;
                mBitmapRectF.right = 20 + mBitmapWidth * 0.5f;
                if (mCurrentValue > mMinValue + 1) {
                    canvas.drawBitmap(mBitmapLeftSideIndicator, mBitmapRectF.left, mBitmapRectF.top, null);
                } else {
                    canvas.drawBitmap(mBitmapLeftSideIndicator, mBitmapRectF.left - 7.5f, mBitmapRectF.top, null);
                    canvas.drawBitmap(mBitmapLeftSideIndicator, mBitmapRectF.left + 7.5f, mBitmapRectF.top, null);
                }
            }

        }
        super.onDraw(canvas);
    }

    public void setValue(float value) {
        if (value > mMaxValue || value < mMinValue) {
            mCurrentValue = value > mMaxValue ? mMaxValue : mMinValue;
        } else {
            mCurrentValue = value;
        }

        onValueChange();

        // refresh when value changed.
        invalidate();
    }

    public void setValidRange(float range) {
        mValidRange = range;
    }

    private boolean isValueValid(float value) {
        return (value > mValidRange * -0.5f) && (value < mValidRange * 0.5f);
    }

    public void onValueChange() {
        // check valid range
        mIsValueValid = isValueValid(mCurrentValue);
        mPrecisionIndicatorListener.onValueValid(mIsValueValid);

        if (mIsValueValid) {
            mIvValueStatus.setImageResource(R.drawable.icon_tips_success_64x64);
        } else {
            boolean isClockwise = mCurrentValue < mValidRange * -0.5f;
            mIvValueStatus.setImageResource(isClockwise ? R.drawable.icon_calibration_clockwise : R.drawable.icon_calibration_anti_clockwise);
        }

        // check value out of visual range
        if (mCurrentValue > (mVisualRange + mValidRange) * 0.5f || mCurrentValue < (mVisualRange + mValidRange) * -0.5f) {
            mIsOutOfVisualRange = true;
        } else {
            mIsOutOfVisualRange = false;
        }
    }

    public void setPrecisionIndicatorListener(PrecisionIndicatorListener listener) {
        mPrecisionIndicatorListener = listener;
    }

    public interface PrecisionIndicatorListener {
        void onValueValid(boolean isValid);
    }
}
