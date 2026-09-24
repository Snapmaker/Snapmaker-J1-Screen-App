package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.new_ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class A400CncOriginAssistantInstallMaterialFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.iv_prepare_install_material)
    ImageView mIvCover;
    @BindView(R2.id.tv_laser_prepare_install_material_content)
    TextView mTvContent;
    @BindView(R2.id.tv_laser_prepare_install_material_tailstock_warning)
    TextView mTvTailstockWarning;

    public static A400CncOriginAssistantInstallMaterialFragment newInstance() {
        return new A400CncOriginAssistantInstallMaterialFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

    }

    private void initView() {
        setTitle(R.string.calibration_cnc_origin_assistant);
        mTvTopBarContent.setText("固定材料（2/9）");
        mGuideProgressBar.setMax(9);
        mGuideProgressBar.setProgress(2);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_cnc_origin_assistant_install_material;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_install_material_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((A400OriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetCarvingToolFragment();
        }
    }

}


