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

public class CNCBitAssistantCompleteFragment extends BaseFragment {
    @BindView(R2.id.tv_guide_complete_content)
    TextView mTvContent;

    public static CNCBitAssistantCompleteFragment newInstance() {
        return new CNCBitAssistantCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        playProcedureCompleteSound();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_complete_land;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mTvContent.setText(R.string.cnc_bit_assistant_complete_desc);
    }

    @OnClick(R2.id.btn_guide_complete_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
