package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;

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
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;

public class A400CncToolChangeCompleteFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.tv_a400_calibration_complete_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_a400_calibration_complete_content)
    TextView mTvContent;
    @BindView(R2.id.btn_next)
    Button mBtnBackHome;
    @BindView(R2.id.btn_back_home)
    Button mBtnStartWork;

    public static Fragment newInstance() {
        return new A400CncToolChangeCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText(R.string.calibration_cnc_change_tool_success);
        mTvContent.setText(R.string.calibration_cnc_change_tool_success_msg);
        mBtnBackHome.setText(R.string.calibration_complete_home_screen);
        mBtnStartWork.setText(R.string.calibration_cnc_manual_tool_start_job);
        ServiceContainer.getInstance().getService(IMachine.class).getCNCController().exitCalibration(true).as(bindToLifecycle()).subscribe();
        playProcedureCompleteSound();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_complete;
    }

    @OnClick(R2.id.btn_back_home)
    void onClickNext() {
        playNormalClickSound();
        // FIXME: TODO :EXIT
        ServiceContainer.getInstance().getService(IRouter.class).routeToFilesPage(3).startAndClear(getContext());
        requireActivity().finish();
    }

    @OnClick(R2.id.btn_next)
    void onClickbackHome() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(getContext());
    }
}
