package fabscreen.features.guide.j1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.view.BaseFragment;

public class GuideJ1InfoFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnClose;

    public static Fragment newInstance(boolean showClose) {
        Fragment fragment = new GuideJ1InfoFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("showClose", showClose);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnClose.setVisibility(requireArguments().getBoolean("showClose") ? View.VISIBLE : View.INVISIBLE);
    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        ((J1GuideActivity) requireActivity()).checkNext();
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_guide_info;
    }
}
