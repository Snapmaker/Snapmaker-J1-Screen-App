package fabscreen.features.print.a400platform;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.j1platform.PrintJ1AdjustmentContainerFragment;
import fabscreen.features.print.print.PrintChangeFilamentFragment;
import fabscreen.features.print.print.PrintChangeFilamentLandFragment;
import fabscreen.features.print.print.PrintDrawer3DPFragment;
import fabscreen.features.print.print.PrintDrawerCNCFragment;
import fabscreen.features.print.print.PrintDrawerLaserFragment;
import fabscreen.features.print.print.PrintFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.view.AreaScrollDrawerLayout;

@Route(path = RoutePath.PRINT_PRINT_A400)
public class PrintA400Activity extends BaseActivity {

    @BindView(R2.id.dl_print_layout)
    AreaScrollDrawerLayout mDlLayout;
    private OnDrawerOpenedListener mOnDrawerOpenedListener;
    IPreferences.Helper mPreferencesHelp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print);

        IMachine.WorkType workType = mMachine.getMachineInfoSubjectHolder().getValue().workType;
        mPreferencesHelp = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        if (isPrinting() || printController.getRecoveryFlag() || printController.getRemovePrintFlag()) {
            goToPrint();
        } else {
            switch (workType) {
                case FDM:
                    goToPrint();
                    break;
                case CNC:
                    goToSetOrigin();
                    break;
                case LASER:
                    gotoLaserPrepareStep();
                    break;
                case NONE:
                default:
                    new AlertDialog.Builder(this)
                            .setTitle("WORK TYPE ERROR")
                            .setMessage("Not detected work type, now is " + workType)
                            .create()
                            .show();
                    break;
            }
        }
    }

    public void getSetZ() {
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.PRINT_LASER_SET_Z_SELECT)
                .start(this);
    }

    private void gotoSetXYOriginFragment() {
        int laserPrintXYOriginModel = mPreferencesHelp.getLaserPrintXYOriginModel();
        if (laserPrintXYOriginModel == 0) {
            addFragment(R.id.print_master_container, SetXYOriginFragment.newInstance());
        }
    }

    public void goToSetOrigin() {
        addFragment(R.id.print_master_container, SetOriginFragment.newInstance());
    }

    public void goToPrint() {
        // We won't need these fragments anymore after we go to print page.
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        addFragment(R.id.print_master_container, new PrintA400Fragment());
    }

    public void gotoJ1AdjustmentContainerFragment() {
        PrintJ1AdjustmentContainerFragment fragment = new PrintJ1AdjustmentContainerFragment();
        addFragment(R.id.print_master_container, fragment);
    }

    private void initFabscreen() {
        Intent intent = getIntent();

        FragmentManager fragmentManager = getSupportFragmentManager();

        // master
        PrintFragment printFragment = new PrintFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_local", intent.getBooleanExtra("is_local", false));
        bundle.putString("file_path", intent.getStringExtra("file_path"));
        printFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .add(R.id.print_master_container, printFragment)
                .commit();

        // detail
        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        Fragment drawerFragment = null;
        switch (headType) {
            case Module.ModuleType.HEAD_3DP: {
                drawerFragment = new PrintDrawer3DPFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.print_drawer_container, drawerFragment)
                        .commit();
                break;
            }
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W: {
                drawerFragment = new PrintDrawerLaserFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.print_drawer_container, drawerFragment)
                        .commit();
                break;
            }
            case Module.ModuleType.HEAD_CNC: {
                drawerFragment = new PrintDrawerCNCFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.print_drawer_container, drawerFragment)
                        .commit();
                break;
            }
            default:
                mDlLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;
        }

        mDlLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (mOnDrawerOpenedListener != null) {
                    mOnDrawerOpenedListener.onDrawerOpened();
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (mOnDrawerOpenedListener != null) {
                    mOnDrawerOpenedListener.onDrawerClosed();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mDlLayout.setScrollBelow(getScreenHigh() - getResources().getDimension(R.dimen.height_bottom_bar));
    }

    public void setOnDrawerOpenedListener(OnDrawerOpenedListener listener) {
        mOnDrawerOpenedListener = listener;
    }

    public void gotoChangeFilamentFragment() {
        PrintChangeFilamentFragment fragment = new PrintChangeFilamentFragment();
        addFragment(R.id.print_master_container, fragment);
    }

    public void closeDrawer() {
        mDlLayout.closeDrawers();
        Logger.d("Print drawer closed.");
    }

    private int getScreenHigh() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return size.y;
    }

    public void gotoPrintCompleteFragment() {
        PrintA400CompleteFragment fragment = new PrintA400CompleteFragment();
        addFragment(R.id.print_master_container, fragment);
    }

    public void gotoPrintFilamentChangedFragment() {
        switch (getApplication().getPackageName()) {
            case "com.snapmaker.fabscreen": {
                break;
            }
            case "com.snapmaker.fabscreenj1":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                PrintChangeFilamentLandFragment fragment = new PrintChangeFilamentLandFragment();
                addFragment(R.id.print_master_container, fragment);
                break;
            default:
                break;
        }
    }

    public void goToThicknessCheck() {
        replaceFragment(R.id.print_master_container, ThicknessCheckFragment.newInstance());
    }

    public void goToTouchMaterial() {
        replaceFragment(R.id.print_master_container, TouchMaterialFragment.newInstance());
    }

    public void gotoLaserPrepareStep() {
        replaceFragment(R.id.print_master_container, A400LaserSetZFragment.newInstance());
    }

    private boolean isPrinting() {
        PrintController controller = mMachine.getPrintController();
        return controller.getPrintState() == PrintController.STATE_PRINTING || controller.getPrintState() == PrintController.STATE_PAUSED;
    }

    public void gotoA400AdjustmentContainerFragment() {
        PrintA400AdjustmentContainerFragment fragment = new PrintA400AdjustmentContainerFragment();
        addFragment(R.id.print_master_container, fragment);
    }

    interface OnDrawerOpenedListener {
        void onDrawerOpened();

        void onDrawerClosed();
    }
}
