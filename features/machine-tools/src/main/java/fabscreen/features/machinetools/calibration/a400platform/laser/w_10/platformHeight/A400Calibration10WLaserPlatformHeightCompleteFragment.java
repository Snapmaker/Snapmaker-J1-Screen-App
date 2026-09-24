package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.platformHeight;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;

public class A400Calibration10WLaserPlatformHeightCompleteFragment extends BaseFragment {
    @BindView(R2.id.tv_a400_calibration_complete_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_a400_calibration_complete_content)
    TextView mTvContent;
    @BindView(R2.id.top_bar_back)
    Button mTopBarBack;


    public static Fragment newInstance() {
        return new A400Calibration10WLaserPlatformHeightCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvTitle.setText(R.string.calibration_platform_height_calibration_10w_title);
        mTvContent.setText(R.string.calibration_complete_platform_height);
        mTopBarBack.setVisibility(View.GONE);
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().exitCalibration(true).as(bindToLifecycle()).subscribe();
        playProcedureCompleteSound();
    }

    @Override
    protected void back() {
        ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
        requireActivity().finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_complete;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        getActivity().finish();
    }

    @OnClick(R2.id.btn_back_home)
    void onClickbackHome() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(getContext());
    }

}
