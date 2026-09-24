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
import fabscreen.platform.base.view.BaseFragment;

public class GuideJ1InstallFanScrewsFragment extends BaseFragment {

    @BindView(R2.id.iv_show_image)
    ImageView mIvShow;

    public static Fragment newInstance() {
        return new GuideJ1InstallFanScrewsFragment();
    }


    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        ((J1GuideActivity) requireActivity()).initGuideInstallFanRecoverMachine();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Glide.with(this).load(R.drawable.pic_j1_guide_fan_install_tight_up_screws_360x408).into(mIvShow);
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().subscribeTemperatureChange();

    }

    @Override
    public void onPause() {
        super.onPause();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().unsubscribeTemperatureChange();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_guide_install_fan_screws;
    }
}
