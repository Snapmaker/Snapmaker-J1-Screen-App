package fabscreen.platform.core.ui.presenter;


import android.util.Log;

import fabscreen.platform.base.Constants;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.view.FabFullScreenDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CoordinateSystemPresenter extends BasePresenter {
    private final static String TAG = CoordinateSystemPresenter.class.getSimpleName();

    private FabFullScreenDialog mDialog;
    private int mCoordinateID;
    private OnCoordinateSwitchListener mCoordinateSwitchListener;

    public CoordinateSystemPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    public void ensureCoordinate(int coordinateID) {
        if (mDialog != null) return;

        mCoordinateID = coordinateID;

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coordinateSystem -> {
                    boolean homed = coordinateSystem.isHomed;

                    if (!homed) {
                        mDialog = FabFullScreenDialog.create(getContext());
                        mDialog.setIcon(R.drawable.pic_dialog_homing_72x72);
                        mDialog.setTitle(R.string.all_homing_title_homing);
                        mDialog.setMessage(R.string.all_homing_content);
                        mDialog.setPositive(R.string.all_homing_title_going_home, (dialog, which) -> {
                            mDialog.setPositive(R.string.all_homing_title_homing, false);

                            Disposable sub1 = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer()
                                    .sendGcode("G28")
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(success -> checkHome());
                            addDisposable(sub1);
                        });
                        mDialog.show();
                    } else {
                        switchCoordinate();
                    }
                }, e -> {
                    Log.e(TAG, "update coordinate system failed.");
                    LogHelper.log(e);
                });
        addDisposable(sub);
    }

    private void checkHome() {
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coordinateSystem -> {
                    boolean homed = coordinateSystem.isHomed;
                    if (homed) {
                        // Switch coordinate system
                        switchCoordinate();
                    } else {
                        // Check homed state periodically until it's homed
                        AndroidSchedulers.mainThread().scheduleDirect(this::checkHome, 2000, Constants.TIME_UNIT);
                    }
                });
        addDisposable(sub);
    }

    private void switchCoordinate() {
        int coordinateID = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().coordinateID;
        boolean coordinateAligned = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().isCoordinateAligned;

        // Switch to coordinate system #1 if not configured
        if (coordinateID != mCoordinateID || !coordinateAligned) {
            addDisposable(ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(mCoordinateID).subscribe());
        }

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (mCoordinateSwitchListener == null) return;
        mCoordinateSwitchListener.onCoordinateSwitched();
    }

    public void setOnCoordinateSwitchListener(OnCoordinateSwitchListener listener) {
        mCoordinateSwitchListener = listener;
    }

    public interface OnCoordinateSwitchListener {
        void onCoordinateSwitched();
    }
}
