package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.core.R;

public class ChessboardView extends View {
    protected int mRows = 5;
    protected int mColumn = 5;

    protected float mAverageWidth = dp2px(5);
    protected float mAverageHeight = dp2px(5);
    protected float mChessboardLineThickness = dp2px(5);
    protected float mChessboardLineMargin = dp2px(32);
    protected float mPawnDiameter = dp2px(12);
    protected float mCheckDiameter = dp2px(16);

    protected int mChessboardLineColor = Color.parseColor("#090909");
    protected int mUnprocessedPieceColor = Color.parseColor("#090909");
    protected int mProcessingPieceBackGroundColor = Color.parseColor("#E62E2E2E");
    protected int mProcessedPieceColor = Color.parseColor("#6E6E6E");
    protected Paint mChessboardLinePaint;
    protected Paint mUnprocessedPiecePaint;
    protected Paint mProcessingPiecePaint;
    protected Paint mProcessingPieceBackGroundPaint;
    protected Paint mProcessedPiecePaint;
    private int mProcessingPieceStartColor = Color.parseColor("#1A41F5");
    private int mProcessingPieceEndColor = Color.parseColor("#1A8CF5");
    private int mWidth;
    private int mHeight;
    private List<ProcessedPiece> mProcessedPieces = new ArrayList<>();


    public ChessboardView(Context context) {
        this(context, null);
    }

    public ChessboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ChessboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initAttrs(attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ChessboardView, defStyleAttr, defStyleRes);

