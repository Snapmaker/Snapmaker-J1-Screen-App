package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.new_ui;

import static fabscreen.platform.base.RoutePath.TOOLS_CALIBRATION_A400_CNC_ORIGIN_ASSISTANT;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationBaseInfoFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400OriginAssistantInfoFragment extends A400CalibrationBaseInfoFragment {
    protected IPreferences.Helper helper;

    public static Fragment newInstance() {
        return new A400OriginAssistantInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        mIvInfoShow.setImageResource(R.drawable.pic_a400_origin_assistant_info);
        mTvInfoShowTitle.setText(R.string.calibration_cnc_origin_assistant);
        mTvCheckMode.setText("圆柱材料");
        mClCheckMode.setVisibility(View.GONE);
        mTvInfoShowContent.setText(R.string.calibration_cnc_origin_assistant_content);
    }

    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getCNCController()
                .setCalibrationMode(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        ServiceContainer.getInstance().getService(IRouter.class)
                                .routeWithClassPath(TOOLS_CALIBRATION_A400_CNC_ORIGIN_ASSISTANT)
                                .start(getContext());
                    }
                }, LogHelper::log);
    }

}
