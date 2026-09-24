package fabscreen.features.print.s20.prepare.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.features.print.s20.preview.PreviewViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.XYZControlPanel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PreviewLaserPrepareTouchMaterialFragment extends BaseFragment {

    @BindView(R2.id.xyz_panel_touch_material)
    XYZControlPanel mControlPanel;
    @BindView(R2.id.btn_touch_material_next)
    Button mBtnNext;
    private PreviewViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_touch_material_title);

        mControlPanel.setStepWidths(0.1f, 1f, 10f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    mViewModel.moveXYZByStep(direction, stepWidth);
                });

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mControlPanel.setXYEnabled(!isMoving);
                    mControlPanel.setZEnabled(!isMoving);
                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_touch_material;
    }

    @Override
    protected PreviewViewModel getViewModel() {
        return getViewModelProvider().get(PreviewViewModel.class);
    }

    @OnClick(R2.id.btn_touch_material_next)
    void onNextClicked() {
        playNormalClickSound();
        mViewModel.save10wMeasuredThickness(mViewModel.getMeasuredThicknessByTouch());
        mViewModel.liftToolhead(false)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    PreviewActivity activity = (PreviewActivity) requireActivity();
                    activity.gotoLaserPrepareSafetyGogglesFragment(false);
                });
    }
}
