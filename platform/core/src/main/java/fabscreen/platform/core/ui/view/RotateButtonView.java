package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.core.R;

public class RotateButtonView extends View {
    ArrayList<float[]> floats;
    float mArrowDistance;
    float mAngleDifference;
    float mArrowCenterlineLength;
    float mArrowDistance2;
    float mAngleDifference2;
    float mArrowCenterlineLength2;
    private float mIncludedAngle;
    // Outer ring curve
    private float mOuterRingWidth;
    private float mOuterRingFilletRadian;
    private float mPadding;
    private float mInnerRingPadding;
    private float mArrowSize;
    private float mArrowMagnification;
    private int mArrowClickColor;

    // Inner circular line
    private float mInnerRingLength;
    private float mInnerRingWidth;
    private float mInnerRingRadian;
    private int mInnerRingNumber;
    private float mTextSize;
    private int mTextColor;
    private String mTextUnitType;
    // Color
    private int mOuterRingColor;
    private int mInnerRingBackground;
    private int mInnerRingColorFirst;
    private int mInnerRingColorSecond;
    private int mArrowColor;
    // Paint
    private Paint mOuterRingPaint;
    private Paint mArrowPaint;
    private Paint mInnerRingBackgroundPaint;
    private Paint mInnerRingColorFirstPaint;
    private Paint mInnerRingColorSecondPaint;
    private Paint mSubscriptTextPaint;
    // Data
    // Whether to draw overlay color
    private boolean mDrawSecondColorStatus;
    private boolean mTouchable = true;
    // Whether the color is superimposed below
    private boolean mDragStatus;
    private int color1Range, color2Range;
    private int color1JumpRange = 0;
    private float mMaxOuterRingRadius;
    private float mMaxOuterRingCenter;
    private float downdeg = 0;
    private onProgressChangedListener mProgressChangeListener;
    private OnCrollerChangeListener mCrollerChangeListener;
    private float mMin = 0;
    private float mMax = 100;

    private boolean mDataFiltering = true;
    private IAppService mApp;


    public RotateButtonView(Context context) {
        this(context, null);
    }

    public RotateButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RotateButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.RotateButtonView, defStyleAttr, defStyleRes);
        mIncludedAngle = typedArray.getDimension(R.styleable.RotateButtonView_srv_includedAngle, 45);
        mOuterRingColor = typedArray.getColor(R.styleable.RotateButtonView_srv_outerRingColor, Color.parseColor("#090909"));
        mOuterRingWidth = typedArray.getDimension(R.styleable.RotateButtonView_srv_outerRingWidth, dp2px(14));
        mOuterRingFilletRadian = typedArray.getDimension(R.styleable.RotateButtonView_srv_outerRingFilletRadian, dp2px(30));
