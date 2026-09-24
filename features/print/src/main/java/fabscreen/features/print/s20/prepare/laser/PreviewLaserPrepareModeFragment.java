package fabscreen.features.print.s20.prepare.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.features.print.s20.preview.PreviewViewModel;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;

public class PreviewLaserPrepareModeFragment extends BaseFragment {

    @BindView(R2.id.iv_laser_prepare_mode_auto)
    ImageView mIvModeAuto;
    @BindView(R2.id.iv_laser_prepare_mode_manual)
    ImageView mIvModeManual;
    @BindView(R2.id.tv_laser_prepare_mode_auto_desc)
    TextView mTvModeAutoDesc;
    @BindView(R2.id.tv_laser_prepare_mode_manual_desc)
    TextView mTvModeManualDesc;
    private PreviewViewModel mViewModel;
    private int mHeadType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {
        setTitle(R.string.laser_choose_mode);
        mHeadType = mViewModel.getHeadType();
        if (mHeadType == Module.ModuleType.HEAD_LASER) {
            mIvModeAuto.setImageResource(R.drawable.pic_laser_mode_auto_240x80);
            mIvModeManual.setImageResource(R.drawable.pic_laser_mode_manual_240x80);
            mTvModeAutoDesc.setText(R.string.preview_auto_mode_desc);
            mTvModeManualDesc.setText(R.string.preview_manual_mode_desc);
        } else if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            mIvModeAuto.setImageResource(R.drawable.pic_laser_10w_mode_auto_240x80);
            mIvModeManual.setImageResource(R.drawable.pic_laser_10w_mode_manual_240x80);
            mTvModeAutoDesc.setText(R.string.preview_auto_mode_desc_10w);
            mTvModeManualDesc.setText(R.string.preview_manual_mode_desc_10w);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_mode;
    }

    @Override
    protected PreviewViewModel getViewModel() {
        return getViewModelProvider().get(PreviewViewModel.class);
    }

    @OnClick(R2.id.btn_preview_laser_prepare_auto)
    void onClickAutoMode() {
        playNormalClickSound();
        Logger.i("Choose auto focus mode.");
        PreviewActivity activity = (PreviewActivity) getContext();
        if (activity != null) {
            if (mHeadType == Module.ModuleType.HEAD_LASER) {
                activity.gotoLaserPrepareMaterialFragment();
            } else if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
                activity.gotoLaserPrepareModeNoteFragment(true);
            }
        }
    }

    @OnClick(R2.id.btn_preview_laser_prepare_manual)
    void onClickManualMode() {
        playNormalClickSound();
        Logger.i("Choose manual focus mode.");
        PreviewActivity activity = (PreviewActivity) requireActivity();
        if (mHeadType == Module.ModuleType.HEAD_LASER) {
            activity.gotoLaserPrepareSafetyGogglesFragment(false);
        } else if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            activity.gotoLaserPrepareModeNoteFragment(false);
        }
    }
}
