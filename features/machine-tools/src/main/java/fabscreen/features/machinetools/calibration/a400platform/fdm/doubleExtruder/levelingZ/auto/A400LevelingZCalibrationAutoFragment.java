package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.auto;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.A400LevelingZViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class A400LevelingZCalibrationAutoFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.iv_leveling_z_ico)
    ImageView mIvIco;
    @BindView(R2.id.tv_leveling_z_content)
    TextView mTvContent;
    Disposable subscribe;
    private A400LevelingZViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400LevelingZCalibrationAutoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.A400LevelingZCalibration(0);
    }

    private void initView() {
        setTitle(getString(R.string.guide_a400_double_extruder_step_1_2_title));
        mTvTopBarContent.setText(R.string.guide_a400_double_extruder_step_1_for_1_3);
        mGuideProgressBar.setMax(4);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mViewModel.getIsMovePopUpObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMove -> {
                    if (!isMove) return;
                    int extruderIndex = mViewModel.getExtruderIndex();
                    switch (extruderIndex) {
                        case 0:
                            setTitle(getString(R.string.guide_a400_double_extruder_step_1_2_title));
                            mTvTopBarContent.setText(R.string.guide_a400_double_extruder_step_1_for_1_3);
                            mIvIco.setImageResource(R.drawable.pic_leveling_z_left);
                            mTvContent.setText(R.string.calibration_headted_z_leveing_auto_left);
                            mGuideProgressBar.setProgress(1);
                            mGuideProgressBar.invalidate();
                            break;
                        case 1:
                            setTitle(getString(R.string.guide_a400_double_extruder_step_1_2_title));
                            mIvIco.setImageResource(R.drawable.pic_leveling_z_right);
                            mTvContent.setText(R.string.calibration_headted_z_leveing_auto_right);
                            mTvTopBarContent.setText(R.string.guide_a400_double_extruder_step_1_for_2_3_subtitle);
                            mGuideProgressBar.setProgress(2);
                            mGuideProgressBar.invalidate();
                            break;
                        default:
                            break;
                    }

                });

        subscribe = mViewModel.getResultObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    switch (result) {
                        case 0:
                            mViewModel.A400LevelingZCalibration(1);
                            break;
                        case 1:
                            mViewModel.exitCalibration()
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(response -> finishActivityWithResultOk());
                            subscribe.dispose();
                            break;
                        default:
                            break;
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_z_calibration_1;
    }

    @Override
    protected A400LevelingZViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingZViewModel.class);
    }

}
