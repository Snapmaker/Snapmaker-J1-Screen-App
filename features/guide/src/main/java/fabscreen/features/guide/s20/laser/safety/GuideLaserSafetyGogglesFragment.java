package fabscreen.features.guide.s20.laser.safety;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideLaserSafetyGogglesFragment extends BaseFragment {
    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_message)
    TextView mTvContent;

    public static GuideLaserSafetyGogglesFragment newInstance() {
        return new GuideLaserSafetyGogglesFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_safety_goggles;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvContent.setText(R.string.laser_safety_goggles_message);
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((GuideLaserActivity) getActivity()).startSetOriginIntroFragment();
    }
}
