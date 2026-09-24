package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class A400ProgressButton extends View {
    private int mDownloaded = 0;
    private int mMax = 0;
    private Paint mStrokePaint;
    private Paint mSolidPaint;
    private RectF mStrokeRectF;
    private RectF mSolidRectF;
    private State mState = State.IDLE;
    private Paint mTextPaint;

    public enum State {
        // click -> download
        IDLE,
        // click -> pause/stop? (now do nothing)
        DOWNLOADING,
        // click -> install
        DOWNLOADED
    }

    public A400ProgressButton(Context context) {
        this(context, null);
    }

    public A400ProgressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public A400ProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Shader shader = new LinearGradient(0, 0, 230, 0, 0xFF1A41F5, 0xFF1A8CF5, Shader.TileMode.CLAMP);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(4);
        mStrokePaint.setShader(shader);

        mSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSolidPaint.setStyle(Paint.Style.FILL);
        mSolidPaint.setShader(shader);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(28f);
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mStrokeRectF = new RectF(2, 2, 228, 90);
        mSolidRectF = new RectF(0, 0, 230, 92);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textTop = fontMetrics.top;
        float textBottom = fontMetrics.bottom;
        float baselineCenterDistance = (textBottom - textTop) / 2 - textBottom;
        int baseLineY = (int) (46 + baselineCenterDistance);

        if (mState == State.IDLE) {
            canvas.save();
            canvas.drawRoundRect(mSolidRectF, 46, 46, mSolidPaint);
            canvas.drawText("Download Now", 115, baseLineY, mTextPaint);
            canvas.restore();
        } else if (mState == State.DOWNLOADING) {
            canvas.save();
            canvas.drawRoundRect(mStrokeRectF, 46, 46, mStrokePaint);
            canvas.clipRect(0, 0, mMax == 0 ? 0 : 230 * mDownloaded / mMax, 92);
            canvas.drawRoundRect(mSolidRectF, 46, 46, mSolidPaint);
            canvas.restore();
            canvas.drawText(mDownloaded + "M / " + mMax + "M", 115, baseLineY, mTextPaint);
        } else if (mState == State.DOWNLOADED) {
            canvas.save();
            canvas.drawRoundRect(mSolidRectF, 46, 46, mSolidPaint);
            canvas.drawText("Update", 115, baseLineY, mTextPaint);
            canvas.restore();
        }
    }

    public void setDownloadedSize(int downloaded) {
        mDownloaded = downloaded;
        invalidate();
    }

    public void setMaxSize(int max) {
        mMax = max;
    }

    public void setState(State state) {
        mState = state;
        invalidate();
    }
}
