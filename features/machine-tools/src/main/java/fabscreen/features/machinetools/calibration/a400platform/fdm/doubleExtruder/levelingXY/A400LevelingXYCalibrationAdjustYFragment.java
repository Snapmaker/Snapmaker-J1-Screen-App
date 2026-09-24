package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.core.ui.view.SlidingRulerView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LevelingXYCalibrationAdjustYFragment extends A400CalibrationBaseFragment {
    A400LevelingXYViewModel mViewModel;
    @BindView(R2.id.srv_a400_leveling_xy_adjust)
    SlidingRulerView mSrvBar;
    @BindView(R2.id.btn_next)
    Button BtnNext;
    int mIndex = 0;
    private boolean isHaveCheck;

    public static Fragment newInstance() {
        return new A400LevelingXYCalibrationAdjustYFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        setTitle(R.string.calibration_a400_leveling_xy_title);
        mIvIco.setVisibility(View.GONE);

        if (getArguments() != null) {
            isHaveCheck = getArguments().getBoolean("is_have_check", false);
        }
        if (isHaveCheck) {
            mTvTopBarContent.setText(R.string.calibration_a400_leveling_xy_content_y);
            mGuideProgressBar.setMax(6);
        } else {
            mTvTopBarContent.setText(R.string.calibration_a400_leveling_xy_content_y_4);
            mGuideProgressBar.setMax(4);
        }


        mGuideProgressBar.setProgress(3);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mSrvBar.setOnProgressChangeListener(index -> mIndex = index);
        BtnNext.setText(R.string.all_save);
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        mViewModel.setAdjustY(mIndex);
        mViewModel.setAdjust().observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        if (getActivity() != null) {
                            ((A400LevelingXYCalibrationActivity) getActivity()).gotoCheckInfo();
                        }
                    }
                }, LogHelper::log);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_xy_calibration_adjust;
    }

    @Override
    protected A400LevelingXYViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingXYViewModel.class);
    }
}
