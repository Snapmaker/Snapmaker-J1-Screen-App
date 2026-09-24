package fabscreen.features.print.a400platform;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;

public class PrintA400CompleteFragment extends BaseFragment {
    @BindView(R2.id.tv_print_file_name)
    TextView mTvFilename;
    @BindView(R2.id.tv_print_time)
    TextView mTvPrintTime;
    @BindView(R2.id.iv_print_file_diagram)
    ImageView mIvPrintFileDiagram;
    @BindView(R2.id.iv_print_base_show)
    ImageView mIvPrintBaseShow;

    private IMachine mA400Machine;

    public static String formatTime(double time) {
        int hour = (int) (time) / 3600;
        int minute = ((int) (time) % 3600) / 60;
        int second = ((int) (time) % 60);

        if (hour < 1) {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        Bitmap bitmap = ServiceContainer.getInstance().getService(IGcodeParser.class).getGcodeThumbnail();
        mTvFilename.setText(workspace.getFileName());
        mTvFilename.setVisibility(TextView.GONE);
        int elapsed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount();
        mTvPrintTime.setText(formatTime(elapsed));
        mIvPrintFileDiagram.setImageBitmap(bitmap);
        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
        IMachine.WorkType workType = mA400Machine.getMachineInfoSubjectHolder().getValue().workType;
        switch (workType) {
            case FDM:
                mIvPrintBaseShow.setImageResource(R.drawable.pic_a400_print_base_show_fdm);
                break;
            case LASER:
                mIvPrintBaseShow.setImageResource(R.drawable.pic_a400_print_base_show_laser);
                break;
            case CNC:
                mIvPrintBaseShow.setImageResource(R.drawable.pic_a400_print_base_show_cnc);
                break;
            default:
                break;
        }
        playProcedureCompleteSound();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print_complete;
    }

    @OnClick(R2.id.btn_print_complete)
    void OnClickComplete() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
    }

    @OnClick(R2.id.btn_print_continue)
    void onClickContinue() {
        playNormalClickSound();
        requireActivity().finish();
    }
}

