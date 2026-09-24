package fabscreen.features.home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.HOME_LAND)
public class HomePrintIdleModuleFragment extends BaseFragment {

    @BindView(R2.id.iv_a400_home_model_pic)
    ImageView mIvA400HomeModelPic;
    private int mFileType = 0;

    public static HomePrintIdleModuleFragment newInstance() {
        return new HomePrintIdleModuleFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        Logger.d("Requesting Print Power Outage...");
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        if (machine.getMachineStatusSubjectHolder().getValue().connected) {
            PrintController printController = machine.getPrintController();
            printController.requestPowerOutageStatus()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(responseStructure -> {
                        if (responseStructure.isSuccess()) {
                            Logger.d("Power loss outage detected.");
                            handlePrintPowerLoss(responseStructure);
                        } else {
                            Logger.d("Power loss issues return " + responseStructure.resultProp.getValue());
                        }
                    }, LogHelper::log);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_home_print_idle;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {

    }

    @Override
    public void onResume() {
        super.onResume();
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        switch (workType) {
            case CNC:
                mFileType = 3;
                mIvA400HomeModelPic.setImageResource(R.drawable.ic_a400_home_cnc_pic);
                break;
            case FDM:
                mFileType = 1;
                mIvA400HomeModelPic.setImageResource(R.drawable.ic_a400_home_3dp_pic);
                break;
            case LASER:
                mFileType = 2;
                mIvA400HomeModelPic.setImageResource(R.drawable.ic_a400_home_laser_pic);
                break;
            case NONE:
                mFileType = 0;
                mIvA400HomeModelPic.setImageDrawable(null);
                break;
            default:
                mFileType = 0;
                mIvA400HomeModelPic.setImageDrawable(null);
        }
    }

    @OnClick(R2.id.btn_home_start)
    void onClickStart() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToFilesPage(mFileType).start(getContext());
    }

    void handlePrintPowerLoss(ResponseStructure response) {
        BaseStructure gcodeFileInfo = (BaseStructure) response.dataProp;
        String md5 = (String) gcodeFileInfo.getProp("md5").getValue();
        String filename = (String) gcodeFileInfo.getProp("filename").getValue();

        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        workspace.initLastPrintFile();
        if (workspace.getPrintFile() == null) {
            Logger.w("Could not find file in workspace!");
            return;
        }
        if (filename.equals(workspace.getFileName())) {
            DecisionDialog.create(requireContext())
                    .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                    .setType(DecisionDialog.WARMING_TYPE)
                    .setPic(R.drawable.ic_yellow_warn)
                    .setTitle(R.string.power_loss_recovery_title)
                    .setContent(R.string.power_loss_recovery_message)
                    .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().requestPrintPowerLossClearMarker()
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(responseStructure -> dialog.dismiss(), LogHelper::log);
                    })
                    .setSecondTv(R.string.all_continue_printing, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                        dialog.dismiss();
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(true);
                        workspace.setFileMD5Value(md5);
                        mRouter.routeToPrintPage(). start(getContext());
                    })
                    .show();
        }
    }
}
