package fabscreen.features.machinetools.calibration;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.FileParsingDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class A400CalibrationBaseFragment extends BaseFragment {
    @Nullable
    @BindView(R2.id.top_bar_content)
    protected TextView mTvTopBarContent;

    @BindView(R2.id.top_bar_title)
    protected TextView mTvTopBarTitle;
    @Nullable
    @BindView(R2.id.top_bar_ico)
    protected ImageView mIvIco;
    @Nullable
    @BindView(R2.id.view_guide_progress_bar)
    protected LinearProgressIndicator mGuideProgressBar;
    public FileParsingDialog fabLoading;
    protected DecisionDialog fabBackConfirm;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabLoading = FileParsingDialog.create(getActivity())
                .setContent(R.string.move_show);
    }

    protected String getTitle() {
        if (mTvTopBarTitle != null) {
            return mTvTopBarTitle.getText() + "";
        }
        return "current process";
    }

    @Override
    protected void back() {
        fabBackConfirm = DecisionDialog.create(getContext())
                .setTitle(getTitle())
                .setContent(getString(R.string.assistant_back_notice, getTitle()))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getContext().getResources().getString(R.string.all_stop), R.color.select_dialog_yellow_txt, ((dialog, which) -> {
                    fabBackConfirm.mCancelBtn.setEnabled(false);
                    fabBackConfirm.mSecondBtn.setEnabled(false);
                    Observable<ResponseStructure> responseStructureObservable = null;
                    IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
                    switch (workType) {
                        case FDM:
                            responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(false);
                            break;
                        case LASER:
                            responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().exitCalibration(false);
                            break;
                        case CNC:
                            responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getCNCController().exitCalibration(false);
                            break;
                    }
                    if (responseStructureObservable == null) return;
                    responseStructureObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                if (!success.isSuccess()) {
                                    Logger.d("Exit Calibration: " + success);
                                }
                                dialog.dismiss();
                                // FIXME: 2022/4/19 Cool down shouldn't be done int the base fragment, it's not a common feature.
                                coolDownBedIfHave();
                                requireActivity().setResult(Activity.RESULT_CANCELED);
                                requireActivity().finish();
                            }, LogHelper::log);
                }));
        fabBackConfirm.show();
    }

    protected void coolDownBedIfHave() {
        MachineController machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        if (machineController.getHeatedBed() != null) {
            machineController.getHeatedBed().setZoneTargetTemperature(0, 0).as(bindToLifecycle()).subscribe(responseStructure -> {
            }, LogHelper::log);
        }
    }

    protected void setContent(CharSequence title) {
        if (mTvTopBarContent != null) {
            mTvTopBarContent.setText(title);
        }
    }

    protected void setContent(String title) {
        if (mTvTopBarContent != null) {
            mTvTopBarContent.setText(title);
        }
    }

    protected void setContent(int resid) {
        if (mTvTopBarContent != null) {
            mTvTopBarContent.setText(resid);
        }
    }

    protected void setTitle(CharSequence title) {
        if (mTvTopBarTitle != null) {
            mTvTopBarTitle.setText(title);
        }
    }

    protected void setTitle(String title) {
        if (mTvTopBarTitle != null) {
            mTvTopBarTitle.setText(title);
        }
    }

    protected void setTitle(int resid) {
        if (mTvTopBarTitle != null) {
            mTvTopBarTitle.setText(resid);
        }
    }


}
