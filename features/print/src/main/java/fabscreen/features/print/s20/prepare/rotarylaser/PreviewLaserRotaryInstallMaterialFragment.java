package fabscreen.features.print.s20.prepare.rotarylaser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class PreviewLaserRotaryInstallMaterialFragment extends BaseFragment {
    public static PreviewLaserRotaryInstallMaterialFragment newInstance() {
        return new PreviewLaserRotaryInstallMaterialFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.cnc_origin_assistant_install_material_title);
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
            if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W) {
                ((PreviewActivity) getActivity()).gotoLaserRotaryMeasureHeightIntro();

            } else {
                ((PreviewActivity) getActivity()).gotoLaserPrepareSafetyGogglesFragment(true);
            }
        }
    }
}