//        mPadding = typedArray.getDimension(R.styleable.RotateButtonView_srv_padding, 0);
        mInnerRingPadding = typedArray.getDimension(R.styleable.RotateButtonView_srv_innerRingPadding, dp2px(20));
        mInnerRingBackground = typedArray.getColor(R.styleable.RotateButtonView_srv_innerRingBackground, Color.parseColor("#6E6E6E"));
        mInnerRingColorFirst = typedArray.getColor(R.styleable.RotateButtonView_srv_innerRingColorFirst, Color.parseColor("#1A41F5"));
        mInnerRingColorSecond = typedArray.getColor(R.styleable.RotateButtonView_srv_innerRingColorSecond, Color.parseColor("#FFAB00"));
        mInnerRingLength = typedArray.getDimension(R.styleable.RotateButtonView_srv_innerRingLength, dp2px(26));
        mInnerRingWidth = typedArray.getDimension(R.styleable.RotateButtonView_srv_innerRingWidth, dp2px(8));
        mInnerRingRadian = typedArray.getDimension(R.styleable.RotateButtonView_srv_innerRingRadian, dp2px(10));
        mInnerRingNumber = typedArray.getInt(R.styleable.RotateButtonView_srv_innerRingNumber, 36);
        mDrawSecondColorStatus = typedArray.getBoolean(R.styleable.RotateButtonView_srv_drawSecondColorStatus, true);
        mTextSize = typedArray.getDimension(R.styleable.RotateButtonView_srv_textSize, dp2px(28));
        mTextColor = typedArray.getColor(R.styleable.RotateButtonView_srv_textColor, Color.parseColor("#6E6E6E"));
        mTextUnitType = typedArray.getString(R.styleable.RotateButtonView_srv_textUnitType);
        mArrowColor = typedArray.getColor(R.styleable.RotateButtonView_srv_arrowColor, Color.parseColor("#1A41F5"));
        mArrowSize = typedArray.getDimension(R.styleable.RotateButtonView_srv_arrowSize, dp2px(22));
        mArrowMagnification = typedArray.getFloat(R.styleable.RotateButtonView_srv_arrowMagnification, 1.5f);
        mArrowClickColor = typedArray.getColor(R.styleable.RotateButtonView_srv_arrowClickColor, Color.parseColor("#1534C4"));
        typedArray.recycle();
    }

    private void initialize(Context context) {
        mApp = ServiceContainer.getInstance().getService(IAppService.class);
        mOuterRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterRingPaint.setStyle(Paint.Style.STROKE);
        mOuterRingPaint.setStrokeCap(Paint.Cap.ROUND);
        mOuterRingPaint.setStrokeWidth(mOuterRingWidth);
        mOuterRingPaint.setStrokeMiter(mOuterRingFilletRadian);
        mOuterRingPaint.setColor(mOuterRingColor);

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStrokeWidth(mInnerRingWidth);
        mArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mArrowPaint.setStrokeCap(Paint.Cap.ROUND);
        mArrowPaint.setStrokeJoin(Paint.Join.ROUND);
        mArrowPaint.setColor(mArrowColor);

        mInnerRingBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerRingBackgroundPaint.setStrokeWidth(mInnerRingWidth);
        mInnerRingBackgroundPaint.setStrokeMiter(mInnerRingRadian);
        mInnerRingBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        mInnerRingBackgroundPaint.setColor(mInnerRingBackground);

        mInnerRingColorFirstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerRingColorFirstPaint.setStrokeWidth(mInnerRingWidth);
        mInnerRingColorFirstPaint.setStrokeMiter(mInnerRingRadian);
        mInnerRingColorFirstPaint.setStrokeCap(Paint.Cap.ROUND);
        mInnerRingColorFirstPaint.setColor(mInnerRingColorFirst);

        mInnerRingColorSecondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerRingColorSecondPaint.setStrokeWidth(mInnerRingWidth);
        mInnerRingColorSecondPaint.setStrokeMiter(mInnerRingRadian);
        mInnerRingColorSecondPaint.setStrokeCap(Paint.Cap.ROUND);
        mInnerRingColorSecondPaint.setColor(mInnerRingColorSecond);

        mSubscriptTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSubscriptTextPaint.setColor(mTextColor);
        mSubscriptTextPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBase(canvas);
        if (mDragStatus) {
            if (mDrawSecondColorStatus) {
                drawColor2(canvas);
            }
            if (mTouchable) {
                drawColor1(canvas);
            }
        } else {
            if (mTouchable) {
                drawColor1(canvas);
            }
            if (mDrawSecondColorStatus) {
                drawColor2(canvas);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        double mArrowSize2 = mArrowSize * mArrowMagnification;
        // Calculate the height of the scaled triangle
        mArrowCenterlineLength = (float) Math.pow(Math.pow(mArrowSize, 2) - Math.pow(mArrowSize / 2, 2), 1 / 2f);
        mArrowCenterlineLength2 = (float) Math.pow(Math.pow(mArrowSize2, 2) - Math.pow(mArrowSize2 / 2, 2), 1 / 2f);
        // Get half the height of the big triangle (the center of the triangle is on the ring)
        float heightDifference = Math.max(mArrowCenterlineLength2, mArrowCenterlineLength) / 2;
        calculateDrawnLines(heightDifference);
        if (mAngleDifference == 0) {
            mArrowDistance = (float) Math.pow(Math.pow(mArrowCenterlineLength / 2 + mMaxOuterRingRadius, 2) + Math.pow(mArrowSize / 2, 2), 1 / 2f);
            mAngleDifference = (float) Math.toDegrees(Math.asin(mArrowSize / 2 / mArrowDistance));

            mArrowDistance2 = (float) Math.pow(Math.pow(mArrowCenterlineLength2 / 2 + mMaxOuterRingRadius, 2) + Math.pow(mArrowSize2 / 2, 2), 1 / 2f);
            mAngleDifference2 = (float) Math.toDegrees(Math.asin(mArrowSize2 / 2 / mArrowDistance));
        }
    }

    private void calculateDrawnLines(float heightDifference) {
        // Get half of the largest square in view
        mMaxOuterRingCenter = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2f;
        mMaxOuterRingRadius = mMaxOuterRingCenter - Math.max(mOuterRingWidth, heightDifference);
        float angleDifference = (360 - (mIncludedAngle * 2)) / (mInnerRingNumber - 1);
        floats = new ArrayList<>(mInnerRingNumber);
        for (int i = 0; i < mInnerRingNumber; i++) {
            float x1 = mMaxOuterRingCenter + mPadding - (float) ((mMaxOuterRingRadius - mInnerRingPadding) * Math.sin(Math.toRadians(mIncludedAngle + angleDifference * i)));
            float x2 = mMaxOuterRingCenter + mPadding - (float) ((mMaxOuterRingRadius - mInnerRingPadding - mInnerRingLength) * Math.sin(Math.toRadians(mIncludedAngle + angleDifference * i)));
            float y1 = mMaxOuterRingCenter + mPadding + (float) ((mMaxOuterRingRadius - mInnerRingPadding) * Math.cos(Math.toRadians(mIncludedAngle + angleDifference * i)));
            float y2 = mMaxOuterRingCenter + mPadding + (float) ((mMaxOuterRingRadius - mInnerRingPadding - mInnerRingLength) * Math.cos(Math.toRadians(mIncludedAngle + angleDifference * i)));
            floats.add(new float[]{x1, y1, x2, y2});
        }
    }

    private void drawBase(Canvas canvas) {
        RectF oval = new RectF(mMaxOuterRingCenter - mMaxOuterRingRadius, mMaxOuterRingCenter - mMaxOuterRingRadius, mMaxOuterRingCenter + mMaxOuterRingRadius, mMaxOuterRingCenter + mMaxOuterRingRadius);
        canvas.drawArc(oval, 90 + mIncludedAngle, 360 - mIncludedAngle * 2, false, mOuterRingPaint);
        Path path = new Path();
        if (mTouchable) {
            if (mDragStatus) {
                path.moveTo((float) (mMaxOuterRingCenter - (mMaxOuterRingRadius - mArrowCenterlineLength2 / 2) * Math.sin(Math.toRadians(downdeg))), (float) (mMaxOuterRingCenter + mPadding + (mMaxOuterRingRadius - mArrowCenterlineLength2 / 2) * Math.cos(Math.toRadians(downdeg))));
                path.lineTo((float) (mMaxOuterRingCenter - mArrowDistance2 * Math.sin(Math.toRadians(downdeg + mAngleDifference2))), (float) (mMaxOuterRingCenter + mPadding + mArrowDistance2 * Math.cos(Math.toRadians(downdeg + mAngleDifference2))));
                path.lineTo((float) (mMaxOuterRingCenter - mArrowDistance2 * Math.sin(Math.toRadians(downdeg - mAngleDifference2))), (float) (mMaxOuterRingCenter + mPadding + mArrowDistance2 * Math.cos(Math.toRadians(downdeg - mAngleDifference2))));
                path.lineTo((float) (mMaxOuterRingCenter - (mMaxOuterRingRadius - mArrowCenterlineLength2 / 2) * Math.sin(Math.toRadians(downdeg))), (float) (mMaxOuterRingCenter + mPadding + (mMaxOuterRingRadius - mArrowCenterlineLength2 / 2) * Math.cos(Math.toRadians(downdeg))));
                mArrowPaint.setColor(mArrowClickColor);
            } else {
                path.moveTo((float) (mMaxOuterRingCenter - (mMaxOuterRingRadius - mArrowCenterlineLength / 2) * Math.sin(Math.toRadians(downdeg))), (float) (mMaxOuterRingCenter + mPadding + (mMaxOuterRingRadius - mArrowCenterlineLength / 2) * Math.cos(Math.toRadians(downdeg))));
                path.lineTo((float) (mMaxOuterRingCenter - mArrowDistance * Math.sin(Math.toRadians(downdeg + mAngleDifference))), (float) (mMaxOuterRingCenter + mPadding + mArrowDistance * Math.cos(Math.toRadians(downdeg + mAngleDifference))));
                path.lineTo((float) (mMaxOuterRingCenter - mArrowDistance * Math.sin(Math.toRadians(downdeg - mAngleDifference))), (float) (mMaxOuterRingCenter + mPadding + mArrowDistance * Math.cos(Math.toRadians(downdeg - mAngleDifference))));
                path.lineTo((float) (mMaxOuterRingCenter - (mMaxOuterRingRadius - mArrowCenterlineLength / 2) * Math.sin(Math.toRadians(downdeg))), (float) (mMaxOuterRingCenter + mPadding + (mMaxOuterRingRadius - mArrowCenterlineLength / 2) * Math.cos(Math.toRadians(downdeg))));
                mArrowPaint.setColor(mArrowColor);
            }
            canvas.drawPath(path, mArrowPaint);
        }
        for (int i = 0; i < floats.size(); i++) {
            canvas.drawLines(floats.get(i), mInnerRingBackgroundPaint);
        }
        String minStr, maxStr;
        if (mTextUnitType != null) {
            minStr = mMin + mTextUnitType;
            maxStr = mMax + mTextUnitType;
        } else {
            minStr = mMin + "";
            maxStr = mMax + "";
        }
        float x1 = mMaxOuterRingCenter - (float) ((mMaxOuterRingRadius + mOuterRingWidth) * Math.sin(Math.toRadians(mIncludedAngle)));
        float x2 = mMaxOuterRingCenter - (float) ((mMaxOuterRingRadius + mOuterRingWidth) * Math.sin(Math.toRadians(-mIncludedAngle)));
        float y = (float) ((mMaxOuterRingRadius + mOuterRingWidth) * Math.cos(Math.toRadians(mIncludedAngle)));
        canvas.drawText(minStr, x1 - mSubscriptTextPaint.measureText(minStr) / 2, mMaxOuterRingRadius + mPadding + y + mArrowSize * 2, mSubscriptTextPaint);
        canvas.drawText(maxStr, x2 - mSubscriptTextPaint.measureText(maxStr) / 2, mMaxOuterRingRadius + mPadding + y + mArrowSize * 2, mSubscriptTextPaint);
    }

    private void drawColor1(Canvas canvas) {
        for (int i = 0; i <= color1Range && i < floats.size(); i++) {
            canvas.drawLines(floats.get(i), mInnerRingColorFirstPaint);
        }
    }

    private void drawColor2(Canvas canvas) {
        for (int i = 0; i <= color2Range && i < floats.size(); i++) {
            canvas.drawLines(floats.get(i), mInnerRingColorSecondPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!mTouchable) return false;
        float dx = e.getX() - mMaxOuterRingRadius - mPadding;
        float dy = e.getY() - mMaxOuterRingRadius - mPadding;
        downdeg = (float) Math.toDegrees(Math.atan2(-dx, dy));
        if (downdeg < 0) {
            downdeg += 360;
        }
        if ((downdeg <= mIncludedAngle || downdeg >= 360 - mIncludedAngle) && e.getAction() == MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (downdeg <= mIncludedAngle) {
            downdeg = mIncludedAngle;
        } else if (downdeg >= 360 - mIncludedAngle) {
            downdeg = 360 - mIncludedAngle;
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragStatus = true;
                if (mCrollerChangeListener != null) {
                    mCrollerChangeListener.onStartTrackingTouch(this, progress());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCrollerChangeListener != null) {
                    mCrollerChangeListener.onProgressChanged(this, progress());
                }
                break;
            case MotionEvent.ACTION_UP:
                mDataFiltering = false;
                if (mCrollerChangeListener != null) {
                    mCrollerChangeListener.onStopTrackingTouch(this, progress());
                }
                mDragStatus = false;
                break;
            default:
                break;
        }
        float currdegProgress = angularRotationProgress(downdeg);
        color1Range = (int) Math.floor(currdegProgress * mInnerRingNumber);
        if (color1JumpRange != color1Range && mDragStatus) {
            color1JumpRange = color1Range;
            SoundUtil.playSound(mApp.getSoundPool(),mApp.getSoundIdByResourceId(R.raw.sound_rotate_button_se_move));
        }
        invalidate();
        return true;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * Progress of converting angles to 0~1
     *
     * @param angular
     * @return
     */
    private float angularRotationProgress(float angular) {
        if (angular == 360 - mIncludedAngle) return 1;
        if (angular == mIncludedAngle) return 0;
        return ((angular - mIncludedAngle) / (360 - 2 * mIncludedAngle));
    }

    public void setOnProgressChangedListener(onProgressChangedListener mProgressChangeListener) {
        this.mProgressChangeListener = mProgressChangeListener;
    }

    public void setCrollerChangeListener(OnCrollerChangeListener onCrollerChangeListener) {
        mCrollerChangeListener = onCrollerChangeListener;
    }

    /**
     * Set lower ring color progress (blue)
     * The lower color will be placed on the top when it is clicked
     */
    public void setColor1Progress(float value) {
        if (mDragStatus) return;
        if (!mDataFiltering) {
            mDataFiltering = true;
            return;
        }
        float progress = (float) (value - mMin) / (mMax - mMin);
        color1Range = (int) (progress * mInnerRingNumber);

        if (progress == 1) {
            downdeg = 360 - mIncludedAngle;
        } else if (progress == 0 || progress == -1) {
            downdeg = mIncludedAngle;
        } else {
            downdeg = progress * (360 - 2 * mIncludedAngle) + mIncludedAngle;
        }
        invalidate();
    }

    /**
     * Set upper ring color progress (Orange)
     *
     * @param value
     */
    public void setColor2Progress(float value) {
        float progress = (float) (value - mMin) / (mMax - mMin);
        color2Range = (int) (progress * mInnerRingNumber);
        invalidate();
    }

    /**
     * Set whether to turn on the upper color (Orange)
     */
    public void setUseColor2(boolean b) {
        mDrawSecondColorStatus = b;
        invalidate();
    }

    /**
     * Set the maximum value of the range (it will be displayed on the page, 100 by default)
     */
    public void setMax(float max) {
        mMax = max;
    }

    /**
     * Set the minimum value of the range (it will be displayed on the page, 0 by default)
     */
    public void setMin(float min) {
        mMin = min;
    }

    public void setTouchable(boolean touchable) {
        mTouchable = touchable;
        invalidate();
    }

    public int progress() {
        return (int) (angularRotationProgress(downdeg) * (mMax - mMin) + mMin);
    }

    public interface onProgressChangedListener {
        void onProgressChanged(int progress);
    }

    public interface OnCrollerChangeListener {
        void onProgressChanged(RotateButtonView croller, float progress);

        void onStartTrackingTouch(RotateButtonView croller, float progress);

        void onStopTrackingTouch(RotateButtonView croller, float progress);
    }

}
