package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideRotaryLaserGetStartedFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.tv_guide_laser_get_started_content)
    TextView mTvContent;
    private int mHeadType;

    public static GuideRotaryLaserGetStartedFragment newInstance() {
        return new GuideRotaryLaserGetStartedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupRotaryLaser()) {
            mBtnBack.setVisibility(View.GONE);
        }
        mHeadType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            mTvContent.setText(R.string.guide_10w_laser_safety_instructions_content);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_get_started;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_guide_laser_get_started_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
                ((GuideRotaryLaserActivity) getActivity()).startCompleteFragment();
            } else {
                ((GuideRotaryLaserActivity) getActivity()).startLaserCalibrationIntroFragment();

            }
        }
    }
}
