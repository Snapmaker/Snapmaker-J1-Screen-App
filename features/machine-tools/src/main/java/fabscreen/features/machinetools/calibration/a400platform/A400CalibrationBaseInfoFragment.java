package fabscreen.features.machinetools.calibration.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public abstract class A400CalibrationBaseInfoFragment extends BaseFragment {
    @BindView(R2.id.cl_calibration_check_mode)
    protected ConstraintLayout mClCheckMode;
    @BindView(R2.id.tv_calibration_check_mode)
    protected TextView mTvCheckMode;
    @BindView(R2.id.iv_calibration_info_show)
    protected ImageView mIvInfoShow;
    @BindView(R2.id.tv_calibration_info_show_title)
    protected TextView mTvInfoShowTitle;
    @BindView(R2.id.tv_calibration_info_show_content)
    protected TextView mTvInfoShowContent;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_base_info;
    }
}
