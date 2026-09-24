package fabscreen.features.machinetools.setup.cnc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public class A400CNCSetupFragment extends BaseFragment {

    private A400CNCSetupViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400CNCSetupFragment();
    }

    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_sub_title)
    TextView mTvSubTitle;
    @BindView(R2.id.progress)
    LinearProgressIndicator mProgress;
    @BindView(R2.id.btn_close)
    Button mBtnClose;
    @BindView(R2.id.iv_demonstrate)
    ImageView mIvDemonstrate;
    @BindView(R2.id.tv_demonstrate_desc)
    TextView mTvDemonstrateDesc;
    @BindView(R2.id.btn_start_or_next)
    Button mBtnStartOrNext;
    @BindView(R2.id.cb_demonstrate)
    CheckBox mCb;
    @BindView(R2.id.tv_demonstrate_guide)
    TextView mTvGuide;
    @BindView(R2.id.iv_guide_problem)
    ImageView mIvGuideProblem;

    private int mCurrentStep = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    @Override
    protected A400CNCSetupViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(A400CNCSetupViewModel.class);
    }

    private void initView() {
        mTvTitle.setVisibility(View.VISIBLE);
        mTvSubTitle.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.VISIBLE);
        mIvGuideProblem.setVisibility(View.GONE);
        mProgress.setMax(4);
        mCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBtnStartOrNext.setEnabled(isChecked);
            }
        });
        refreshView();
    }

    private void refreshView() {
        mProgress.setProgress(mCurrentStep + 1);
        switch (mCurrentStep) {
            case 0:
                mCb.setVisibility(View.GONE);
                mTvGuide.setVisibility(View.GONE);
                mBtnStartOrNext.setEnabled(true);
//                mIvDemonstrate.setImageResource(R.drawable.pic_guide_cnc_safety_goggles_360x320);
                mIvDemonstrate.setBackgroundResource(R.drawable.pic_guide_cnc_safety_goggles_360x320);
                mTvTitle.setText(R.string.guide_a400_cnc_initial_setup_guidance);
                mTvSubTitle.setText(R.string.guide_a400_cnc_1_4_subtitle);
                mTvDemonstrateDesc.setText(R.string.guide_a400_cnc_1_4_msg);
                mBtnStartOrNext.setText(R.string.all_next);
                break;
            case 1:
//                mIvDemonstrate.setImageResource(R.drawable.pic_guide_cnc_prepare_material_360x320);
                mIvDemonstrate.setBackgroundResource(R.drawable.pic_guide_cnc_prepare_material_360x320);
                mCb.setChecked(false);
                mBtnStartOrNext.setEnabled(false);
                mCb.setVisibility(View.VISIBLE);
                mTvGuide.setVisibility(View.VISIBLE);
                mTvGuide.setText(HtmlCompat.fromHtml(requireContext().getResources().getString(mViewModel.isRotaryAvailable() ?
                                R.string.a400_cnc_four_axial_guide_accept : R.string.a400_cnc_tri_axial_guide_accept)
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
                mTvTitle.setText(R.string.guide_a400_cnc_initial_setup_guidance);
                mTvSubTitle.setText(R.string.guide_a400_cnc_2_4_subtitle);
                mTvDemonstrateDesc.setText(HtmlCompat.fromHtml(mViewModel.isRotaryAvailable() ? getString(R.string.guide_a400_cnc_2_4_four_axial_msg)
                        : getString(R.string.guide_a400_cnc_2_4_tri_axial_msg), HtmlCompat.FROM_HTML_MODE_LEGACY));
                mBtnStartOrNext.setText(R.string.all_next);
                break;
            case 2:
                mBtnStartOrNext.setEnabled(false);
                mCb.setVisibility(View.VISIBLE);
                mCb.setChecked(false);
                mTvGuide.setVisibility(View.VISIBLE);
                mTvGuide.setText(HtmlCompat.fromHtml(requireContext().getResources().getString(mViewModel.isRotaryAvailable() ?
                                R.string.a400_cnc_four_axial_guide_accept : R.string.a400_cnc_tri_axial_guide_accept)
                        , HtmlCompat.FROM_HTML_MODE_LEGACY));
                mIvDemonstrate.setBackgroundResource(R.drawable.pic_guide_cnc_prepare_tool_head_360x320);
                mTvTitle.setText(R.string.guide_a400_cnc_initial_setup_guidance);
                mTvSubTitle.setText(R.string.guide_a400_cnc_3_4_subtitle);
                mTvDemonstrateDesc.setText(HtmlCompat.fromHtml(mViewModel.isRotaryAvailable() ? getString(R.string.guide_a400_cnc_3_4_four_axial_msg)
                        : getString(R.string.guide_a400_cnc_3_4_tri_axial_msg), HtmlCompat.FROM_HTML_MODE_LEGACY));
                mBtnStartOrNext.setText(R.string.all_next);
                break;
            case 3:
                mBtnStartOrNext.setEnabled(true);
                mCb.setVisibility(View.GONE);
                mTvGuide.setVisibility(View.GONE);
//                mIvDemonstrate.setImageResource(R.drawable.pic_guide_cnc_prepare_tool_head_360x320);
                mIvDemonstrate.setBackgroundResource(R.drawable.pic_guide_cnc_prepare_tool_head_360x320);
                mTvTitle.setText(R.string.guide_a400_cnc_initial_setup_guidance);
                mTvSubTitle.setText(R.string.guide_a400_cnc_4_4_subtitle);
                mTvDemonstrateDesc.setText(HtmlCompat.fromHtml(mViewModel.isRotaryAvailable() ? getString(R.string.guide_a400_cnc_4_4_four_axial_msg)
                        : getString(R.string.guide_a400_cnc_4_4_tri_axial_msg), HtmlCompat.FROM_HTML_MODE_LEGACY));
                mBtnStartOrNext.setText(R.string.all_done);
                break;
        }
    }

    @OnClick(R2.id.btn_start_or_next)
    void onStartOrNextClicked() {
        playNormalClickSound();
        if (mCurrentStep < 3) {
            mCurrentStep++;
            refreshView();
        } else {
            finishActivityWithResultOk();
        }
    }

    @OnClick(R2.id.btn_close)
    void onCloseClicked() {
        playNormalClickSound();
        back();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_guide_setup;
    }
}
