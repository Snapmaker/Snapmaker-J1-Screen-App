package fabscreen.platform.core.ui.presenter;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

public class TextFragment extends BaseFragment {

    @BindView(R2.id.tv_center_text)
    TextView mTvCenterText;

    private String mTitle;
    private String mText;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(mTitle);
        mTvCenterText.setText(mText);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_text;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    public void setTitle(String title) {
        super.setTitle(title);
        mTitle = title;
    }

    public void setText(String text) {
        mText = text;
    }
}
