package fabscreen.features.guide.j1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GuideJ1InstallFanInfoFragment extends BaseFragment {

    @BindView(R2.id.iv_show_image)
    ImageView mIvShow;
    protected FileLoadingDialog fabMoving;
    protected FileLoadingDialog fabHoming;

    public static Fragment newInstance() {
        return new GuideJ1InstallFanInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Glide.with(this).load(R.drawable.pic_j1_guide_fan_install_info_360x408).into(mIvShow);

        fabMoving = FileLoadingDialog.create(requireContext(), true);
        fabMoving.setContent(getString(R.string.j1_dialog_guide_moving));
        fabHoming = FileLoadingDialog.create(requireContext(), true);
        fabHoming.setContent(getString(R.string.j1_dialog_guide_moving));

        checkHome();
    }

    public void checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            goHome();
        }
    }

    private void goHome() {
        fabHoming.show();
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        service.getMachineController().home(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(homeState -> {
                    fabHoming.dismiss();
                }, LogHelper::log);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_guide_install_fan_info;
    }

    @OnClick(R2.id.btn_j1_guide_fan_install_intro_go_to_install)
    public void onClickNext() {
        playNormalClickSound();
        fabMoving.show();
        Vector vector = new Vector();
        vector.setZ(30);
        MachineController machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        machineController.gotoAbsolutePosition(vector)
                .flatMap(response -> machineController.controlSwitchMotor(false))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    fabMoving.dismiss();
                    ((J1GuideActivity) requireActivity()).initGuideInstallFanScrews();
                });
    }

    @OnClick(R2.id.btn_j1_guide_fan_install_intro_skip)
    void onClickSkip() {
        playNormalClickSound();
        ((J1GuideActivity) requireActivity()).checkNext();
    }
}
