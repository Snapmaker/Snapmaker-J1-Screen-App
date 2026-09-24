package fabscreen.features.settings.a400.moduleassistant.replacehotend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;

public class CommonIntroFragment extends BaseFragment {
    public static final String KEY_OPERATION = "operation";
    public static final int INTRO = 1;
    public static final int REPLACE = 2;
    private int mOperation;
    private ReplaceHotendViewModel mViewModel;

    @BindView(R2.id.iv_intro)
    ImageView mIvIntro;
    @BindView(R2.id.tv_intro_title)
    TextView mTvIntroTitle;
    @BindView(R2.id.tv_intro_content)
    TextView mTvIntroContent;
    @BindView(R2.id.btn_start)
    Button mBtnStart;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_common_intro_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceHotendViewModel.class);
        initView();
    }

    private void initView() {
        mOperation = requireArguments().getInt(KEY_OPERATION);
        @DrawableRes int imgRes = 0;
        if (mOperation == INTRO) {
//            imgRes = R.drawable.xxx;
            mTvIntroContent.setText("The screen will guide you to replace the hot end. After the replacement, the mainboard will restart automatically.");
            mBtnStart.setText("Start");
        } else if (mOperation == REPLACE) {
//            imgRes = R.drawable.yyy;
            mTvIntroTitle.setVisibility(View.VISIBLE);
            mTvIntroTitle.setText("Replace the Hot End");
            mTvIntroContent.setText("Detach the existing hot end and attach the new hot end.");
            mBtnStart.setText("Done");
        }
//        Glide.with(requireContext()).load(imgRes).into(mIvIntro);
    }

    @OnClick(R2.id.btn_start)
    void onStartClicked() {
        playNormalClickSound();
        if (mOperation == INTRO) {
            if (requireActivity() instanceof ReplaceHotendActivity) {
                ((ReplaceHotendActivity) requireActivity()).goToReplaceHotendProcess();
            }
        } else if (mOperation == REPLACE) {
            mViewModel.restartMainboard();
        }
    }
}
