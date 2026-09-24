package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class CNCOriginAssistantGetStartedLandFragment extends A400CalibrationBaseFragment {
    public static CNCOriginAssistantGetStartedLandFragment newInstance() {
        return new CNCOriginAssistantGetStartedLandFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.cnc_origin_assistant);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_origin_assistant_intro_land;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_cnc_origin_assistant_intro_start)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CncOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetMaterialFragment();
        }
    }

}
