package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ReplaceModuleIntroFragment extends BaseFragment {

    private ReplaceModuleViewModel mViewModel;

    @BindView(R2.id.sw_work_mode)
    SwitchCompat mSwWorkMode;
    @BindView(R2.id.iv_replace_module_pic)
    ImageView mIvMainPic;

    public static Fragment newInstance() {
        return new ReplaceModuleIntroFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_replace_module;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceModuleViewModel.class);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.a400_replace_module_title));
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(18));
        Glide.with(requireContext()).load(R.drawable.pic_a400_replace_modules_intro).apply(options).into(mIvMainPic);
        mSwWorkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playSwitchSound();
            }
        });
    }

    @OnClick({R2.id.btn_start})
    @Override
    public void onClick(View view) {
        super.onClick(view);
        playNormalClickSound();
        if (view.getId() == R.id.btn_start) {
            mViewModel.startReplaceModuleMode(mSwWorkMode.isChecked())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(this::handleResult);
        }
    }

    private void handleResult(int result) {
        switch (result) {
            case 0:
                ((A400ReplaceModuleActivity) requireActivity()).goToReplaceModuleInstruction(mSwWorkMode.isChecked());
                break;
            case 1:
            case 2:
            default:
        }
    }
}
