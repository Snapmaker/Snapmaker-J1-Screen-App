package fabscreen.features.filemanager.a400platform;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.features.filemanager.BrowseFileDetailViewModel;
import fabscreen.features.filemanager.DetailDesc;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.WarmTipDialog;
import fabscreen.platform.core.ui.view.FileParsingDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BrowseA400FileDetailFragment extends BaseFragment implements View.OnTouchListener {

    @BindView(R2.id.tv_browse_j1_file_detail_filename)
    TextView mTvFilename;
    @BindView(R2.id.tv_browse_j1_file_detail_info)
    TextView mTvFileInfo;
    @BindView(R2.id.tv_browse_j1_file_detail_image)
    ImageView mIvFileImage;
    @BindView(R2.id.gl_browse_j1_file_detail_desc)
    GridLayout mGvDetailDesc;
    @BindView(R2.id.btn_browse_j1_file_detail_start)
    Button startBtn;

    protected FileParsingDialog mFabLoading;
    private BrowseFileDetailViewModel mViewModel;
    protected IMachine mMachine;

    private DecisionDialog mDecisionDialog;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnTouchListener(this);
        mViewModel = getViewModel();
        mViewModel.setFile(((BrowseA400Activity) requireActivity()).getShowFile());
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        initView();
    }

    public void initView() {
        // set filename
        startBtn.setText(mMachine.getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.FDM ?
                R.string.all_print : R.string.all_next);

        String filename = mViewModel.getFileName();
        if (!filename.equals("NULL")) {
            mTvFilename.setText(filename);
        }
        mTvFilename.setText(filename);
        if (!mViewModel.getFileInfo().equals("NULL")) {
            mTvFileInfo.setText(mViewModel.getFileInfo());
        } else {
            mTvFileInfo.setText("");
        }

        mFabLoading = FileParsingDialog.create(getContext());
        mFabLoading.setContent(requireContext().getString(R.string.copy_usb_file));

        Bitmap thumbnail = mViewModel.getGcodeThumbnail();
        if (thumbnail != null) {
            mIvFileImage.setImageBitmap(thumbnail);
        } else if (mViewModel.getBrowseShowFile() != null) {
            Glide.with(requireContext())
                    .load(mViewModel.getBrowseShowFile().getDefaultDisplay())
                    .into(mIvFileImage);
        }

        ArrayList<DetailDesc> showData = mViewModel.getShowData();
        for (int i = 0; i < showData.size(); i++) {
            DetailA400DataView detailView = new DetailA400DataView(getContext(), showData.get(i).getDetailDataName(), showData.get(i).getDetailDataValue());
            mGvDetailDesc.addView(detailView.initialize(), updateParams(i));
        }

        mDecisionDialog = DecisionDialog.create(getActivity())
                .setType(DecisionDialog.ERROR_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, true, true, false)
                .setPic(R.drawable.ic_fail_224x224);

    }

    private GridLayout.LayoutParams updateParams(int index) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = dp2px(318f);
        layoutParams.rowSpec = GridLayout.spec(index / 2);
        layoutParams.columnSpec = GridLayout.spec(index % 2);
        layoutParams.topMargin = dp2px(24f);
        layoutParams.setGravity(Gravity.START);
        return layoutParams;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_browse_print_file_info;
    }

    @Override
    protected BrowseFileDetailViewModel getViewModel() {
        return getViewModelProvider().get(BrowseFileDetailViewModel.class);
    }


    @OnClick(R2.id.btn_browse_j1_file_detail_start)
    void onClickStart() {
        playNormalClickSound();
        if (mMachine.getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.FDM && mMachine.getMachineController().getHeatedBed() == null) {

            WarmTipDialog.create(getActivity())
                    .setPic(R.drawable.icon_tips_error_80x80)
                    .setTitle("热床没有连接")
                    .show();
        } else {
            if (!mFabLoading.isShowing()) mFabLoading.show();
            mViewModel.handleResult()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        mFabLoading.dismiss();
                        if (success) {
                            // check if toolhead and extruder match
                            prepareStartPrint();
                        } else {
                            WarmTipDialog.create(getActivity())
                                    .setPic(R.drawable.icon_tips_error_80x80)
                                    .setTitle("拷贝失败, 请检查 U 盘或文件是否可用")
                                    .show();
                        }
                    }, e -> {
                        mFabLoading.dismiss();
                        Toast.makeText(getContext(), "拷贝失败,e:" + e.toString(), Toast.LENGTH_LONG).show();
                    });
        }

    }

    private void prepareStartPrint() {
        if (isJ1()) {
            mRouter.routeToPrintPage().start(getContext());
        } else {
            mViewModel.checkToolhead()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(pass -> {
                        if (pass) {
                            checkExtruder();
                        } else {
                            showToolheadMismatchDialog();
                        }
                    }, e -> {
                        showToolheadMismatchDialog();
                        LogHelper.log(e);
                    });
        }
    }

    private void showToolheadMismatchDialog() {
        switch (mMachine.getMachineInfoSubjectHolder().getValue().workType) {
            case FDM:
                mDecisionDialog.setWarmTv(R.string.file_detail_dialog_inconsistent_tool_head_3dp_warm_tip, R.color.palette_red_monza);
                break;
            case LASER:
                mDecisionDialog.setWarmTv(R.string.file_detail_dialog_inconsistent_tool_head_laser_warm_tip, R.color.palette_red_monza);
                break;
            case CNC:
                mDecisionDialog.setWarmTv(R.string.file_detail_dialog_inconsistent_tool_head_cnc_warm_tip, R.color.palette_red_monza);
                break;
            default:
                break;
        }
        mDecisionDialog.setTitle(R.string.file_detail_dialog_inconsistent_tool_head_title)
                .setContent(R.string.file_detail_dialog_inconsistent_nozzle_diameter_message)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_left_text_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv(R.string.all_continue, R.color.select_dialog_red_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkExtruder();
                    }
                }).show();

    }

    private void checkExtruder() {
        mViewModel.checkExtruder()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(pass -> {
                    if (pass) {
                        if (mMachine.getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.FDM) {
                            DecisionDialog.create(requireContext())
                                    .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                                    .setPic(R.drawable.ic_a400_clean_up_112x112)
                                    .setTitle(getString(R.string.file_manager_a400_procedure_start_confirm_dialog_title,
                                            getString(R.string.all_print)))
                                    .setContent(R.string.file_manager_a400_procedure_start_confirm_dialog_content_3dp)
                                    .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .setSecondTv(R.string.all_confirm, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                                        dialog.dismiss();
                                        mRouter.routeToPrintPage().start(getContext());
                                    })
                                    .show();
                        } else {
                            mRouter.routeToPrintPage().start(getContext());
                        }
                    } else {
                        showExtruderMismatchDialog();
                    }
                }, e -> {
                    showExtruderMismatchDialog();
                    LogHelper.log(e);
                });
    }

    private void showExtruderMismatchDialog() {
        mDecisionDialog.setTitle(R.string.file_detail_dialog_inconsistent_nozzle_diameter_title)
                .setWarmTv(R.string.file_detail_dialog_inconsistent_nozzle_diameter_warm_tip, R.color.palette_red_monza)
                .setContent(R.string.file_detail_dialog_inconsistent_tool_head_message)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_left_text_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv(R.string.all_continue, R.color.select_dialog_red_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mRouter.routeToPrintPage().start(requireContext());
                    }
                }).show();
    }

    private boolean isJ1() {
        MachineInfo machineInfo = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
        return machineInfo.seriesId == IMachine.MachineSeries.J && machineInfo.modelId == IMachine.MachineModel.J1;
    }

    @Optional
    @OnClick({R2.id.btn_browse_j1_file_detail_cancel})
    public void onClickBack() {
        playNormalClickSound();
        back();
    }

    @Optional
    @OnClick({R2.id.tv_browse_j1_file_detail_bg})
    public void onTouchOutside() {
        back();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
