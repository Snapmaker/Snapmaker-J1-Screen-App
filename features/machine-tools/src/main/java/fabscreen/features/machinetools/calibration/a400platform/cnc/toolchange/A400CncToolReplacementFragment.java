package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;

public class A400CncToolReplacementFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.fragment_calibration_image)
    ImageView mIvImage;
    @BindView(R2.id.fragment_calibration_title)
    TextView mTvTitle;
    @BindView(R2.id.fragment_calibration_content)
    TextView mTvContent;
    @BindView(R2.id.bt_a400_calibration_submit)
    Button mBtnCalibration;

    public static Fragment newInstance() {
        return new A400CncToolReplacementFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        setTitle(R.string.calibration_cnc_tool_change);
        mTvTopBarContent.setText(R.string.calibration_cnc_tool_change_second_steps);
//        mTvTitle.setText("更换刀具");
        mTvContent.setText(R.string.chang_tool_message);
        mBtnCalibration.setText(R.string.all_next);
        mGuideProgressBar.setMax(3);
        mGuideProgressBar.setProgress(2);
        mGuideProgressBar.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.bt_a400_calibration_submit)
    public void onClickSubmit() {
        playNormalClickSound();
        ((A400CncToolChangeAssistantActivity) requireActivity()).gotoSetZ();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_cnc_manual_tool_replacement;
    }

}
