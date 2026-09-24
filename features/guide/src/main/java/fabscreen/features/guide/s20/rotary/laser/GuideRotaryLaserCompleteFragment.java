package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideRotaryLaserCompleteFragment extends BaseFragment {
    @BindView(R2.id.tv_guide_complete_content)
    TextView mTvContent;

    public static GuideRotaryLaserCompleteFragment newInstance() {
        return new GuideRotaryLaserCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        playProcedureCompleteSound();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_complete;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mTvContent.setText(R.string.guide_laser_rotary_complete_content);
    }

    @OnClick(R2.id.btn_guide_complete_next)
    void onClickNext() {
        playNormalClickSound();
        Logger.i("Guide Rotary Laser finished.");
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupRotaryLaser(true);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
