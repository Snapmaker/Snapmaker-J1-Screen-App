package fabscreen.features.print.print;

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
import butterknife.ButterKnife;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.view.AreaScrollDrawerLayout;

@Route(path = RoutePath.PRINT_PRINT)
public class PrintActivity extends BaseActivity {

    @BindView(R2.id.dl_print_layout)
    AreaScrollDrawerLayout mDlLayout;
    private OnDrawerOpenedListener mOnDrawerOpenedListener;

    MachineInfo mMachineInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print);

        mMachineInfo = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();

        ButterKnife.bind(this);

        switch (mMachineInfo.modelId) {
            case IMachine.MachineModel.A250:
            case IMachine.MachineModel.A350: {
                initFabscreen();
                break;
            }
            case IMachine.MachineModel.A400:
            case IMachine.MachineModel.J1: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                PrintLandFragment fragment = new PrintLandFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.print_master_container, fragment)
                        .commit();
                break;
            }
            case IMachine.MachineModel.UNDEFINED:
            default:
                break;
        }
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        PrintCompleteLandFragment fragment = new PrintCompleteLandFragment();
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

    interface OnDrawerOpenedListener {
        void onDrawerOpened();

        void onDrawerClosed();
    }
}
