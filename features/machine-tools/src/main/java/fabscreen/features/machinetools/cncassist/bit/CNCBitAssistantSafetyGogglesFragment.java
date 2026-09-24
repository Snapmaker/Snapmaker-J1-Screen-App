package fabscreen.features.machinetools.cncassist.bit;

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
import fabscreen.platform.base.view.BaseViewModel;

public class CNCBitAssistantSafetyGogglesFragment extends BaseFragment {
    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_message)
    TextView mTvContent;

    public static CNCBitAssistantSafetyGogglesFragment newInstance() {
        return new CNCBitAssistantSafetyGogglesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_safety_goggles;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mTvTitle.setText(R.string.all_safety_goggles);
        mTvContent.setText(R.string.guide_cnc_safety_goggles_content);
    }

    @OnClick(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CNCBitAssistantActivity) getActivity()).gotoCNCBitAssistantStep1();
        }
    }
}
