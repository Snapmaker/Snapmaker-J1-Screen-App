package fabscreen.platform.core.ui.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R2;

public abstract class BaseProgressFragment extends BaseFragment {
    @BindView(R2.id.tv_main_title)
    TextView mTvMainTitle;
    @BindView(R2.id.tv_sub_title)
    TextView mTvSubtitle;
    @BindView(R2.id.progress)
    LinearProgressIndicator mProgress;
    @BindView(R2.id.iv_close)
    ImageView mIvClose;

    protected void setMainTitle(String title) {
        mTvMainTitle.setText(title);
    }

    protected void setSubTitle(String title) {
        mTvSubtitle.setText(title);
    }

    protected void setProgress(int cur, int max) {
        mProgress.setMax(max);
        mProgress.setProgress(cur);
    }

    protected void setIfShowClose(boolean show) {
        mIvClose.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
