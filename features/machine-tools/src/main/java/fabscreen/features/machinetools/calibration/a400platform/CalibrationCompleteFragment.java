package fabscreen.features.machinetools.calibration.a400platform;

import static fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity.CalibrationType.BED_LEVELING_AUTO;
import static fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity.CalibrationType.CAMERA_CALI;
import static fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity.CalibrationType.DUAL_EXTRUDER_XY;
import static fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity.CalibrationType.THK_MEASURE;
import static fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity.CalibrationType.Z_CALI_AUTO;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.ViewUtils;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CalibrationCompleteFragment extends BaseFragment {

    private CalibrationCompleteViewModel mViewModel;

    public static Fragment newInstance(int type) {
        Fragment fragment = new CalibrationCompleteFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R2.id.tv_a400_calibration_complete_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_a400_calibration_complete_content)
    TextView mTvContent;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        Bundle arguments = getArguments();
        int type = Objects.requireNonNull(arguments).getInt("type");
        initView(type);
        playProcedureCompleteSound();
    }

    private void initView(int type) {
        mViewModel.saveAndExitCalibration()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isExiting -> ViewUtils.enableButtons((ViewGroup) requireView(), !isExiting), LogHelper::log);

        setTitleAndContent(type);
    }

    private void setTitleAndContent(int type) {
        String title = "";
        String content = "";

        switch (type) {
            case Z_CALI_AUTO:
                title = getString(R.string.all_calibration_completed);
                content = "Congrats! You have completed the Z Offset Calibration.";
                break;

            case BED_LEVELING_AUTO:
                title = getString(R.string.all_calibration_completed);
                content = "Congrats! You have completed the Heated Bed Leveling.";
                break;

            case DUAL_EXTRUDER_XY:
                title = getString(R.string.all_calibration_completed);
                content = getString(R.string.calibration_complete_dual_extruder_xy);
                break;

            case THK_MEASURE:
                title = getString(R.string.all_calibration_completed);
                content = getString(R.string.calibration_complete_thk_measure);
                break;

            case CAMERA_CALI:
                title = getString(R.string.all_calibration_completed);
                content = getString(R.string.calibration_complete_camera_cali);
                break;
        }

        mTvTitle.setText(title);
        mTvContent.setText(content);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_complete;
    }

    @OnClick({R2.id.btn_next, R2.id.btn_back_home})
    public void onClick(View view) {
        playNormalClickSound();
        int id = view.getId();

        if (id == R.id.btn_next) {
            back();
        } else if (id == R.id.btn_back_home) {
            mRouter.backHome().start(requireContext());
        }
    }

    @Override
    protected CalibrationCompleteViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(CalibrationCompleteViewModel.class);
    }
}