        mRows = typedArray.getInteger(R.styleable.ChessboardView_cv_rows, 5);
        mColumn = typedArray.getInteger(R.styleable.ChessboardView_cv_colums, 5);
        mChessboardLineThickness = typedArray.getFloat(R.styleable.ChessboardView_cv_chessboard_line_thickness, 4);
        mChessboardLineMargin = typedArray.getFloat(R.styleable.ChessboardView_cv_chessboard_margin, 32);
        mPawnDiameter = typedArray.getFloat(R.styleable.ChessboardView_cv_pawn_diameter, 12);
        mCheckDiameter = typedArray.getFloat(R.styleable.ChessboardView_cv_check_diameter, 16);
        mChessboardLineColor = typedArray.getColor(R.styleable.ChessboardView_cv_chessboard_line_color, Color.parseColor("#090909"));
        mUnprocessedPieceColor = typedArray.getColor(R.styleable.ChessboardView_cv_unprocessed_pieces_color, Color.parseColor("#090909"));
        mProcessingPieceStartColor = typedArray.getColor(R.styleable.ChessboardView_cv_processing_pieces_start_color, Color.parseColor("#1A41F5"));
        mProcessingPieceEndColor = typedArray.getColor(R.styleable.ChessboardView_cv_processing_pieces_end_color, Color.parseColor("#1A8CF5"));
        mProcessingPieceBackGroundColor = typedArray.getColor(R.styleable.ChessboardView_cv_processing_pieces_background_color, Color.parseColor("#E62E2E2E"));
        mProcessedPieceColor = typedArray.getColor(R.styleable.ChessboardView_cv_processed_line_color, Color.parseColor("#6E6E6E"));
        mRows -= 1;
        mColumn -= 1;
        typedArray.recycle();
    }

    private void initialize() {
        mChessboardLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChessboardLinePaint.setStyle(Paint.Style.STROKE);
        mChessboardLinePaint.setStrokeWidth(mChessboardLineThickness);
        mChessboardLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mChessboardLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mChessboardLinePaint.setColor(mChessboardLineColor);

        mUnprocessedPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnprocessedPiecePaint.setStyle(Paint.Style.FILL);
        mUnprocessedPiecePaint.setColor(mUnprocessedPieceColor);

        mProcessingPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProcessingPiecePaint.setStyle(Paint.Style.FILL);
        mProcessingPiecePaint.setShader(new LinearGradient(0, 0, mCheckDiameter, mCheckDiameter, mProcessingPieceStartColor, mProcessingPieceEndColor, Shader.TileMode.MIRROR));

        mProcessingPieceBackGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProcessingPieceBackGroundPaint.setStyle(Paint.Style.FILL);
        mProcessingPieceBackGroundPaint.setColor(mProcessingPieceBackGroundColor);

        mProcessedPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProcessedPiecePaint.setStyle(Paint.Style.FILL);
        mProcessedPiecePaint.setColor(mProcessedPieceColor);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mAverageWidth = (mWidth - mChessboardLineMargin * 2 - mPawnDiameter) / mColumn;
        mAverageHeight = (mHeight - mChessboardLineMargin * 2 - mPawnDiameter) / mRows;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float normalMargin = mChessboardLineMargin + mPawnDiameter / 2;
        float checkMargin = mChessboardLineMargin + mCheckDiameter / 2;

        for (int i = 0; i <= mRows; i++) {
            canvas.drawLine(normalMargin, normalMargin + mAverageHeight * i, mWidth - normalMargin, normalMargin + mAverageHeight * i, mChessboardLinePaint);
        }
        for (int i = 0; i <= mColumn; i++) {
            canvas.drawLine(normalMargin + mAverageWidth * i, normalMargin, normalMargin + mAverageWidth * i, mHeight - normalMargin, mChessboardLinePaint);
        }

        for (int i = 0; i < mProcessedPieces.size(); i++) {
            int indexX = i % (mColumn + 1);
            int indexY = i / (mColumn + 1);
            Paint paint = null;
            boolean isProcessing = false;
            switch (mProcessedPieces.get(i).processedPieceState) {
                case PROCESSED:
                    paint = mProcessedPiecePaint;
                    break;
                case PROCESSING:
                    isProcessing = true;
                    canvas.drawCircle(mAverageWidth * indexX + checkMargin, mHeight - (checkMargin + mAverageHeight * indexY), mCheckDiameter + dp2px(6), mProcessingPieceBackGroundPaint);
                    paint = mProcessingPiecePaint;
                    break;
                case UNPROCESSED:
                    paint = mUnprocessedPiecePaint;
                    break;
            }
            canvas.drawCircle(mAverageWidth * indexX + (isProcessing ? checkMargin : normalMargin), mHeight - (mAverageHeight * indexY + (isProcessing ? checkMargin : normalMargin)), (isProcessing ? mCheckDiameter : mPawnDiameter), paint);
        }
    }

    public void changeChessboard(int rows, int column) {
        mRows = rows - 1;
        mColumn = column - 1;
        mAverageWidth = (float) mWidth / mColumn;
        mAverageHeight = (float) mHeight / mRows;
        invalidate();
    }

    public void setData(int rows, int column, List<ProcessedPiece> processedPieceList) {
        mRows = rows - 1;
        mColumn = column - 1;
        mProcessedPieces = processedPieceList;
        mAverageWidth = (mWidth - mChessboardLineMargin * 2 - mPawnDiameter) / mColumn;
        mAverageHeight = (mHeight - mChessboardLineMargin * 2 - mPawnDiameter) / mRows;
        invalidate();
    }

    public void setProcessedPieceList(List<ProcessedPiece> processedPieceList) throws Exception {
        if (processedPieceList.size() > (mRows + 1) * (mColumn + 1))
            throw new Exception("Inconsistent number of chessboards");
        mProcessedPieces = processedPieceList;
        invalidate();
    }

    public void setChangPieceState(int index1, ProcessedPieceState processedPieceState1, int index2, ProcessedPieceState processedPieceState2) {
        mProcessedPieces.get(index1 - 1).processedPieceState = processedPieceState1;
        mProcessedPieces.get(index2 - 1).processedPieceState = processedPieceState2;
        invalidate();
    }


    public enum ProcessedPieceState {
        UNPROCESSED,
        PROCESSING,
        PROCESSED
    }

    public static class ProcessedPiece {
        public ProcessedPieceState processedPieceState;

        public ProcessedPiece() {
            this(ProcessedPieceState.UNPROCESSED);
        }

        public ProcessedPiece(ProcessedPieceState processedPieceState) {
            this.processedPieceState = processedPieceState;
        }
    }
}

