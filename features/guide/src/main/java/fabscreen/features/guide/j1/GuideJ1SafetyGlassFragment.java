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

public class GuideJ1SafetyGlassFragment extends BaseFragment {

    @BindView(R2.id.iv_show_image)
    ImageView mIvShow;

    public static Fragment newInstance() {
        return new GuideJ1SafetyGlassFragment();
    }


    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        ((J1GuideActivity) requireActivity()).checkNext();
//        getServiceContainer().getService(IMachine.class)
//                .getMachineController()
//                .getHeatedBed()
//                .requestInfo()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(heatedBedStatusResponseStructure -> {
//                    HeatedBed.HeatedBedStatus heatedBedStatus = heatedBedStatusResponseStructure.dataProp;
//
//                    RemoveGlassPlateDialogFragment.newInstance((int) heatedBedStatus.getZoneList().get(0).getCurrentTemperature())
//                            .setOnClickListener(() -> ((J1GuideActivity) requireActivity()).checkNext())
//                            .show(getChildFragmentManager(), "remove_plate");
////
//                });


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Glide.with(this).asGif().load(R.drawable.gif_leveling_bed_auxiliary_calibration_info).into(mIvShow);
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
        return R.layout.fragment_j1_guide_safety_glass;
    }
}
