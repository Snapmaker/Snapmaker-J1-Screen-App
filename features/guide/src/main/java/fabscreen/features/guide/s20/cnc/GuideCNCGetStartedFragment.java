package fabscreen.features.guide.s20.cnc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideCNCGetStartedFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;

    public static GuideCNCGetStartedFragment newInstance() {
        return new GuideCNCGetStartedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupCNC()) {
            mBtnBack.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_cnc_get_started;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_guide_cnc_get_started_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((GuideCNCActivity) getActivity()).startSafetyGogglesFragment();
        }
    }
}
