package fabscreen.features.machinetools.setup.laser.tenw;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SetupIntroFragment extends BaseFragment {
    @BindView(R2.id.iv_setup_intro)
    ImageView mIvSetupIntro;
    @BindView(R2.id.tv_setup_intro)
    TextView mTvSetupIntro;
    private SetupIntroViewModel mViewModel;
    @BindView(R2.id.btn_start)
    Button mBtnStart;

    public static Fragment newInstance(Bundle pageData) {
        Fragment fragment = new SetupIntroFragment();
        fragment.setArguments(pageData);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(SetupIntroViewModel.class);
        initView();
    }

    private void initView() {
        mBtnStart.setText(R.string.all_start);
        mTvSetupIntro.setText(requireArguments().getString("desc"));
//        Glide.with(getActivity()).load(R.drawable.ssdf).apply(RequestOptions.bitmapTransform(new RoundedCorners(18))).into(mIvSetupIntro);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_setup_intro;
    }

    @OnClick(R2.id.btn_start)
    void onStartClicked() {
        playNormalClickSound();
        // set mode and go
        mViewModel.setMode(Objects.requireNonNull(requireArguments().getString("router_destination")))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    if (result == 0) {
                        ((SetupIntroActivity) requireActivity()).goToDestinationForResult();
                    }
                });
    }
}
