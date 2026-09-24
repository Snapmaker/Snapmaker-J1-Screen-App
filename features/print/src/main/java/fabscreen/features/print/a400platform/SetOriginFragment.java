package fabscreen.features.print.a400platform;

import static fabscreen.platform.base.service.IMachine.WorkType.CNC;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.PrintReadyViewModel;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.SelectModelBean;
import fabscreen.platform.core.ui.view.SelectModelDialog;
import fabscreen.platform.core.ui.view.ViewUtils;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SetOriginFragment extends BaseFragment {

    private PrintReadyViewModel mViewModel;
    private int mCurrentMode = -1;

    public static Fragment newInstance() {
        return new SetOriginFragment();
    }

    @BindView(R2.id.btn_next)
    Button mBtnNext;
    @BindView(R2.id.fcv_jog_mode)
    FragmentContainerView mFcvJogMode;
    @BindView(R2.id.tv_run_boundary)
    TextView mTvRunBoundary;
    @BindView(R2.id.view_guide_progress_bar)
    LinearProgressIndicator mProgressBar;
    @BindView(R2.id.top_bar_title)
    TextView mTvTopBarTitle;
    @BindView(R2.id.top_bar_content)
    TextView mTvTopBarContent;
    @BindView(R2.id.top_bar_ico)
    ImageView mIvTapBarIcon;
    @BindView(R2.id.tv_model_type)
    TextView tvModelType;
    @BindView(R2.id.tv_tip)
    TextView mTvTip;

    private SelectModelDialog mSelectModelDialog;
    private List<SelectModelBean> mModelList;
    private IPreferences.Helper mHelper;
    private int selectModelType;
    private IMachine.WorkType mWorkType;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        if (mViewModel.getWorkType() == IMachine.WorkType.LASER) {
            mViewModel.setOriginIndicatorState(true);
        }
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViewModel.getWorkType() == IMachine.WorkType.LASER) {
            mViewModel.setOriginIndicatorState(false);
        }
    }

    private void initView() {
        mBtnNext.setText(R.string.all_next);
        mBtnNext.setVisibility(View.VISIBLE);
        mIvTapBarIcon.setVisibility(View.GONE);
        mWorkType = mViewModel.mMachine.getMachineInfoSubjectHolder().getValue().workType;
        switch (mWorkType) {
            case CNC:
                mTvTopBarTitle.setText(R.string.print_cnc_job_preparation);
                mTvTopBarContent.setText(getString(R.string.print_set_work_origin, "(2/2)"));
                mProgressBar.setMax(2);
                mProgressBar.setProgress(2);
                mBtnNext.setText(R.string.all_start_job);
                break;
            case LASER:
                mTvTopBarTitle.setText(R.string.print_laser_job_preparation);
                mTvTopBarContent.setText(getString(R.string.print_set_work_origin, "(3/3)"));
                mProgressBar.setMax(3);
                mProgressBar.setProgress(3);
                mBtnNext.setText(R.string.all_start_job);
                break;
        }

        //Text underline
        mTvRunBoundary.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTvRunBoundary.getPaint().setAntiAlias(true);

        mModelList = new ArrayList<>();
        SelectModelBean modelOne = new SelectModelBean();
        modelOne.setTitle(getString(R.string.calibration_base_mode));
        modelOne.setContent(getString(R.string.print_base_mode));
        SelectModelBean modelTwo = new SelectModelBean();
        modelTwo.setTitle(getString(R.string.calibration_advanced_mode));
        modelTwo.setContent(getString(R.string.print_advanced_mode));
        mModelList.add(modelOne);
        mModelList.add(modelTwo);

        mHelper = getServiceContainer().getService(IPreferences.class).getHelper();
        selectModelType = mHelper.getCncSelectModel();
        refreshView(selectModelType);
        mSelectModelDialog = mSelectModelDialog.create(requireContext())
                .setTitle(R.string.calibration_select_mode_title)
                .setData(mModelList).setPosition(selectModelType).setOnItemClickListener(new SelectModelDialog.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        playNormalClickSound();
                        mHelper.setCncSelectModel(position);
                        refreshView(position);
                    }
                });

    }


    private void refreshView(int position) {
        if (position == mCurrentMode) return;
        tvModelType.setText(position == 0 ? R.string.calibration_base_mode : R.string.calibration_advanced_mode);
        mTvRunBoundary.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        switch (position) {
            case 0:
                String title;
                String desc;
                if (mViewModel.getIsRotaryAvailable()) {
                    title = getString(R.string.a400_print_cnc_print_xyzb_title);
                    desc = getString(R.string.a400_print_cnc_print_xyzb_content);
                } else {
                    title = getString(R.string.print_set_coord_origin, mViewModel.getWorkType() == CNC ? "XYZ " : "XY");
                    desc = getString(mViewModel.getWorkType() == CNC ? R.string.print_job_preparation_set_coord_message :
                            R.string.print_job_preparation_set_coord_xy_message);
                }

                // basic mode
                Bundle bundle = new Bundle();
                bundle.putInt("pic", 0);// TODO: 2022/4/28 the main pic
                bundle.putString("title", title);
                bundle.putString("desc", desc);
                Fragment fragment = WorkPrepareWithJogFragment.newInstance(bundle);
                watchForButtonStates(fragment);
                getChildFragmentManager().beginTransaction().replace(R.id.fcv_jog_mode, fragment, "prepare").commit();
                break;
            case 1:
                // advance mode
                getChildFragmentManager().beginTransaction().replace(R.id.fcv_jog_mode, mRouter.getFragmentInstance(RoutePath.PREPARE_PRINT_JOG_CONTROL)).commit();
                if (mWorkType == CNC) {
                    mTvTip.setText(R.string.a400_cnc_four_axis_content);
                }
                break;
        }
        mCurrentMode = position;
    }

    private void watchForButtonStates(Fragment fragment) {
        Logger.d("is work prepare fragment: %1$s,%2$s", fragment instanceof WorkPrepareWithJogFragment, fragment);
        if (fragment instanceof WorkPrepareWithJogFragment) {
            ((WorkPrepareWithJogFragment) fragment).getButtonsEnableObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(shouldEnable -> ViewUtils.enableButtons((ViewGroup) requireView(), shouldEnable), LogHelper::log);
        }
    }

    @Override
    protected PrintReadyViewModel getViewModel() {
        return getViewModelProvider().get(PrintReadyViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_set_origin;
    }

    @OnClick(R2.id.tv_run_boundary)
    void onRunBoundaryClicked() {
        playNormalClickSound();
        runBoundary();
    }

    public void runBoundary() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setPic(R.drawable.pic_toolhead_run_boundary)
                .setType(DecisionDialog.TIP_TYPE)
                .setTitle(requireContext().getString(R.string.toolhead_run_boundary))
                .setContent(requireContext().getString(R.string.toolhead_run_boundary_message))
                .setSecondTv(requireContext().getString(R.string.all_confirm), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    // If cur mode is basic, we need to set current position as origin first.
                    mViewModel.runBoundary(mCurrentMode == 0)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(result -> Logger.d("Run boundary result is %s", result), LogHelper::log);
                })
                .setFirstTv(requireContext().getString(R.string.all_cancel), R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        playNormalClickSound();

        if (mViewModel.getWorkType() == CNC && mCurrentMode == 1) {

            showCNCPrintDialog();
        } else {
            Observable<Boolean> observable;

            if (mViewModel.getWorkType() == CNC) {
                observable = mViewModel.setXYZOrigin();
            } else {
                observable = mViewModel.setXYOrigin();
            }

            observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        if (success) {
                            switch (mViewModel.getWorkType()) {
                                case FDM:
                                    ((PrintA400Activity) requireActivity()).goToPrint();
                                    break;
                                case LASER:
                                    DecisionDialog.create(requireContext())
                                            .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                                            .setType(DecisionDialog.TIP_TYPE)
                                            .setPic(R.drawable.pic_laser_goggles_240x160)
                                            .setTitle(requireContext().getString(R.string.laser_safety_goggles_open))
                                            .setContent(requireContext().getString(R.string.laser_safety_goggles_open_message))
                                            .setSecondTv(requireContext().getString(R.string.all_confirm), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                                                dialog.dismiss();
                                                ((PrintA400Activity) requireActivity()).goToPrint();
                                            })
                                            .setFirstTv(requireContext().getString(R.string.all_cancel), R.color.select_dialog_white_txt, (dialog, which) -> {
                                                dialog.dismiss();
                                            })
                                            .show();
                                    break;
                                case CNC:
                                    showCNCPrintDialog();
                                    break;
                                default:
                                    break;
                            }

                        } else {
                            showErrorDialog("Go to origin fail!");
                        }
                    }, e -> {
                        showErrorDialog(e.getMessage());
                        LogHelper.log(e);
                    });
        }
    }

    private void showCNCPrintDialog() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setPic(R.drawable.pic_a400_control_cnc_open)
                .setType(DecisionDialog.TIP_TYPE)
                .setTitle(requireContext().getString(R.string.cnc_safety_goggles_open))
                .setContent(requireContext().getString(R.string.cnc_safety_goggles_open_message))
                .setSecondTv(requireContext().getString(R.string.all_confirm), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    ((PrintA400Activity) requireActivity()).goToPrint();
                })
                .setFirstTv(requireContext().getString(R.string.all_cancel), R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @OnClick(R2.id.tv_model_type)
    public void OnclickModel() {
        playSwitchSound();
        mSelectModelDialog.show();
    }

    @Override
    protected void back() {
        playNormalClickSound();
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.WARMING_TYPE)
                .setPic(R.drawable.ic_yellow_warn)
                .setTitle(getString(R.string.all_stop) + mTvTopBarTitle.getText().toString())
                .setContent(getString(R.string.a400_stop_work_title, getString(R.string.all_stop)))
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setSecondTv(R.string.all_stop, R.color.select_dialog_yellow_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        }).show();
    }

    private void showErrorDialog(String s) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error!")
                .setMessage(s)
                .create()
                .show();
    }
}
