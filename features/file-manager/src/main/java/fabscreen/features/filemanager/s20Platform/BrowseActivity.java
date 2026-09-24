package fabscreen.features.filemanager.s20Platform;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.ButterKnife;
import fabscreen.features.filemanager.R;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.FILE_BROWSER)
public class BrowseActivity extends BaseActivity {
    int mHeadType = Module.ModuleType.HEAD_UNPLUGGED;
    int mMachineType = Constants.MACHINE_UNKNOWN;
    MachineInfo mMachineInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);
        ButterKnife.bind(this);

        mMachineInfo = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
        mMachineType = mMachineInfo.seriesId;
        BrowseFragment fragment = new BrowseFragment();
        addFragment(R.id.fragment_container, fragment);
//        switch (mMachineType) {
//            case Constants.MACHINE_UNKNOWN:
//            case Constants.MACHINE_A_0: {
//                BrowseFragment fragment = new BrowseFragment();
//                addFragment(R.id.fragment_container, fragment);
//                break;
//            }
//            case Constants.MACHINE_A_400:
//            case Constants.MACHINE_J_1: {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                BrowseLandFragment fragment = new BrowseLandFragment();
//                addFragment(R.id.fragment_container, fragment);
//                break;
//            }
//            default:
//                break;
//        }
    }

    public void gotoBrowseInfoJ1Fragment() {
        BrowseInfoJ1Fragment fragment = new BrowseInfoJ1Fragment();
        addFragment(R.id.fragment_container, fragment);
    }
}
