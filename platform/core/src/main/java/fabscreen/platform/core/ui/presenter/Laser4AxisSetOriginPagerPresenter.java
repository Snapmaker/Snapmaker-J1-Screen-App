package fabscreen.platform.core.ui.presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.orhanobut.logger.Logger;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.base.view.FabConfirm;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class Laser4AxisSetOriginPagerPresenter implements DefaultLifecycleObserver {
    @BindView(R2.id.vp_widget_4axis_set_origin_pager)
    ViewPager mVpPager;
    private Context context;
    private ActionButton mBtnSetOriginX;
    private ActionButton mBtnSetOriginY;
    private ActionButton mBtnSetOriginZ;
    private ActionButton mBtnSetOriginB;
    private ActionButton mBtnSetOrigin;
    private ActionButton mBtnGotoOrigin;
    private ActionButton mBtnRunBoundary;
    private ActionButton mBtnHome;

    private boolean mIsDisabledZ = false;

    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    private MyPagerAdapter mPagerAdapter;
    private ModelBoundary mBoundary;
    private boolean mBoundaryWarningFlag = false;

    public Laser4AxisSetOriginPagerPresenter(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    protected <T> AutoDisposeConverter<T> bindToLifecycle() {
        LifecycleOwner owner = (LifecycleOwner) getContext();
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(owner, Lifecycle.Event.ON_DESTROY));
    }

    public boolean getBoundaryWarningFlag() {
        return mBoundaryWarningFlag;
    }

    public void setBoundaryWarningFlag(boolean flag) {
        mBoundaryWarningFlag = flag;
    }

    public Observable<Boolean> getMovingEventObservable() {
        return mMovingEventSubject.hide();
    }

    public void bindView(Lifecycle lifecycle, View view, boolean isRunBoundaryFirst) {
        lifecycle.addObserver(this);
        ButterKnife.bind(this, view);

        final LayoutInflater inflater = LayoutInflater.from(getContext());

        List<View> views = new ArrayList<>();
        views.add(createPage1(inflater, isRunBoundaryFirst));
        views.add(createPage2(inflater));

        mPagerAdapter = new MyPagerAdapter(views);
        mVpPager.setAdapter(mPagerAdapter);

        mMovingEventSubject
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> setEnabled(!movingEvent));
    }

    public void disabledZ() {
        mIsDisabledZ = true;
        mBtnSetOriginZ.setEnabled(false);
    }

    public void setEnabled(boolean enabled) {
        mBtnSetOriginX.setEnabled(enabled);
        mBtnSetOriginY.setEnabled(enabled);
        mBtnSetOriginZ.setEnabled(enabled && !mIsDisabledZ);
        mBtnSetOriginB.setEnabled(enabled);
        mBtnSetOrigin.setEnabled(enabled);
        mBtnGotoOrigin.setEnabled(enabled);
        mBtnRunBoundary.setEnabled(enabled);
        mBtnHome.setEnabled(enabled);
    }

    public void setBoundary(@Nullable ModelBoundary boundary) {
        mBoundary = boundary;
        mBtnRunBoundary.setEnabled(mBoundary != null);
    }

    private View createPage1(LayoutInflater inflater, boolean isBoundaryFirst) {
        View page;
        if (isBoundaryFirst) {
            page = inflater.inflate(R.layout.widget_laser_4axis_set_origin_pager_p1b, null);
        } else {
            page = inflater.inflate(R.layout.widget_laser_4axis_set_origin_pager_p1, null);
        }

        mBtnSetOrigin = page.findViewById(R.id.btn_widget_set_origin_set_origin);
        mBtnSetOrigin.setOnClickListener(this::onClickSetOrigin);

        mBtnGotoOrigin = page.findViewById(R.id.btn_widget_set_origin_goto_origin);
        mBtnGotoOrigin.setOnClickListener(this::onClickGotoOrigin);

        mBtnRunBoundary = page.findViewById(R.id.btn_widget_set_origin_run_boundary);
        mBtnRunBoundary.setEnabled(false);
        mBtnRunBoundary.setOnClickListener(this::onClickRunBoundary);

        mBtnHome = page.findViewById(R.id.btn_widget_set_origin_home);
        mBtnHome.setOnClickListener(this::onClickHome);

        return page;
    }

    private View createPage2(LayoutInflater inflater) {
        View page = inflater.inflate(R.layout.widget_laser_4axis_set_origin_pager_p2, null);

        mBtnSetOriginX = page.findViewById(R.id.btn_widget_set_origin_x);
        mBtnSetOriginX.setOnClickListener(this::onClickSetOriginX);

        mBtnSetOriginY = page.findViewById(R.id.btn_widget_set_origin_y);
        mBtnSetOriginY.setOnClickListener(this::onClickSetOriginY);

        mBtnSetOriginZ = page.findViewById(R.id.btn_widget_set_origin_z);
        mBtnSetOriginZ.setOnClickListener(this::onClickSetOriginZ);

        mBtnSetOriginB = page.findViewById(R.id.btn_widget_set_origin_b);
        mBtnSetOriginB.setOnClickListener(this::onClickSetOriginB);

        return page;
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
    }

    private void onClickSetOriginX(View v) {
        mMovingEventSubject.onNext(true);
        mBtnSetOriginX.setActivated(true);

        Logger.i("Requesting set origin x...");
        Vector vector = new Vector();
        vector.setX(0);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginX.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginX.setActivated(false);
                });
    }

    private void onClickSetOriginY(View v) {
        mMovingEventSubject.onNext(true);
        mBtnSetOriginY.setActivated(true);

        Logger.i("Requesting set origin y...");

        Vector vector = new Vector();
        vector.setY(0);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginY.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginY.setActivated(false);
                });
    }

    private void onClickSetOriginZ(View v) {
        mMovingEventSubject.onNext(true);
        mBtnSetOriginZ.setActivated(true);

        Logger.i("Requesting set origin z...");

        Vector vector = new Vector();
        vector.setZ(0);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginZ.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginZ.setActivated(false);
                });
    }

    private void onClickSetOriginB(View v) {
        mMovingEventSubject.onNext(true);
        mBtnSetOriginB.setActivated(true);

        Logger.i("Requesting set origin b...");

        Vector vector = new Vector();
        vector.setB(0);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginB.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnSetOriginB.setActivated(false);
                });
    }

    private void onClickHome(View v) {
        mMovingEventSubject.onNext(true);
        mBtnHome.setActivated(true);

        Logger.i("Requesting G28...");

        // TODO: bug here, Homing on CS#1 will result in not get correct coordinate x, y, z
        // So we switch back go CS#0, G28 and then switch back
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0))
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnHome.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnHome.setActivated(false);
                });
    }

    private void onClickSetOrigin(View v) {
        mMovingEventSubject.onNext(true);
        mBtnSetOrigin.setActivated(true);

        Logger.i("Requesting set origin...");
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        vector.setB(0);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnSetOrigin.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnSetOrigin.setActivated(false);
                });
    }

    private void onClickGotoOrigin(View v) {
        final float currentZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();
        mMovingEventSubject.onNext(true);
        mBtnGotoOrigin.setActivated(true);

        Logger.i("Requesting go to origin...");

        if (currentZ > 0) {
            // Engage direction, move X Y linear module first, then Z.
            ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800"))
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        mMovingEventSubject.onNext(false);
                        mBtnGotoOrigin.setActivated(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                        mBtnGotoOrigin.setActivated(false);
                    });
        } else {
            // Retract direction, move Z linear module first, then X Y.
            ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800")
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000"))
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        mMovingEventSubject.onNext(false);
                        mBtnGotoOrigin.setActivated(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                        mBtnGotoOrigin.setActivated(false);
                    });
        }
    }

    private Observable<SSTPPacketContent.GcodeResponse> runBoundaryWith(int index, int speed) {
        // We assume that only running boundary in XY dimension with laser pattern
        if (index == 5) {
            String cmd = String.format(Locale.US, "G0 B0 Y0 F%d", speed);
            return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(cmd);
        } else {
            // 0 -> 1 -> 2 -> 3 -> 4(0) -> 5(origin)
            final float[] point = mBoundary.getBoundaryPoint(index % 4);
            String cmd = String.format(Locale.US, "G0 B%.2f Y%.2f F%d", point[0], point[1], speed);

            return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(cmd)
                    .flatMap(response -> runBoundaryWith(index + 1, speed));
        }
    }

    private void showBoundaryWarningDialog() {
        final boolean is10wLaserPlugged = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W;

        int descStringRes = is10wLaserPlugged ? R.string.preview_laser_10w_rotary_set_origin_dialog_run_boundary_warning
                : R.string.preview_laser_rotary_set_origin_dialog_run_boundary_warning;
        int confirmStringRes = is10wLaserPlugged ? R.string.all_ok : R.string.guide_got_it;

        FabConfirm.create(context)
                .setIcon(R.drawable.pic_dialog_warning_72x72)
                .setDescription(descStringRes)
                .setConfirm(confirmStringRes, (dialog, which) -> {
                    mBoundaryWarningFlag = false;
                    dialog.dismiss();
                    startRunBoundary();
                })
                .show();
    }

    private void startRunBoundary() {
        mMovingEventSubject.onNext(true);
        mBtnRunBoundary.setActivated(true);

        Logger.i("Start run boundary.");
        runBoundaryWith(0, 3000)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(res -> {
                    mMovingEventSubject.onNext(false);
                    mBtnRunBoundary.setActivated(false);
                }, e -> {
                    mMovingEventSubject.onNext(false);
                    mBtnRunBoundary.setActivated(false);
                    LogHelper.log(e);
                });
    }

    private void onClickRunBoundary(View v) {
        // Show dialog once if user click run boundary for noticing potential dangerous.
        if (mBoundaryWarningFlag) {
            showBoundaryWarningDialog();
        } else {
            startRunBoundary();
        }
    }

    class MyPagerAdapter extends PagerAdapter {
        private List<View> views;

        MyPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}
