package fabscreen.features.filemanager.a400platform;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.filemanager.BrowseFileDetailViewModel;
import fabscreen.features.filemanager.BrowseFileListAdapter;
import fabscreen.features.filemanager.BrowseViewModel;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.R2;
import fabscreen.features.filemanager.j1Platform.BrowseJ1Activity;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.core.ui.view.FileParsingDialog;
import fabscreen.platform.core.ui.view.PullDownMenu;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BrowseA400FileListFragment extends BaseFragment implements BrowseFileListAdapter.OnItemClickListener {
    protected FileParsingDialog fabLoading;
    @BindView(R2.id.btn_browse_file_list_top_folder)
    TextView mTvFolder;
    @BindView(R2.id.btn_top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.rcv_browse_local_file_list)
    RecyclerView mRcvLocalFileList;
    @BindView(R2.id.btn_browse_file_list_sort_option)
    Button mBtnListSort;
    @BindView(R2.id.ll_browse_external_file_list_empty)
    LinearLayout mLlFileListEmpty;
    @BindView(R2.id.tl_browse_file_list)
    TabLayout mTabLayout;
    @BindView(R2.id.iv_browse_external_file_list_empty)
    ImageView mIvEmpty;
    @BindView(R2.id.tv_browse_external_file_list_empty_title)
    TextView mTvEmptyTitle;
    @BindView(R2.id.tv_browse_external_file_list_empty_content)
    TextView mTvEmptyContent;

    private IRouter mRouter;
    private IPrintWorkspace mPrintWorkspace;

    private IGcodeParser mParser;

    private MenuAdapter mMenuAdapter;

    private BrowseFileListAdapter mLocalFileListAdapter;
    private GridLayoutManager mGridLayoutManager;
    private BrowseFileListAdapter mExternalFileListAdapter;

    private BrowseViewModel mViewModel;
    private BrowseFileDetailViewModel browseFileDetailViewModel;
    private final int mPrintMode = 0;
    private boolean mIsJ1;
    private BrowseViewModel.FileType filterType;
    private String[] mTabs;
    private int mSelectMunPosition = 0;

    public static Fragment newInstance(int fileType) {
        // FIXME: FileType should be passed directly for use
        BrowseViewModel.FileType type = BrowseViewModel.FileType.FILE_TYPE_UNKNOWN;
        switch (fileType) {
            case 1:
                type = BrowseViewModel.FileType.FILE_TYPE_GCODE;
                break;
            case 2:
                type = BrowseViewModel.FileType.FILE_TYPE_NC;
                break;
            case 3:
                type = BrowseViewModel.FileType.FILE_TYPE_CNC;
                break;
            case 4:
                type = BrowseViewModel.FileType.FILE_TYPE_UPDATE;
                break;
            case 0:
            default:
                type = BrowseViewModel.FileType.FILE_TYPE_UNKNOWN;
                break;

        }
        Fragment fragment = new BrowseA400FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("file_type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRouter = ServiceContainer.getInstance().getService(IRouter.class);
        mPrintWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);
        mIsJ1 = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();

        mTabs = new String[]{"Local", "USB"};
        for (int i = 0; i < mTabs.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mTabs[i]));
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        //Local
                        playNormalClickSound();
                        initLocalFiles();
                        updateLocalFilesLists();
                        setTabTxtStyle(0);
                        break;
                    case 1:
                        //USB
                        playNormalClickSound();
                        initExternalFiles();
                        setTabTxtStyle(1);
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mIvEmpty.setImageResource(mIsJ1 ? R.drawable.pic_file_normal_160x160
                : R.drawable.pic_a400_file_normal);

        if (bundle != null)
            filterType = (BrowseViewModel.FileType) bundle.getSerializable("file_type");

        browseFileDetailViewModel = getViewModel();
        initLocalFiles();
        updateLocalFilesLists();
        initMenu();

        if (mIsJ1 && mViewModel.getFileManagerStateValue()) {
            initExternalFiles();
            mTabLayout.setScrollPosition(1, 0, true);
            setTabTxtStyle(1);
        } else {

            setTabTxtStyle(0);
        }

        ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                .requestPrintModeStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    int printMode = ((UInt8Prop) baseStructure.getProp("printMode")).getValue();
                    Logger.d("Print Mode " + printMode);
                });
        fabLoading = FileParsingDialog.create(requireContext()).setContent(getResources().getString(R.string.all_file_loading_tip));
    }

    //Set the TabLayout text style
    public void setTabTxtStyle(int position) {
        for (int i = 0; i < mTabs.length; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            String selectTab = tab.getText().toString().trim();

            SpannableString spannableString = new SpannableString(selectTab);
            StyleSpan styleSpan = new StyleSpan(position == i ? Typeface.BOLD : Typeface.NORMAL);
            spannableString.setSpan(styleSpan, 0, selectTab.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            tab.setText(spannableString);
        }
    }

    @Override
    protected BrowseFileDetailViewModel getViewModel() {
        return getViewModelProvider().get(BrowseFileDetailViewModel.class);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_browse_file_list;
    }

    private void initLocalFiles() {
        mViewModel = new BrowseViewModel(true, filterType);
        mViewModel.listFiles();
        mViewModel.nowFolderObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(folderName -> {
                    if (folderName.isEmpty()) {
                        if (mIsJ1) {
                            mBtnBack.setVisibility(View.VISIBLE);
                        } else {
                            mBtnBack.setVisibility(View.GONE);
                        }
                        mTabLayout.setVisibility(View.VISIBLE);
                        mTvFolder.setVisibility(View.INVISIBLE);
                    } else {
                        mTvFolder.setVisibility(View.VISIBLE);
                        mTabLayout.setVisibility(View.INVISIBLE);
                        if (mIsJ1) {
                            mTvFolder.setText(folderName);
                        } else {
                            mBtnBack.setVisibility(View.GONE);
                            mTvFolder.setText("返回上一级");
                        }
                    }
                });
    }

    private void initExternalFiles() {
        mViewModel = new BrowseViewModel(false, filterType);
        mViewModel.getFileManagerStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isAttach -> {
                    if (isAttach) {
                        mViewModel.listFiles();
                        updateUsbFilesLists();
                    } else {
                        showLocalOrUsbTip(false);
                    }
                }, LogHelper::log);
        mViewModel.nowFolderObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(folderName -> {
                    if (folderName.isEmpty()) {
                        if (mIsJ1) {
                            mBtnBack.setVisibility(View.VISIBLE);
                        } else {
                            mBtnBack.setVisibility(View.GONE);
                        }
                        mTabLayout.setVisibility(View.VISIBLE);
                        mTvFolder.setVisibility(View.INVISIBLE);
                    } else {
                        mTvFolder.setVisibility(View.VISIBLE);
                        mTabLayout.setVisibility(View.INVISIBLE);
                        if (mIsJ1) {
                            mTvFolder.setText(folderName);
                        } else {
                            mBtnBack.setVisibility(View.GONE);
                            mTvFolder.setText("返回上一级");
                        }
                    }
                });
    }

    private void updateLocalFilesLists() {
        // view
        mLocalFileListAdapter = new BrowseFileListAdapter(mIsJ1);
        mLocalFileListAdapter.setOnItemClickListener(this);
        mGridLayoutManager = new GridLayoutManager(getContext(), 5);
        mRcvLocalFileList.setAdapter(mLocalFileListAdapter);
        mRcvLocalFileList.setLayoutManager(mGridLayoutManager);

        // update file lists
        mViewModel.getFileListItemsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(items -> {
                    if (items != null && items.size() > 0) {
                        mRcvLocalFileList.setVisibility(View.VISIBLE);
                        mLlFileListEmpty.setVisibility(View.GONE);
                    } else {
                        showLocalOrUsbTip(true);
                    }
                    mLocalFileListAdapter.setFileItems(items);
                    mLocalFileListAdapter.notifyDataSetChanged();
                });
    }

    private void updateUsbFilesLists() {
        mExternalFileListAdapter = new BrowseFileListAdapter(mIsJ1);
        mExternalFileListAdapter.setOnItemClickListener(this);
        mGridLayoutManager = new GridLayoutManager(getContext(), 5);
        mRcvLocalFileList.setAdapter(mExternalFileListAdapter);
        mRcvLocalFileList.setLayoutManager(mGridLayoutManager);

        // update file lists
        mViewModel.getFileListItemsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(items -> {
                    if (items.size() > 0) {
                        mRcvLocalFileList.setVisibility(View.VISIBLE);
                        mLlFileListEmpty.setVisibility(View.GONE);
                    } else {
                        showLocalOrUsbTip(false);
                    }
                    mExternalFileListAdapter.setFileItems(items);
                    mExternalFileListAdapter.notifyDataSetChanged();
                });
    }

    public void showLocalOrUsbTip(boolean isLocal) {
        mLlFileListEmpty.setVisibility(View.VISIBLE);
        mRcvLocalFileList.setVisibility(View.GONE);
        if (mIsJ1) {
            mIvEmpty.setImageResource(isLocal ? R.drawable.pic_file_normal_160x160 : R.drawable.pic_file_error_160x160);
        }
        mTvEmptyTitle.setVisibility(isLocal ? View.GONE : View.VISIBLE);
        mTvEmptyTitle.setText(isLocal ? "" : "USB Flash Drive Not Recognized");
        mTvEmptyContent.setText(isLocal ? "Empty" : "Please replug or replace the USB flash drive.Possible cause: \n" +
                "1. The file system of the USB flash drive is not FAT32. \n" +
                "2. The USB flash drive has malfunctioned.");
    }

    private void initMenu() {
        ArrayList<String> menuItems = new ArrayList<>();
        menuItems.add("Date(ascending)");
        menuItems.add("Date(descending)");
        menuItems.add("Name(ascending)");
        menuItems.add("Name(descending)");

        mMenuAdapter = new MenuAdapter(getContext(), menuItems);
        mMenuAdapter.setOnItemClickListener((view, position) -> {
            playNormalClickSound();
            mSelectMunPosition = position;
            switch (position) {
                case 0:
                    mViewModel.setFilter(BrowseViewModel.FILTER_DATE_ASCENDING);
                    break;
                case 1:
                    mViewModel.setFilter(BrowseViewModel.FILTER_DATE_DESCENDING);
                    break;
                case 2:
                    mViewModel.setFilter(BrowseViewModel.FILTER_NAME_ASCENDING);
                    break;
                case 3:
                    mViewModel.setFilter(BrowseViewModel.FILTER_NAME_DESCENDING);
                    break;
            }
            PullDownMenu.dismiss();
        });
    }

    @OnClick(R2.id.btn_top_bar_back)
    void onClickBack() {
        playNormalClickSound();
        if (mIsJ1 && !mViewModel.nowFolderValue().isEmpty()) {
            mViewModel.popDirectory();
        } else {
            back();
        }
    }

    @OnClick(R2.id.btn_browse_file_list_top_folder)
    void onClickTopFolder() {
        playNormalClickSound();
        mViewModel.popDirectory();
    }

    @OnClick(R2.id.btn_browse_file_list_sort_option)
    void onClickSort() {
        mBtnListSort.setBackgroundResource(R.drawable.icon_screen_bg_select);
        playNormalClickSound();
        PullDownMenu.create(getContext(), mMenuAdapter)
                .setOnDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mBtnListSort.setBackgroundResource(R.drawable.icon_screen_bg_normal);
                    }
                })
                .showBelowView(mBtnListSort, -(int) DimensUtils.dp2px(300), 10);
        if (mMenuAdapter != null) {
            mMenuAdapter.setSelectPosition(mSelectMunPosition);
        }
    }

    @Override
    public void onItemClick(int position) {
        Logger.d("User click:%d", position);
        BrowseViewModel.BrowseJ1FileItem item = mViewModel.getFileListItems().get(position);
        switch (item.fileType) {
            case FILE_TYPE_NC:
                startParse(IMachine.WorkType.LASER, item.getFilePath());
                break;
            case FILE_TYPE_CNC:
                startParse(IMachine.WorkType.CNC, item.getFilePath());
                break;
            case FILE_TYPE_GCODE:
                startParse(IMachine.WorkType.FDM, item.getFilePath());
                break;
            case FILE_TYPE_UPDATE:
                Intent intent = new Intent();
                intent.putExtra("file_path", item.getFilePath());
                intent.putExtra("is_local", mViewModel.isLocal());
                finishActivityWithResultOk(intent);
                break;
            case FILE_TYPE_DIRECTORY:
                mViewModel.gotoDirectory(item.getFilePath());
                break;
            case FILE_TYPE_LOG:
            case FILE_TYPE_UNKNOWN:
            default:
                Toast.makeText(getContext(), String.format("踏入了未知领域 %d，请联系开发", item.getFilePath()), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startParse(IMachine.WorkType workType, String filePath) {
        // Show dialog when we started to parse the file, prevent causing bugs when user commit another action.
        if (!fabLoading.isShowing()) {
            fabLoading.show();
        }

        // TODO: Product requirements, document parsing ahead of time
        mParser.startParse(filePath, mViewModel.isLocal(), workType);
        mParser.getParseProgressObservable()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .takeUntil(progress -> progress == 100)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    if (progress == -1) {
                        Logger.d("Try to parse file %s failed." + filePath);
                        FabAlert.alert(getContext(), "Read file " + filePath + " failed! Please Check USB disk status.");
                        fabLoading.dismiss();
                    }
                    if (progress == 100) {
                        fabLoading.dismiss();
                        browseFileDetailViewModel.setFile(filePath, mViewModel.isLocal());
                        FragmentActivity fragmentActivity = requireActivity();
                        if (fragmentActivity instanceof BrowseJ1Activity) {
                            ((BrowseJ1Activity) fragmentActivity).gotoBrowseFileDetailFragment();
                        } else if (fragmentActivity instanceof BrowseA400Activity) {
                            ((BrowseA400Activity) fragmentActivity).gotoBrowseFileDetailFragment();
                        }

//                        handleResult(selectFile);
                    }
                }, e -> {
                    Logger.d("Try to parse file %s failed." + filePath + "\nError: " + e);
                    FabAlert.alert(getContext(), "Read file " + filePath + " failed! Please Check USB disk status.");
                    fabLoading.dismiss();
                });
    }

    @OnClick({R2.id.btn_browse_file_back, R2.id.btn_browse_file_back_title})
    public void onClickTopBack() {
        back();
    }

}
