package fabscreen.platform.base.lib.hook;

import android.view.View;

public class ProxyOnClickListener implements View.OnClickListener {
    View.OnClickListener mListener;

    public ProxyOnClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        // play sound

        if (mListener != null) {
            mListener.onClick(v);
        }
    }
}
