package fabscreen.features.filemanager.j1Platform;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.ButterKnife;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.entity.BrowseShowFile;
import fabscreen.features.filemanager.testPlatform.NewBrowseFileListFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.FILE_BROWSE_J1)
public class BrowseJ1Activity extends BaseActivity {
    BrowseShowFile TempFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);
        ButterKnife.bind(this);

        gotoBrowseJ1FileListFragment(getIntent().getIntExtra("file_type", 0));
    }

    public void gotoBrowseJ1FileListFragment(int fileType) {
        addFragment(R.id.fragment_container, NewBrowseFileListFragment.newInstance(fileType));
    }

    public void gotoBrowseFileDetailFragment() {
        BrowseJ1FileDetailFragment fragment = new BrowseJ1FileDetailFragment();
        addFragment(R.id.fragment_container, fragment);

    }

    public BrowseShowFile getShowFile() {
        return TempFile;
    }

    public void setShowFile(BrowseShowFile browseShowFile) {
        TempFile = browseShowFile;
    }
}
