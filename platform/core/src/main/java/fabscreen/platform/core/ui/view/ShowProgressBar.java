package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class ShowProgressBar extends SeekBar {

    private boolean mTouch = false;

    public ShowProgressBar(Context context) {
        super(context);
    }

    public ShowProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setTouch(boolean touch) {
        mTouch = touch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mTouch) {
            return super.onTouchEvent(ev);
        }
        return false;

    }
}
