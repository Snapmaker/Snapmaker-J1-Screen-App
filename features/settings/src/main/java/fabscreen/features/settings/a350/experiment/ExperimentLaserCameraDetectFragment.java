package fabscreen.features.settings.a350.experiment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.data.imgprocess.LaserCalibrationProcess;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class ExperimentLaserCameraDetectFragment extends BaseFragment {
    @BindView(R2.id.iv_experiment_laser_image)
    ImageView mIvImage;
    @BindView(R2.id.iv_experiment_laser_image_output)
    ImageView mIvImageOut;
    @BindView(R2.id.tv_experiment_laser_detection)
    TextView mTvDetection;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap bitmap = BitmapFactory.decodeFile(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/capture.jpg");

        mIvImage.setImageBitmap(bitmap);

        LaserCalibrationProcess.setDebugImageView(mIvImageOut);
        int index = LaserCalibrationProcess.process(getContext(), bitmap);
        Log.d("LaserCameraDetect", "index = " + index);
        mTvDetection.setText("Detected: " + index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LaserCalibrationProcess.setDebugImageView(null);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment_laser;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }
}
