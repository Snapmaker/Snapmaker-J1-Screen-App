package fabscreen.features.print.prepare.laser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrepareLaserWorkHeightAutoMeasureFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("3-1 准备激光作业");
    }

    public void closePrepare() {
        requireActivity().finish();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_prepare_laser_work_height_auto_measure;
    }


    @OnClick(R2.id.btn_prepare_laser_work_height_auto_measure_next)
    void onClickNext() {
        playNormalClickSound();
        AndroidSchedulers.mainThread().scheduleDirect(this::closePrepare, 200, TimeUnit.MILLISECONDS);
        ServiceContainer.getInstance().getService(IRouter.class).routeToPrintPage().start(getContext());
    }

    @OnClick(R2.id.btn_prepare_laser_work_height_auto_measure_select_mode)
    void onClickSelectMode() {
        playNormalClickSound();
        ((PrepareLaserActivity) requireActivity()).gotoLaserWorkHeightSelectModeFragment();

    }

}

