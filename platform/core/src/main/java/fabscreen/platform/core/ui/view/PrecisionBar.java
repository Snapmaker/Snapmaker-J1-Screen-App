package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import fabscreen.platform.core.R;

public class PrecisionBar extends ConstraintLayout {
    int mMinimumRangeValue;
    int mMaximumRangeValue;
    int mMinimumAvailableValue;
    int mMaximumAvailableValue;
    TextView mTvMinimumRange;
    TextView mTvMaximumRange;
    TextView mTvCurrentValue;
    SeekBar mSeekBar;
    PrecisionBarListener mPrecisionBarListener;
    int mWidth;
    int mHeight;

    public PrecisionBar(Context context) {
        this(context, null);
    }

    public PrecisionBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrecisionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    public PrecisionBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PrecisionBar, defStyleAttr, defStyleRes);

        mMinimumRangeValue = typedArray.getInt(R.styleable.PrecisionBar_pb_minimum_range_value, 0);
        mMaximumRangeValue = typedArray.getInt(R.styleable.PrecisionBar_pb_maximum_range_value, 0);
        mMinimumAvailableValue = typedArray.getInt(R.styleable.PrecisionBar_pb_minimum_available_value, 0);
        mMaximumAvailableValue = typedArray.getInt(R.styleable.PrecisionBar_pb_maximum_available_value, 0);
        typedArray.recycle();
    }

    private void initialize(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View parentView = inflater.inflate(R.layout.view_precision_bar, this, true);

        mTvMinimumRange = parentView.findViewById(R.id.minimum_range_value);
        mTvMaximumRange = parentView.findViewById(R.id.maximum_range_value);
        mTvCurrentValue = parentView.findViewById(R.id.current_value);
        mSeekBar = parentView.findViewById(R.id.seekBar12);

        mTvMinimumRange.setText(String.format("%d", mMinimumRangeValue));
        mTvMaximumRange.setText(String.format("%d", mMaximumRangeValue));
        mSeekBar.getProgressDrawable().draw(getCanvas());
        mSeekBar.setEnabled(false);
    }

    private Canvas getCanvas() {
        mWidth = 300;
        mHeight = 100;
        Canvas canvas = new Canvas();
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        RectF r1 = new RectF();
        r1.left = 0;
        r1.right = mWidth;
        r1.top = 0;
        r1.bottom = mHeight;

        canvas.drawRoundRect(r1, 5, 5, backgroundPaint);

        Paint CheckPaint = new Paint();
        backgroundPaint.setColor(Color.RED);
        RectF r2 = new RectF();
        r2.left = mWidth / (mMaximumRangeValue - mMinimumRangeValue) * mMinimumAvailableValue;
        r2.right = mWidth / (mMaximumRangeValue - mMinimumRangeValue) * mMaximumAvailableValue;
        r2.top = 0;
        r2.bottom = mHeight;
        canvas.drawRoundRect(r2, 5, 5, CheckPaint);

        return canvas;

    }

    public void setValue(float value) {
        // FIXME: In the wrong position, floating point error is possible
        value *= 10;
        float percentage = 100f / (mMaximumRangeValue - mMinimumRangeValue);
        mSeekBar.setProgress((int) (percentage * (value - mMinimumRangeValue)));
        mTvCurrentValue.setText(value + "");
        mTvCurrentValue.setX(mSeekBar.getLeft() + (mSeekBar.getProgress() * (mSeekBar.getWidth() / 100f)) - (mTvCurrentValue.getWidth() / 2f));
        if (mPrecisionBarListener == null) return;
        mPrecisionBarListener.isWithinRange((value >= (float) mMinimumAvailableValue && value <= (float) mMaximumAvailableValue));
    }

    public void setPrecisionBarListener(PrecisionBarListener precisionBarListener) {
        mPrecisionBarListener = precisionBarListener;
    }

    public interface PrecisionBarListener {
        void isWithinRange(boolean b);
    }
}
