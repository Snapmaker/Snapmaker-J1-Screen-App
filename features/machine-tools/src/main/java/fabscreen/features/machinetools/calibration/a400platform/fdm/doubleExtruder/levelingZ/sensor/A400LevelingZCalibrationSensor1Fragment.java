package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.sensor;

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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class A400LevelingZCalibrationSensor1Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.iv_leveling_z_ico)
    ImageView mIvIco;
    @BindView(R2.id.tv_leveling_z_content)
    TextView mTvContent;
    Disposable subscribe;
    private A400LevelingZViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400LevelingZCalibrationSensor1Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.A400LevelingZSensorCalibration(0);
    }

    private void initView() {
        mGuideProgressBar.setMax(4);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        setTitle(R.string.calibration_Z_offset_calibration_title);
        mTvTopBarContent.setText(getString(R.string.calibration_headted_z_leveing_sensor_height, getString(R.string.all_heft), 1, 4));
        mViewModel.getIsMovePopUpObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMove -> {
                    if (!isMove) return;
                    int extruderIndex = mViewModel.getExtruderIndex();
                    switch (extruderIndex) {
                        case 0:
                            setTitle(R.string.calibration_Z_offset_calibration_title);
                            mTvTopBarContent.setText(getString(R.string.calibration_headted_z_leveing_sensor_height, getString(R.string.all_heft), 1, 4));
                            mIvIco.setImageResource(R.drawable.pic_leveling_z_left);
                            mTvContent.setText(R.string.calibration_headted_z_leveing_auto_left);
                            mGuideProgressBar.setProgress(1);
                            mGuideProgressBar.invalidate();
                            break;
                        case 1:
                            setTitle(R.string.calibration_Z_offset_calibration_title);
                            mIvIco.setImageResource(R.drawable.pic_leveling_z_right);
                            mTvContent.setText(R.string.calibration_headted_z_leveing_auto_right);
                            mTvTopBarContent.setText(getString(R.string.calibration_headted_z_leveing_sensor_height, getString(R.string.all_right), 2, 4));
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
                            mViewModel.A400LevelingZSensorCalibration(1);
                            break;
                        case 1:
                            if (getActivity() == null) return;
                            ((A400LevelingZCalibrationSensorActivity) requireActivity()).initialHeightCalibration();
                            subscribe.dispose();
                            break;
                        default:
                            break;
                    }
                });
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
