package fabscreen.features.print.s20.prepare.safety;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PreviewLaserPrepareSafetyGogglesFragment extends BaseFragment {

    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_message)
    TextView mTvMessage;
    @BindView(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    Button mBtnNext;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        CoordinateSystemPresenter coordinateSystemPresenter = new CoordinateSystemPresenter(getContext(), getModel(), disposables);
//        coordinateSystemPresenter.ensureCoordinate(1);

        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        if (headType == Module.ModuleType.HEAD_LASER_10W) {
            mTvMessage.setText(R.string.laser_10w_safety_goggles_message);
        } else {
            mTvMessage.setText(R.string.laser_safety_goggles_message);
        }
        mTvTitle.setText(R.string.laser_safety_goggles);
        mBtnNext.setText(R.string.all_next);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_safety_goggles;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    void onClickNext() {
        playNormalClickSound();
        PreviewActivity activity = (PreviewActivity) requireActivity();
        if (getArguments() != null) {
            boolean autoMode = getArguments().getBoolean("auto_mode");
            if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
                activity.gotoLaserRotarySetOriginFragment(autoMode);
            } else {
                if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W) {
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                            .getHeaderSecurityStatus()
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(headerSecurity -> {
                                if (headerSecurity.status == 0) {
                                    activity.gotoLaserPrepareSetOriginFragment(autoMode);
                                }
                            });
                } else {
                    activity.gotoLaserPrepareSetOriginFragment(autoMode);
                }
            }
        }
    }
}
