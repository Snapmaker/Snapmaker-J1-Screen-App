package fabscreen.features.filemanager.a400platform;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.ButterKnife;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.entity.BrowseShowFile;
import fabscreen.features.filemanager.testPlatform.NewBrowseFileListFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.FILE_BROWSE_A400)
public class BrowseA400Activity extends BaseActivity {
    BrowseShowFile TempFile;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_have_top);
        ButterKnife.bind(this);
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        gotoBrowseFileListFragment(getIntent().getIntExtra("file_type", workType.ordinal()));
    }

    public void gotoBrowseFileListFragment(int fileType) {
        addFragment(R.id.fragment_container, NewBrowseFileListFragment.newInstance(fileType));
    }

    public void gotoBrowseFileDetailFragment() {
        BrowseA400FileDetailFragment fragment = new BrowseA400FileDetailFragment();
        addFragment(R.id.fragment_container, fragment, false);
    }

    public BrowseShowFile getShowFile() {
        return TempFile;
    }

    public void setShowFile(BrowseShowFile browseShowFile) {
        TempFile = browseShowFile;
    }
}
