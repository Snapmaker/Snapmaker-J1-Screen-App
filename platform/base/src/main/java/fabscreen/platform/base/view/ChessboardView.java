package fabscreen.platform.base.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ChessboardView extends View {

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

    }

    private void initialize() {
    }


}
