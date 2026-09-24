package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R;

public class J1ProgressButton extends View {
    private int mDownloaded = 0;
    private int mMax = 0;
    private Paint mStrokePaint;
    private Paint mSolidPaint;
    private RectF mStrokeRectF;
    private RectF mSolidRectF;
    private State mState = State.IDLE;
    private Paint mTextPaint;
    private Context mContext;

    public enum State {
        // click -> download
        IDLE,
        // click -> pause/stop? (now do nothing)
        DOWNLOADING,
        // click -> install
        DOWNLOADED
    }

    public J1ProgressButton(Context context) {
        this(context, null);
        mContext = context;
    }

    public J1ProgressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public J1ProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        Shader shader = new LinearGradient(DimensUtils.dp2px(60), 0, DimensUtils.dp2px(25), DimensUtils.dp2px(48), 0xFFF56A00, 0xFFFFAB00, Shader.TileMode.CLAMP);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(DimensUtils.dp2px(1.5f));
        mStrokePaint.setShader(shader);

        mSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSolidPaint.setStyle(Paint.Style.FILL);
        mSolidPaint.setShader(shader);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mStrokeRectF = new RectF(DimensUtils.dp2px(0.75f), DimensUtils.dp2px(0.75f), DimensUtils.dp2px(139.25f), DimensUtils.dp2px(47.25f));
        mSolidRectF = new RectF(0, 0, DimensUtils.dp2px(140), DimensUtils.dp2px(48));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textTop = fontMetrics.top;
        float textBottom = fontMetrics.bottom;
        float baselineCenterDistance = (textBottom - textTop) / 2 - textBottom;
        int baseLineY = (int) (DimensUtils.dp2px(24) + baselineCenterDistance);

        if (mState == State.IDLE) {
            canvas.save();
            canvas.drawRoundRect(mSolidRectF, DimensUtils.dp2px(24), DimensUtils.dp2px(24), mSolidPaint);
            canvas.drawText(mContext.getString(R.string.all_download), DimensUtils.dp2px(70), baseLineY, mTextPaint);
            canvas.restore();
        } else if (mState == State.DOWNLOADING) {
            canvas.save();
            canvas.drawRoundRect(mStrokeRectF, DimensUtils.dp2px(24), DimensUtils.dp2px(24), mStrokePaint);
            canvas.clipRect(0, 0, mMax == 0 ? 0 : DimensUtils.dp2px(140) * mDownloaded / mMax, DimensUtils.dp2px(48));
            canvas.drawRoundRect(mSolidRectF, DimensUtils.dp2px(24), DimensUtils.dp2px(24), mSolidPaint);
            canvas.restore();
            canvas.drawText(mContext.getString(R.string.all_unit_m_has_diagonal, mDownloaded + "") + mContext.getString(R.string.all_unit_m, mMax + ""), DimensUtils.dp2px(70), baseLineY, mTextPaint);
        } else if (mState == State.DOWNLOADED) {
            canvas.save();
            canvas.drawRoundRect(mSolidRectF, DimensUtils.dp2px(24), DimensUtils.dp2px(24), mSolidPaint);
            canvas.drawText(mContext.getString(R.string.all_update), DimensUtils.dp2px(70), baseLineY, mTextPaint);
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
