package fabscreen.features.machinetools.setup.singlesingle.loadfilament;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public class SingleSingleLoadFilamentFragment extends BaseFragment {
    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_sub_title)
    TextView mTvSubTitle;
    @BindView(R2.id.progress)
    LinearProgressIndicator mProgress;
    @BindView(R2.id.btn_close)
    Button mBtnClose;
    @BindView(R2.id.iv_filament_intro)
    ImageView mIvFilamentIntro;
    @BindView(R2.id.tv_filament_intro)
    TextView mTvFilamentIntro;
    @BindView(R2.id.iv_filament_load)
    ImageView mIvFilamentLoad;
    @BindView(R2.id.tv_filament_load)
    TextView mTvFilamentLoad;
    @BindView(R2.id.v_tune_area)
    View mVTuneAreaBg;
    @BindView(R2.id.sb_temperature)
    AppCompatSeekBar mSbTemperature;
    @BindView(R2.id.btn_unload)
    Button mBtnUnload;
    @BindView(R2.id.btn_load)
    Button mBtnLoad;
    @BindView(R2.id.btn_start_or_complete)
    Button mBtnStartOrComplete;

    private int mCurrentStep = 0;

    public static Fragment newInstance() {
        return new SingleSingleLoadFilamentFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mProgress.setMax(1);
        refreshView();
    }

    private void refreshView() {
        mProgress.setProgress(mCurrentStep);
        switch (mCurrentStep) {
            case 0:
                mProgress.setVisibility(View.INVISIBLE);
                mIvFilamentLoad.setVisibility(View.INVISIBLE);
                mTvFilamentLoad.setVisibility(View.INVISIBLE);
                mVTuneAreaBg.setVisibility(View.INVISIBLE);
                mSbTemperature.setVisibility(View.INVISIBLE);
                mBtnLoad.setVisibility(View.INVISIBLE);
                mBtnUnload.setVisibility(View.INVISIBLE);
                mBtnStartOrComplete.setText("开始");
                break;
            case 1:
                mProgress.setVisibility(View.VISIBLE);
                mIvFilamentIntro.setVisibility(View.INVISIBLE);
                mTvFilamentIntro.setVisibility(View.INVISIBLE);
                mIvFilamentLoad.setVisibility(View.VISIBLE);
                mTvFilamentLoad.setVisibility(View.VISIBLE);
                mVTuneAreaBg.setVisibility(View.VISIBLE);
                mSbTemperature.setVisibility(View.VISIBLE);
                mBtnLoad.setVisibility(View.VISIBLE);
                mBtnUnload.setVisibility(View.VISIBLE);
                mBtnStartOrComplete.setText("完成");
                break;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_single_single_load_filament;
    }

    @OnClick(R2.id.btn_unload)
    void onUnLoadClick() {
        playNormalClickSound();
    }

    @OnClick(R2.id.btn_load)
    void onLoadClick() {
        playNormalClickSound();
    }

    @OnClick(R2.id.btn_start_or_complete)
    void onStartOrCompleteClick() {
        playNormalClickSound();
        if (mCurrentStep < 1) {
            mCurrentStep++;
            refreshView();
        } else {
            ((SingleSingleLoadFilamentActivity) requireActivity()).setResultAndFinish();
        }
    }
}
