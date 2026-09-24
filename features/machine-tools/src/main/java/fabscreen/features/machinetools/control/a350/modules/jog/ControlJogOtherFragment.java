package fabscreen.features.machinetools.control.a350.modules.jog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R2;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * {@link ControlJogOtherFragment} is a control jog page.
 * <p>
 * "Other" is used to differ this from {@link ControlJog3DPFragment}, which has a different coordinate and home widget.
 */
public class ControlJogOtherFragment extends ControlJogFragment {
    @BindView(R2.id.tv_widget_coordinate_relative_x_value)
    TextView mTvRelativeX;
    @BindView(R2.id.tv_widget_coordinate_relative_y_value)
    TextView mTvRelativeY;
    @BindView(R2.id.tv_widget_coordinate_relative_z_value)
    TextView mTvRelativeZ;
    @Nullable
    @BindView(R2.id.tv_widget_coordinate_relative_b_value)
    TextView mTvRelativeBValue;

    public static Fragment newInstance() {
        return new ControlJogOtherFragment();
    }

    @Override
    protected void modifyViewBeforeBind(View rootView) {
        super.modifyViewBeforeBind(rootView);
        View view;
        if (mViewModel.isRotaryAvailable()) {
            view = LayoutInflater.from(requireContext()).inflate(R.layout.widget_coordinate_xyzb_gemini_panel, (ViewGroup) rootView, false);
        } else {
            view = LayoutInflater.from(requireContext()).inflate(R.layout.widget_coordinate_gemini_panel, (ViewGroup) rootView, false);
        }
        if (rootView == null) return;
        ViewGroup container = (ViewGroup) rootView;
        container.addView(view, 0);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_control_jog;
    }

    @Override
    protected void setCoordinateValue() {
        // set coordinate xyzb value
        mViewModel.getMachineStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    float x = (float) machineStatus.x;
                    float y = (float) machineStatus.y;
                    float z = (float) machineStatus.z;
                    float b = (float) machineStatus.b;

                    float offsetX = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();
                    float offsetY = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY();
                    float offsetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();

                    mTvAbsoluteX.setText(String.format(Locale.US, "%.2f", x - offsetX));
                    mTvAbsoluteY.setText(String.format(Locale.US, "%.2f", y - offsetY));
                    mTvAbsoluteZ.setText(String.format(Locale.US, "%.2f", z - offsetZ));

                    // B Axis is implement by Rotary Module instead of Linear Module.
                    // For now there is no "machine offset" concept in rotating movement.
                    if (mTvAbsoluteBValue != null) {
                        mTvAbsoluteBValue.setText(String.format(Locale.US, "%.2f", b));
                    }

                    mTvRelativeX.setText(String.format(Locale.US, "%.2f", x));
                    mTvRelativeY.setText(String.format(Locale.US, "%.2f", y));
                    mTvRelativeZ.setText(String.format(Locale.US, "%.2f", z));
                    if (mTvRelativeBValue != null) {
                        mTvRelativeBValue.setText(String.format(Locale.US, "%.2f", b));
                    }
                });
    }

//    @Override
//    protected void setCoordinateBVisible(boolean visible) {
//        super.setCoordinateBVisible(visible);
//        mTvRelativeBLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mTvRelativeBValue.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mTvRelativeBDegree.setVisibility(visible ? View.VISIBLE : View.GONE);
//        // Relative. Absolute is set in parent fragment.
//        RelativeLayout container = (RelativeLayout) mTvRelativeBLabel.getParent();
//        int paddingPixel = DimensUtils.dp2px(visible ? 5 : 10, requireContext());
//        container.setPadding(0, paddingPixel, 0, paddingPixel);
//    }
}
