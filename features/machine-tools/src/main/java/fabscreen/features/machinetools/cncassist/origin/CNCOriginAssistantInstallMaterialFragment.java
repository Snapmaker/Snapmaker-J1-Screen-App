package fabscreen.features.machinetools.cncassist.origin;

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
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class CNCOriginAssistantInstallMaterialFragment extends BaseFragment {
    @BindView(R2.id.iv_prepare_install_material)
    ImageView mIvCover;
    @BindView(R2.id.tv_laser_prepare_install_material_content)
    TextView mTvContent;
    @BindView(R2.id.tv_laser_prepare_install_material_tailstock_warning)
    TextView mTvTailstockWarning;

    public static CNCOriginAssistantInstallMaterialFragment newInstance() {
        return new CNCOriginAssistantInstallMaterialFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.cnc_origin_assistant_install_material_title);

        mIvCover.setImageResource(R.drawable.pic_cnc_origin_assistant_install_material_360x240);
        mTvContent.setText(R.string.cnc_origin_assistant_install_material_content);
        mTvTailstockWarning.setVisibility(TextView.VISIBLE);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_install_material;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_install_material_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CNCOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetCarvingToolFragment();
        }
    }
}
