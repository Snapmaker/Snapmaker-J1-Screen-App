package fabscreen.features.machinetools.cncassist.origin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CNCOriginAssistantSafetyGogglesFragment extends BaseFragment {
    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_preview_laser_prepare_safety_goggles_message)
    TextView mTvContent;

    public static CNCOriginAssistantSafetyGogglesFragment newInstance() {
        return new CNCOriginAssistantSafetyGogglesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_safety_goggles;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mTvTitle.setText(R.string.all_safety_goggles);
        mTvContent.setText(R.string.guide_cnc_safety_goggles_content);
    }

    private AlertDialog showMachineMovingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_cnc_origin_assistant_set_origin_moving, null);
        dialog.setView(view);
        dialog.show();

        return dialog;
    }

    private void gotoOriginAssistantSetOriginFragment() {
        if (getActivity() != null) {
            ((CNCOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetOriginFragment();
        }
    }

    @OnClick(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            AlertDialog dialog = showMachineMovingDialog();
            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                    .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0))
                    .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(ret -> {
                        dialog.dismiss();
                        gotoOriginAssistantSetOriginFragment();
                    }, e -> {
                        dialog.dismiss();
                        LogHelper.log(e);
                    });
        }
    }
}
