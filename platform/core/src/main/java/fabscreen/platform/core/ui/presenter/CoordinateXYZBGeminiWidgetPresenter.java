package fabscreen.platform.core.ui.presenter;

import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R2;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CoordinateXYZBGeminiWidgetPresenter extends BasePresenter {
    @BindView(R2.id.tv_widget_coordinate_absolute_x_value)
    TextView mTvAbsoluteX;
    @BindView(R2.id.tv_widget_coordinate_absolute_y_value)
    TextView mTvAbsoluteY;
    @BindView(R2.id.tv_widget_coordinate_absolute_z_value)
    TextView mTvAbsoluteZ;
    @BindView(R2.id.tv_widget_coordinate_absolute_b_value)
    TextView mTvAbsoluteB;

    @BindView(R2.id.tv_widget_coordinate_relative_x_value)
    TextView mTvRelativeX;
    @BindView(R2.id.tv_widget_coordinate_relative_y_value)
    TextView mTvRelativeY;
    @BindView(R2.id.tv_widget_coordinate_relative_z_value)
    TextView mTvRelativeZ;
    @BindView(R2.id.tv_widget_coordinate_relative_b_value)
    TextView mTvRelativeB;

    public CoordinateXYZBGeminiWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    public void bind(View view) {
        ButterKnife.bind(this, view);
    }

    public void connect() {
        Disposable sub = MachineStatusManager.getMachineInfoHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(machineStatus -> {
                    float x = (float) machineStatus.x;
                    float y = (float) machineStatus.y;
                    float z = (float) machineStatus.z;
                    float b = (float) machineStatus.b;

                    float offsetX = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();
                    float offsetY = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY();
                    float offsetZ = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();

                    mTvAbsoluteX.setText(String.format(Locale.US, "%.2f", x - offsetX));
                    mTvAbsoluteY.setText(String.format(Locale.US, "%.2f", y - offsetY));
                    mTvAbsoluteZ.setText(String.format(Locale.US, "%.2f", z - offsetZ));

                    // B Axis is implement by Rotary Module instead of Linear Module.
                    // For now there is no "machine offset" concept in rotating movement.
                    mTvAbsoluteB.setText(String.format(Locale.US, "%.2f", b));

                    mTvRelativeX.setText(String.format(Locale.US, "%.2f", x));
                    mTvRelativeY.setText(String.format(Locale.US, "%.2f", y));
                    mTvRelativeZ.setText(String.format(Locale.US, "%.2f", z));
                    mTvRelativeB.setText(String.format(Locale.US, "%.2f", b));
                });
        addDisposable(sub);
    }
}
