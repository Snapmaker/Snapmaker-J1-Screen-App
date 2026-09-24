package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.core.ui.base.BaseProgressFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ReplaceModuleInstructionFragment extends BaseProgressFragment {

    @BindView(R2.id.iv_help)
    ImageView mIvHelp;
    @BindView(R2.id.sv_instruction)
    ScrollView mSvInstruction;
    @BindView(R2.id.tv_tap_help_tip)
    TextView mTvTapHelp;
    @BindView(R2.id.group_4pin_on)
    Group mGroup4pinOn;

    private ReplaceModuleViewModel mViewModel;

    public static Fragment newInstance(boolean checked) {
        Fragment fragment = new ReplaceModuleInstructionFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("keep4pinOn", checked);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceModuleViewModel.class);
        initView();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void initView() {
        initTitle();

        setProgress(1, 3);

        refreshInstructionContent();
    }

    private void initTitle() {
        setMainTitle("Replace Module");
        setSubTitle("Replace Module(1/3)");
        mIvHelp.setVisibility(View.VISIBLE);
        setIfShowClose(false);
    }

    private void refreshInstructionContent() {
        boolean keep4pinOn = requireArguments().getBoolean("keep4pinOn");
        if (keep4pinOn) {
            mGroup4pinOn.setVisibility(View.VISIBLE);
        } else {
            Drawable iconDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_a400_sub_title_help, requireActivity().getTheme());
            assert iconDrawable != null;
            iconDrawable.setBounds(0, 0, 24, 24);
            String source = getString(R.string.a400_replace_module_tap_help_tips);
            SpannableString spannableString = new SpannableString(source);
            spannableString.setSpan(new ImageSpan(iconDrawable, DynamicDrawableSpan.ALIGN_BOTTOM), source.indexOf("{icon}"), source.indexOf("{icon}") + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTvTapHelp.setText(spannableString);

            mSvInstruction.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_manual_replace_instruction;
    }

    @OnClick(R2.id.btn_next)
    @Override
    public void onClick(View view) {
        super.onClick(view);
        playNormalClickSound();
        mViewModel.restartMachine()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handleResult);
    }

    private void handleResult(int result) {
        switch (result) {
            case 0:
                goToRestarting();
                break;
            case 1:
            default:
                new SuperToastHelper.Builder()
                        .setMessage("Restart fail!")
                        .build()
                        .showToast(requireContext());
                break;
        }
    }

    private void goToRestarting() {
        if (requireActivity() instanceof A400ReplaceModuleActivity) {
            ((A400ReplaceModuleActivity) requireActivity()).goToReplaceModuleRestart();
        }
    }
}
