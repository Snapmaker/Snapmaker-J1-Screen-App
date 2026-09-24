package fabscreen.features.machinetools.calibration.j1Platform.levelingZ;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;

public class LevelingZCalibrationLInstructionsFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.tv_calibration_instructions_content)
    TextView mTvShowCount;
    @BindView(R2.id.tv_calibration_instructions_title)
    TextView mTvShowTitle;

    public static Fragment newInstance() {
        return new LevelingZCalibrationLInstructionsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

    }

    private void initView() {
        mTvShowTitle.setText(R.string.leveling_Z_calibration_L_title);
        mTvShowCount.setText(R.string.leveling_Z_calibration_L_content);
    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((LevelingZCalibrationActivity) getActivity()).gotoLevelingZCalibrationL();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_calibration_instructions;
    }

}
