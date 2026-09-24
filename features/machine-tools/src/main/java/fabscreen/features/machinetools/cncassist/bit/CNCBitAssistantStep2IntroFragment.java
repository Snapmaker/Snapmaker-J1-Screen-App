package fabscreen.features.machinetools.cncassist.bit;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class CNCBitAssistantStep2IntroFragment extends BaseFragment {
    @BindView(R2.id.iv_cnc_bit_assistant_intro_cover)
    ImageView mIvBitAssistantIntro;
    @BindView(R2.id.tv_cnc_bit_assistant_intro_content)
    TextView mTvBitAssistantIntroContent;
    @BindView(R2.id.btn_cnc_bit_assistant_intro_next)
    Button mBtnNext;

    public static CNCBitAssistantStep2IntroFragment newInstance() {
        return new CNCBitAssistantStep2IntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.cnc_bit_assistant_bit_change_title);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_bit_assistant_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mBtnNext.setText(R.string.all_next);
        mIvBitAssistantIntro.setImageResource(R.drawable.pic_cnc_bit_assistant_step2_intro_desc_360x240);
        mTvBitAssistantIntroContent.setText(R.string.cnc_bit_assistant_step2_intro_desc);
    }

    @OnClick(R2.id.btn_cnc_bit_assistant_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CNCBitAssistantActivity) getActivity()).gotoCNCBitAssistantStep2();
        }
    }
}
