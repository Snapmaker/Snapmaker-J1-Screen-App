package fabscreen.features.machinetools.cncassist.origin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class CNCOriginAssistantGetStartedFragment extends BaseFragment {
    public static CNCOriginAssistantGetStartedFragment newInstance() {
        return new CNCOriginAssistantGetStartedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.cnc_origin_assistant);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_origin_assistant_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_cnc_origin_assistant_intro_start)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CNCOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetMaterialFragment();
        }
    }
}
