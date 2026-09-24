package fabscreen.features.machinetools.calibration.s20._30._cnc.changeassistant;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.core.ui.view.GuideProgressBar;

public class CncChangeAssistant21Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.view_guide_progress_bar)
    GuideProgressBar mGuideProgressBar;

    public static Fragment newInstance() {
        return new CncChangeAssistant21Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("2-1 换刀助手");
        initView();
    }

    private void initView() {
        mGuideProgressBar.setmStepNum(3);
        mGuideProgressBar.setmStepIndex(2);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);

    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_change_assistant_2;
    }


    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CncChangeAssistantActivity) getActivity()).gotToCncChangeAssistant3();
        }
    }
}
