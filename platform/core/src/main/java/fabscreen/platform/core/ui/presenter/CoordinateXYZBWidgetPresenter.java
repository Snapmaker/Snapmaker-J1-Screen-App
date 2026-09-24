package fabscreen.platform.core.ui.presenter;


import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CoordinateXYZBWidgetPresenter extends BasePresenter {
    @BindView(R2.id.tv_widget_coordinate_absolute_x_value)
    TextView mTvAbsoluteX;
    @BindView(R2.id.tv_widget_coordinate_absolute_y_value)
    TextView mTvAbsoluteY;
    @BindView(R2.id.tv_widget_coordinate_absolute_z_value)
    TextView mTvAbsoluteZ;
    @BindView(R2.id.tv_widget_coordinate_absolute_b_value)
    TextView mTvAbsoluteB;

    @BindView(R2.id.btn_widget_coordinate_home)
    ActionButton mBtnHome;

    public CoordinateXYZBWidgetPresenter(CompositeDisposable compositeDisposable) {
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

                    mTvAbsoluteX.setText(String.format(Locale.US, "%.2f", x));
                    mTvAbsoluteY.setText(String.format(Locale.US, "%.2f", y));
                    mTvAbsoluteZ.setText(String.format(Locale.US, "%.2f", z));
                    mTvAbsoluteB.setText(String.format(Locale.US, "%.2f", b));
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_widget_coordinate_home)
    void onClickHome() {
        mBtnHome.setActivated(true);

        // Note that this widget is only used for 3DP
        // We simply use CS#0, thus no coordinate system need to be updated
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mBtnHome.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mBtnHome.setActivated(false);
                });
        addDisposable(sub);
    }
}
