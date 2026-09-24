package fabscreen.features.guide.j1;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.view.BaseFragment;

public class GuideJ1SuccessfullyFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new GuideJ1SuccessfullyFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        requireActivity().finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_guide_success;
    }
}
