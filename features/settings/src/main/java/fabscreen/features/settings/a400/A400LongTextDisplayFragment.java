package fabscreen.features.settings.a400;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;

public class A400LongTextDisplayFragment extends BaseFragment {

    public static A400LongTextDisplayFragment newInstance(int titleRes, int contentRes) {
        A400LongTextDisplayFragment fragment = new A400LongTextDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("titleRes", titleRes);
        bundle.putInt("contentRes", contentRes);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_content)
    TextView mTvContent;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText(requireArguments().getInt("titleRes"));
        mTvContent.setText(requireArguments().getInt("contentRes"));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_long_text_display;
    }

    @OnClick(R2.id.btn_user_improvement_program_back)
    public void onClickBack() {
        back();
    }

}
