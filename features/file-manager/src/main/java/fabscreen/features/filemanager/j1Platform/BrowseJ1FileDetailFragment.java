package fabscreen.features.filemanager.j1Platform;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.features.filemanager.BrowseFileDetailViewModel;
import fabscreen.features.filemanager.DetailDesc;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.DetailDataView;
import fabscreen.platform.core.ui.view.FabProgressDialog;
import fabscreen.platform.core.ui.view.ModelPopupAdapter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BrowseJ1FileDetailFragment extends BaseFragment {

    @BindView(R2.id.tv_browse_j1_file_detail_filename)
    TextView mTvFilename;
    @BindView(R2.id.tv_browse_j1_file_detail_image)
    ImageView mIvFileImage;
    @BindView(R2.id.gl_browse_j1_file_detail_desc)
    GridLayout mGvDetailDesc;
    @BindView(R2.id.ll_browse_j1_file_detail_mode_select)
    LinearLayout mlyModeSelect;
    @BindView(R2.id.btn_browse_j1_file_detail_start)
    Button startBtn;
    @BindView(R2.id.tv_browse_j1_print_mode_selected)
    TextView modelTv;

    private ModelPopupAdapter mPopupAdapter;
    private List<String> mList;
    private PopupWindow mPopupWindow;
    private View mPopupWindowView;
    private RecyclerView mPopupRv;

    protected FabProgressDialog mFabLoading;
    private BrowseFileDetailViewModel mViewModel;
    private String mModeErrorMsg;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        mViewModel.setFile(((BrowseJ1Activity) requireActivity()).getShowFile());
        mPopupWindowView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_list_file_detail, null);
        initView();
    }

    public void initView() {
        mPopupRv = mPopupWindowView.findViewById(R.id.file_detail_popup_rv);
        mPopupWindow = new PopupWindow(mPopupWindowView, (int) DimensUtils.dp2px(180), (int) DimensUtils.dp2px(244));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        // set filename
        String filename = mViewModel.getFileName();
        if (!filename.equals("NULL")) {
            int suffixIndex = filename.lastIndexOf(".");
            if (suffixIndex != -1) {
                filename = filename.substring(0, suffixIndex);
            }
        }
        mTvFilename.setText(filename);

        mList = new ArrayList<>();
        mList.add(getString(R.string.file_detail_standard_mode_name));
        mList.add(getString(R.string.file_detail_backup_mode_name));
        mList.add(getString(R.string.file_detail_copy_mode_name));
        mList.add(getString(R.string.file_detail_mirror_mode_name));

        mPopupAdapter = new ModelPopupAdapter(mList, getActivity());
        mPopupRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPopupRv.setAdapter(mPopupAdapter);
        // on select mode
        mPopupAdapter.setOnItemOnclickListener(position -> {
            if (mViewModel.checkPrintModeAvailable(position) == BrowseFileDetailViewModel.MODE_NORMAL) {
                Logger.d("print model change available");
                mViewModel.setPrintMode(position);
                mViewModel.setXOffsetWithMode(position);
                modelTv.setText(mList.get(position));
            } else {
                Logger.d("print model change not available");
                // show error dialog
                if (mViewModel.checkPrintModeAvailable(position) == BrowseFileDetailViewModel.MODE_OUT_OF_RANGE) {
                    mModeErrorMsg = getString(R.string.j1_file_details_error_out_of_rang);
                } else {
                    mModeErrorMsg = getString(mList.get(position).equals(getString(R.string.file_detail_backup_mode_name)) ?
                            R.string.j1_file_details_disable_dual_extrusion_for_back_up :
                            R.string.j1_file_details_disable_dual_extrusion);
                }
                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(mModeErrorMsg)
                        .setCanceledOnTouchOutSide(true)
                        .setFirstTv(requireContext().getString(R.string.j1_file_details_i_know), R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
            mPopupWindow.dismiss();

        });
        // Init print mode showing.
        int printMode = mViewModel.getPrintMode();
        int filePrintMode = mViewModel.getFilePrintMode();
        if (filePrintMode != printMode) {
            printMode = filePrintMode;
            mViewModel.setPrintMode(filePrintMode);
        }
        String printModeName;
        switch (printMode) {
            case IPrintWorkspace.PRINT_MODE_NORMAL:
                printModeName = getResources().getString(R.string.file_detail_standard_mode_name);
                break;
            case IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP:
                printModeName = getResources().getString(R.string.file_detail_backup_mode_name);
                break;
            case IPrintWorkspace.PRINT_MODE_CLONE:
                printModeName = getResources().getString(R.string.file_detail_copy_mode_name);
                break;
            case IPrintWorkspace.PRINT_MODE_MIRROR:
                printModeName = getResources().getString(R.string.file_detail_mirror_mode_name);
                break;
            default:
                printModeName = getResources().getString(R.string.file_detail_standard_mode_name);
                break;
        }
        modelTv.setText(printModeName);

        mFabLoading = new FabProgressDialog(getContext());
        mFabLoading.setMessage(R.string.copy_usb_file);

        Bitmap thumbnail = mViewModel.getGcodeThumbnail();
        if (thumbnail != null) {
            mIvFileImage.setImageBitmap(thumbnail);
        } else if (mViewModel.getBrowseShowFile() != null && mViewModel.getBrowseShowFile().getShowPic() != null) {
            Glide.with(requireContext())
                    .load(mViewModel.getBrowseShowFile().getShowPic())
                    .into(mIvFileImage);
        }

        // FIXME:The View is exclusive to J1 and the UI does not need to handle the display
        mlyModeSelect.setVisibility(ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J ? View.VISIBLE : View.INVISIBLE);

        ArrayList<DetailDesc> showData = mViewModel.getShowData();
        for (int i = 0; i < showData.size(); i++) {
            mGvDetailDesc.addView(new DetailDataView(getContext(), showData.get(i).getDetailDataName(), showData.get(i).getDetailDataValue()).initialize(), updateParams(i));
        }

        mViewModel.getUsbStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(hasUsb -> {
                    if (!hasUsb && !((BrowseJ1Activity) requireActivity()).getShowFile().getIFile().isLocal()) {
                        back();
                    }
                }, LogHelper::log);

    }

    private GridLayout.LayoutParams updateParams(int index) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = dp2px(175f);
        layoutParams.rowSpec = GridLayout.spec(index / 2);
        layoutParams.columnSpec = GridLayout.spec(index % 2);
        layoutParams.topMargin = dp2px(16f);
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
        return R.layout.fragment_j1_browse_file_detail;
    }

    @Override
    protected BrowseFileDetailViewModel getViewModel() {
        return getViewModelProvider().get(BrowseFileDetailViewModel.class);
    }


    @OnClick(R2.id.btn_browse_j1_file_detail_start)
    void onClickStart() {
        playNormalClickSound();
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
                        DecisionDialog.create(requireContext())
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                .setTitle(R.string.j1_file_details_failed_copy_title)
                                .setContent(R.string.j1_file_details_failed_copy_content)
                                .setFirstTv(R.string.all_ok, R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                }, e -> {
                    mFabLoading.dismiss();
                    Toast.makeText(getContext(), "拷贝失败,e:" + e.toString(), Toast.LENGTH_LONG).show();
                });
    }

    private void prepareStartPrint() {
        if (isJ1()) {
            mViewModel.checkFileExtruderRetractionDistance()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(pass -> {
                        if (pass) {
                            mRouter.routeToPrintPage().start(getContext());
                        } else {
                            Logger.d("Retraction distance exceed detected.");
                            showFDMExtruderRetractionLimitDialog();
                        }
                    }, e -> {
                        LogHelper.log(e);
                        showFDMExtruderRetractionLimitDialog();
                    });
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

    private void showFDMExtruderRetractionLimitDialog() {
        DecisionDialog.create(getActivity())
                .setType(DecisionDialog.ERROR_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                .setTitle(R.string.j1_file_detail_dialog_inappropriate_retraction_distance_title)
                .setContent(R.string.j1_file_detail_dialog_inappropriate_retraction_distance_message)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_left_text_color, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_continue, R.color.select_dialog_red_txt, (dialog, which) -> {
                    dialog.dismiss();
                    Logger.i("Continue to print.");
                    // Start routing to print module, prepare for print job.
                    mRouter.routeToPrintPage().start(getContext());
                }).show();
    }

    private void showToolheadMismatchDialog() {
        DecisionDialog.create(getActivity())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                .setTitle("Error!")
                .setContent("Toolhead mismatch, continue?")
                .setFirstTv("Cancel", R.color.select_dialog_left_text_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv("continue", R.color.palette_orange_web, new DialogInterface.OnClickListener() {
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
                        mRouter.routeToPrintPage().start(getContext());
                    } else {
                        showExtruderMismatchDialog();
                    }
                }, e -> {
                    showExtruderMismatchDialog();
                    LogHelper.log(e);
                });
    }

    private void showExtruderMismatchDialog() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                .setTitle(" Error!")
                .setContent("Extruder diameter mismatch, continue?")
                .setFirstTv("Cancel", R.color.select_dialog_left_text_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv("continue", R.color.palette_orange_web, new DialogInterface.OnClickListener() {
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
    @OnClick({R2.id.btn_j1_top_bar_back})
    public void onClicBack() {
        playNormalClickSound();
        back();

    }

    @OnClick({R2.id.btn_browse_j1_file_detail_print_mode_help})
    public void onClick() {
        playNormalClickSound();
        ProblemDialog.create(getActivity()).show();
    }

    @OnClick({R2.id.tv_browse_j1_print_mode_selected, R2.id.iv_browse_j1_print_mode_selected})
    public void onClick1() {
        playNormalClickSound();
        int[] location = new int[2];
        modelTv.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(modelTv, Gravity.NO_GRAVITY, location[0], location[1] - mPopupWindow.getHeight());

    }

    @Override
    protected void back() {
        super.back();
        mViewModel.setPrintMode(0);
    }
}
