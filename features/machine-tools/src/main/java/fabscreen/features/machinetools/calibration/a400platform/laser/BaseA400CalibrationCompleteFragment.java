package fabscreen.features.machinetools.calibration.a400platform.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public class BaseA400CalibrationCompleteFragment extends BaseFragment {

    @BindView(R2.id.tv_a400_calibration_complete_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_a400_calibration_complete_content)
    TextView mTvContent;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected  void setCompleteTitle(String title) {
        mTvTitle.setText(title);
    }

    protected void setCompleteContent(String content) {
        mTvContent.setText(content);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_complete;
    }

    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        playNormalClickSound();
        back();
    }

    @OnClick(R2.id.btn_back_home)
    void onBackHomeClicked() {
        playNormalClickSound();
        mRouter.backHome().start(requireContext());
    }
}
