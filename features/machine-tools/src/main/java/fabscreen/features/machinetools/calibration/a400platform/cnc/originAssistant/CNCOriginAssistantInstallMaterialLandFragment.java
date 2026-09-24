package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class CNCOriginAssistantInstallMaterialLandFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.iv_prepare_install_material)
    ImageView mIvCover;
    @BindView(R2.id.tv_laser_prepare_install_material_content)
    TextView mTvContent;
    @BindView(R2.id.tv_laser_prepare_install_material_tailstock_warning)
    TextView mTvTailstockWarning;
    @BindView(R2.id.view_guide_progress_bar)
    LinearProgressIndicator mGuideProgressBar;

    public static CNCOriginAssistantInstallMaterialLandFragment newInstance() {
        return new CNCOriginAssistantInstallMaterialLandFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGuideProgressBar.setMax(9);
        mGuideProgressBar.setProgress(2);
        setTitle(R.string.calibration_cnc_origin_assistant);
        setContent(getString(R.string.a400_cnc_origin_fix_material_title));

        mIvCover.setImageResource(R.drawable.pic_cnc_origin_assistant_install_material_360x240);
        mTvTailstockWarning.setVisibility(TextView.VISIBLE);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_install_material_land;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_install_material_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CncOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetCarvingToolFragment();
        }
    }

}


