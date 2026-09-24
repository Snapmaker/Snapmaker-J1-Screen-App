package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FabConfirm;

public class CNCOriginAssistantSetOriginIntroLandFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.iv_cnc_origin_assistant_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_cnc_origin_assistant_intro_content)
    TextView mTvContent;
    @BindView(R2.id.btn_cnc_origin_assistant_intro_start)
    Button mBtnNext;
    @BindView(R2.id.view_guide_progress_bar)
    LinearProgressIndicator mGuideProgressBar;

    public static CNCOriginAssistantSetOriginIntroLandFragment newInstance() {
        return new CNCOriginAssistantSetOriginIntroLandFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGuideProgressBar.setMax(9);
        mGuideProgressBar.setProgress(4);
        setTitle(R.string.calibration_cnc_origin_assistant);
        setContent(R.string.a400_cnc_origin_operation_instruction_title);
        mIvCover.setImageResource(R.drawable.pic_cnc_origin_assistant_set_origin_intro_desc_360x240);
        mTvContent.setText(R.string.cnc_origin_assistant_set_origin_intro_content);
        mBtnNext.setText(R.string.all_next);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_origin_assistant_intro_land;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_cnc_origin_assistant_intro_start)
    void onClickNext() {
        playNormalClickSound();
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, false, true)
                .setPic(R.drawable.ic_laser_glass)
                .setType(DecisionDialog.TIP_TYPE)
                .setContent(R.string.a400_cnc_origin_open_glass_msg)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_grey_txt, (dialog, which) -> {
                    dialog.dismiss();

                })
                .setSecondTv(R.string.all_confirm, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    ((CncOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetOriginFragment();
                }).show();

    }

}
