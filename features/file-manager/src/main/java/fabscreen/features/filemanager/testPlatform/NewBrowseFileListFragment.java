package fabscreen.features.filemanager.testPlatform;

import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
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
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.features.filemanager.NewBrowseFileListAdapter;
import fabscreen.features.filemanager.NewBrowseViewModel;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.R2;
import fabscreen.features.filemanager.a400platform.BrowseA400Activity;
import fabscreen.features.filemanager.entity.BrowseShowFile;
import fabscreen.features.filemanager.entity.FileType;
import fabscreen.features.filemanager.j1Platform.BrowseJ1Activity;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.core.ui.view.PullDownMenu;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static fabscreen.features.filemanager.NewBrowseViewModel.FileCollation.FILTER_DATE_ASCENDING;
import static fabscreen.features.filemanager.NewBrowseViewModel.FileCollation.FILTER_DATE_DESCENDING;
import static fabscreen.features.filemanager.NewBrowseViewModel.FileCollation.FILTER_NAME_ASCENDING;
import static fabscreen.features.filemanager.NewBrowseViewModel.FileCollation.FILTER_NAME_DESCENDING;
import static fabscreen.features.filemanager.NewBrowseViewModel.FileCollation.FILTER_NONE;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_DIRECTORY;

public class NewBrowseFileListFragment extends BaseFragment implements NewBrowseFileListAdapter.OnItemClickListener {

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
    @BindView(R2.id.rl_browse_bottom_operation_bar)
    RelativeLayout mRlBottomOperationBar;
    @BindView(R2.id.btn_browse_file_list_edit_mode)
    Button mBtnExitMode;
    @Nullable
    @BindView(R2.id.btn_browse_move)
    Button mBtnA400Mode;
    @Nullable
    @BindView(R2.id.btn_browse_delete)
    Button mBtnA400Delete;

    @Nullable
    @BindView(R2.id.tv_browse_move)
    TextView mTvJ1Mode;
    @Nullable
    @BindView(R2.id.tv_browse_delete)
    TextView mTvJ1Delete;

    protected FileLoadingDialog fabLoading;
    protected FileLoadingDialog J1fabMoving;
    protected FileLoadingDialog A400fabMoving;
    protected FileLoadingDialog fabDeleting;

    private IGcodeParser mParser;
    private MenuAdapter mMenuAdapter;

    private FileType filterType;
    private String[] mTabs;
    private int mSelectMunPosition = 1;
    private GridLayoutManager mGridLayoutManager;
    private NewBrowseViewModel mNewViewModel;
    private NewBrowseFileListAdapter mNewFileListAdapter;
    private Disposable parserSub;
    private int mLastSelectedTabIndex = -1;

    private boolean mIsJ1;
    private boolean isFirst = true;

    @NonNull
    public static Fragment newInstance(int fileType) {
        // FIXME: FileType should be passed directly for use
        FileType type;
        switch (fileType) {
            case 1:
                type = FileType.FILE_TYPE_GCODE;
                break;
            case 2:
                type = FileType.FILE_TYPE_NC;
                break;
            case 3:
                type = FileType.FILE_TYPE_CNC;
                break;
            case 4:
                type = FileType.FILE_TYPE_UPDATE;
                break;
            case 0:
            default:
                type = FileType.FILE_TYPE_UNKNOWN;
                break;

        }
        Fragment fragment = new NewBrowseFileListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("file_type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewViewModel = getFragmentScopeViewModel(NewBrowseViewModel.class);
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);
        mIsJ1 = mNewViewModel.isIsJ1();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initMenu();
        initData();
        if (mIsJ1 && mNewViewModel.getFileManagerStateValue()) {
            mTabLayout.selectTab(mTabLayout.getTabAt(1));
        } else {
            switchStorage(0);
        }
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            filterType = (FileType) bundle.getSerializable("file_type");
            HashSet<FileType> fileTypes = new HashSet<>();
            fileTypes.add(filterType);
            fileTypes.add(FILE_TYPE_DIRECTORY);
            mNewViewModel.setFilterType(fileTypes);
        }
    }

    private void initView() {
        refreshTabs();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                playNormalClickSound();
                Logger.d("browser: tab selected, %d", tab.getPosition());
                switchStorage(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mNewViewModel.nowFolderObservable()
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

        mNewViewModel.getFileListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(items -> {
                    Logger.d("Get file list size " + items.size());
                    if (items != null && items.size() > 0) {
                        mNewFileListAdapter.setFileItems(items);
                        mNewFileListAdapter.notifyDataSetChanged();
                        mRcvLocalFileList.setVisibility(View.VISIBLE);
                        mLlFileListEmpty.setVisibility(View.GONE);
                    } else {
                        showErrorOrEmpty(true);
                    }
                });

        mIvEmpty.setImageResource(mIsJ1 ? R.drawable.pic_file_normal_160x160
                : R.drawable.pic_a400_file_normal);
        mNewFileListAdapter = new NewBrowseFileListAdapter(mIsJ1);
        mNewFileListAdapter.setOnItemClickListener(this);
        mGridLayoutManager = new GridLayoutManager(getContext(), mIsJ1 ? 4 : 5);
        mRcvLocalFileList.setAdapter(mNewFileListAdapter);
        mRcvLocalFileList.setLayoutManager(mGridLayoutManager);

        mNewViewModel.getUpdateView()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    if (mNewFileListAdapter != null) {
                        mNewFileListAdapter.notifyItemChanged(integer);
                    }
                }, LogHelper::log);
        fabLoading = FileLoadingDialog.create(requireContext(), mIsJ1).setContent(getString(R.string.all_file_loading_tip));
        fabDeleting = FileLoadingDialog.create(requireContext(), mIsJ1).setContent(getString(R.string.all_browse_deleting));
        J1fabMoving = FileLoadingDialog.create(requireContext(), mIsJ1).setContent(mNewViewModel.isLocal() ? getString(R.string.all_browse_copying_to_usb) : getString(R.string.all_browse_copying_to_local));
        A400fabMoving = FileLoadingDialog.create(requireContext(), mIsJ1);
        mNewViewModel.getDataProgressObservable()
                .throttleLast(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(dataProgress -> {
                    if (mIsJ1) {
                        J1fabMoving.setContent(getString(R.string.j1_copying_file_msg,
                                mNewViewModel.isLocal() ? getString(R.string.all_browse_copying_to_usb) : getString(R.string.all_browse_copying_to_local),
                                getMBSize(dataProgress.getCurrentSize()),
                                getMBSize(dataProgress.getTotalSize())));
                    } else {
                        A400fabMoving.setProgress((int) ((dataProgress.getCurrentSize() / (double) dataProgress.getTotalSize()) * 100));
                    }
                }, LogHelper::log);

        mNewViewModel.getUSBStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(bool -> {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        refreshTabs();
                    }
                    if (bool) {
                        if (mBtnA400Mode != null && mNewViewModel.getSelectFileSet().size() > 0) {
                            mBtnA400Mode.setEnabled(true);
                        }
                        if (mTvJ1Mode != null && mNewViewModel.getSelectFileSet().size() > 0) {
                            mTvJ1Mode.setEnabled(true);
                        }
                    } else {
                        if (mBtnA400Mode != null) {
                            mBtnA400Mode.setEnabled(false);
                        }
                        if (mTvJ1Mode != null) {
                            mTvJ1Mode.setEnabled(false);
                        }
                        /*if (mNewViewModel.isUSB()) {
                            mTabLayout.selectTab(mTabLayout.getTabAt(1 - mNewViewModel.getNowStorage().ordinal()));
//                            switchStorage(1 - mNewViewModel.getNowStorage().ordinal());
                        }*/
                    }
                });

        mNewViewModel.getSelectFileObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(set -> {
                    if (set.isEmpty()) {
                        if (mBtnA400Mode != null) {
                            mBtnA400Mode.setEnabled(false);
                        }
                        if (mTvJ1Mode != null) {
                            mTvJ1Mode.setEnabled(false);
                        }
                        if (mBtnA400Delete != null) {
                            mBtnA400Delete.setEnabled(false);
                        }
                        if (mTvJ1Delete != null) {
                            mTvJ1Delete.setEnabled(false);
                        }
                    } else {
                        if (mBtnA400Delete != null) {
                            mBtnA400Delete.setEnabled(true);
                        }
                        if (mTvJ1Delete != null) {
                            mTvJ1Delete.setEnabled(true);
                        }
                        if (mNewViewModel.getUSBStateValue()) {
                            if (mBtnA400Mode != null) {
                                mBtnA400Mode.setEnabled(true);
                            }
                            if (mTvJ1Mode != null) {
                                mTvJ1Mode.setEnabled(true);
                            }
                        }
                    }
                });
    }

    private void refreshTabs() {
        mTabs = mNewViewModel.getFileManagerStateValue() ? new String[]{getString(R.string.all_local), "USB"} : new String[]{getString(R.string.all_local)};
        mTabLayout.removeAllTabs();
        for (String tab : mTabs) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tab));
        }
    }

    private double getMBSize(long size) {
        return (double) size / 1024 / 1024;
    }

    public void switchStorage(int index) {
        if (mLastSelectedTabIndex == index) return;
        mLastSelectedTabIndex = index;
        if (mNewViewModel.isIsSelectMode()) {
            onClickEditMode();
        }
        int result = mNewViewModel.setNowStorage(index);
        Logger.d("Set result is %d, index: %d", result, index);
        switch (result) {
            case 1:
                // Do nothing?
            case 0:
                if (mNewViewModel.isErrorUsb()) {
                    // showErrorView
                    showErrorOrEmpty(!mNewViewModel.isErrorUsb());
                }
                setTabTxtStyle(result);
                setTextShow();
                break;
            case 2:
            case 3:
            default:
                mTabLayout.selectTab(mTabLayout.getTabAt(0));
                break;
        }
    }

    private void setTextShow() {
        if (mBtnA400Mode != null) {
            mBtnA400Mode.setText(mNewViewModel.isLocal() ? getString(R.string.all_browse_copy_to_usb) : getString(R.string.all_browse_copy_to_local));
        }
        if (mTvJ1Mode != null) {
            mTvJ1Mode.setText(mNewViewModel.isLocal() ? getString(R.string.all_browse_copy_to_usb) : getString(R.string.all_browse_copy_to_local));
        }
    }

    //Set the TabLayout text style
    public void setTabTxtStyle(int position) {
        for (int i = 0; i < mTabs.length; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab == null || tab.getText() == null) continue;
            String selectTab = tab.getText().toString().trim();
            SpannableString spannableString = new SpannableString(selectTab);
            StyleSpan styleSpan = new StyleSpan(position == i ? Typeface.BOLD : Typeface.NORMAL);
            spannableString.setSpan(styleSpan, 0, selectTab.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tab.setText(spannableString);
        }
    }

    @Override
    protected int getLayoutResID() {
        return mIsJ1 ? R.layout.fragment_browse_file_list : R.layout.fragment_a400_browse_file_list;
    }

    public void showErrorOrEmpty(boolean isEmpty) {
        mLlFileListEmpty.setVisibility(View.VISIBLE);
        mRcvLocalFileList.setVisibility(View.GONE);
        if (mIsJ1) {
            mIvEmpty.setImageResource(isEmpty ? R.drawable.pic_file_normal_160x160 : R.drawable.pic_file_error_160x160);
        } else {
            mIvEmpty.setImageResource(R.drawable.pic_a400_file_normal_148x148);
        }
        mTvEmptyTitle.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mTvEmptyTitle.setText(isEmpty ? "" : getString(R.string.all_browse_usb_not_recognized));
        mTvEmptyContent.setText(isEmpty ? getString(R.string.all_browse_empty) : getString(R.string.all_browse_usb_not_recognized_content));
    }

    private void initMenu() {
        ArrayList<String> menuItems = new ArrayList<>();
        menuItems.add(getString(R.string.j1_file_date_ascending));
        menuItems.add(getString(R.string.j1_file_date_descending));
        menuItems.add(getString(R.string.j1_file_name_ascending));
        menuItems.add(getString(R.string.j1_file_name_descending));
        mMenuAdapter = new MenuAdapter(getContext(), menuItems);
        mMenuAdapter.setOnItemClickListener((view, position) -> {
            playNormalClickSound();
            mSelectMunPosition = position;
            switch (position) {
                case 0:
                    mNewViewModel.setFileCollation(FILTER_DATE_ASCENDING);
                    break;
                case 1:
                    mNewViewModel.setFileCollation(FILTER_DATE_DESCENDING);
                    break;
                case 2:
                    mNewViewModel.setFileCollation(FILTER_NAME_ASCENDING);
                    break;
                case 3:
                    mNewViewModel.setFileCollation(FILTER_NAME_DESCENDING);
                    break;
                default:
                    mNewViewModel.setFileCollation(FILTER_NONE);
                    break;
            }
            PullDownMenu.dismiss();
        });
    }

    @Override
    public void onItemClick(int position) {
        Logger.d("User click:%d", position);
        BrowseShowFile browseFile = mNewViewModel.getFileListValues().get(position);

        if (mNewViewModel.getNowStorage() == NewBrowseViewModel.StorageMedium.PARTITION_LOCAL) {
            onChoosingFile(browseFile, position);
        } else {
            mNewViewModel.checkAvailableSpace(browseFile)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(operationResults -> {
                       switch (operationResults.result) {
                           case 0:
                           case 1:
                               onChoosingFile(browseFile, position);
                               break;
                           case 2:
                               Logger.w("Insufficient System Storage.");
                               mNewViewModel.freeUpStorageSpace(browseFile);
                               onChoosingFile(browseFile, position);
                               break;
                           case 3:
                               Logger.w("Low System Storage.");
                               onChoosingFile(browseFile, position);
                               break;
                       }
                    });
        }

    }

    private void onChoosingFile(BrowseShowFile browseFile, int position) {
        switch (browseFile.getFileType()) {
            case FILE_TYPE_NC:
                startParse(IMachine.WorkType.LASER, browseFile);
                break;
            case FILE_TYPE_CNC:
                startParse(IMachine.WorkType.CNC, browseFile);
                break;
            case FILE_TYPE_GCODE:
                startParse(IMachine.WorkType.FDM, browseFile);
                break;
            case FILE_TYPE_UPDATE:
                if (mIsJ1) {
                    userConfirmUpdate(browseFile);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("file_path", browseFile.getIFile().getAbsolutePath());
                    intent.putExtra("is_local", mNewViewModel.isLocal());
                    finishActivityWithResultOk(intent);
                }
                break;
            case FILE_TYPE_DIRECTORY:
                mNewViewModel.gotoDirectory(position);
                break;
            case FILE_TYPE_LOG:
            case FILE_TYPE_UNKNOWN:
            default:
                Toast.makeText(getContext(), String.format("踏入了未知领域 %d，请联系开发", browseFile.getIFile().getAbsolutePath()), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void userConfirmUpdate(BrowseShowFile browseShowFile) {
        DecisionDialog.create(requireContext())
                .setContent(R.string.j1_update_firmware_mag)
                .setType(DecisionDialog.TIP_TYPE)
                .setDialogStatus(2, false, false, false, true)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                    /*requireActivity().setResult(Activity.RESULT_CANCELED);
                    requireActivity().finish();*/
                })
                .setSecondTv(R.string.all_update, R.color.select_dialog_orange_txt, (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("file_path", browseShowFile.getIFile().getAbsolutePath());
                    intent.putExtra("is_local", browseShowFile.getIFile().isLocal());
                    finishActivityWithResultOk(intent);
                })
                .show();
    }

    private void startParse(IMachine.WorkType workType, BrowseShowFile browseShowFile) {
        IFile iFile = browseShowFile.getIFile();
        // Show dialog when we started to parse the file, prevent causing bugs when user commit another action.
        if (!fabLoading.isShowing()) {
            fabLoading.show();
        }

        // TODO: Product requirements, document parsing ahead of time
        mParser.startParse(iFile, workType);

        if (parserSub != null && !parserSub.isDisposed()) parserSub.dispose();
        parserSub = mParser.getParseProgressObservable()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .takeUntil(progress -> progress == 100)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    if (progress == -1) {
                        Logger.d("Try to parse file %s failed." + iFile.getAbsolutePath());
                        fabLoading.dismiss();
                        DecisionDialog.create(requireContext())
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                .setTitle(R.string.j1_Failed_to_Parse_File_title)
                                .setContent(R.string.j1_Failed_to_Parse_File_msg)
                                .setFirstTv(getString(R.string.all_j1_i_know), R.color.select_dialog_orange_txt, (dialog, which) -> {
                                    dialog.dismiss();
                                }).show();
                    }
                    if (progress == 100) {
                        fabLoading.dismiss();
                        FragmentActivity fragmentActivity = requireActivity();
                        if (fragmentActivity instanceof BrowseJ1Activity) {
                            ((BrowseJ1Activity) fragmentActivity).setShowFile(browseShowFile);
                            ((BrowseJ1Activity) fragmentActivity).gotoBrowseFileDetailFragment();
                        } else if (fragmentActivity instanceof BrowseA400Activity) {
                            ((BrowseA400Activity) fragmentActivity).setShowFile(browseShowFile);
                            ((BrowseA400Activity) fragmentActivity).gotoBrowseFileDetailFragment();
                        }
                    }
                }, e -> {
                    Logger.d("Try to parse file %s failed." + iFile.getAbsolutePath() + "\nError: " + e);
                    fabLoading.dismiss();
                    DecisionDialog.create(requireContext())
                            .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                            .setTitle(R.string.j1_Failed_to_Parse_File_title)
                            .setContent(R.string.j1_Failed_to_Parse_File_msg)
                            .setFirstTv(getString(R.string.all_j1_i_know), R.color.select_dialog_orange_txt, (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                });
    }


    @Override
    public void onItemSelect(int position, boolean state) {
        playNormalClickSound();
        if (state) {
            mNewViewModel.addSelectFileList(position);
        } else {
            mNewViewModel.removeSelectFileList(position);
        }
    }

    @Optional
    @OnClick({R2.id.btn_browse_file_back, R2.id.btn_browse_file_back_title})
    public void onClickTopBack() {
        if (mNewViewModel.isIsSelectMode()) {
            onClickEditMode();
        }
        back();
    }

    @OnClick(R2.id.btn_top_bar_back)
    void onClickBack() {
        playNormalClickSound();
        if (mIsJ1 && !mNewViewModel.nowFolderValue().isEmpty()) {
            mNewViewModel.popDirectory();
        } else {
            back();
        }
    }

    @OnClick(R2.id.btn_browse_file_list_top_folder)
    void onClickTopFolder() {
        playNormalClickSound();
        mNewViewModel.popDirectory();
    }

    @OnClick(R2.id.btn_browse_file_list_sort_option)
    void onClickSort() {
        playNormalClickSound();
        if (mMenuAdapter != null) {
            mMenuAdapter.setSelectPosition(mSelectMunPosition);
        }
        if (mIsJ1) {
            PullDownMenu.create(getContext(), mMenuAdapter)
                    .showBelowView(mBtnListSort, -(int) DimensUtils.dp2px(190), 10);
        } else {
            mBtnListSort.setBackgroundResource(R.drawable.icon_screen_bg_select);
            PullDownMenu.create(getContext(), mMenuAdapter)
                    .setOnDismiss(() -> mBtnListSort.setBackgroundResource(R.drawable.icon_screen_bg_normal))
                    .showBelowView(mBtnListSort, -(int) DimensUtils.dp2px(300), 10);
        }
    }

    @Optional
    @OnClick({R2.id.tv_browse_move, R2.id.btn_browse_move})
    public void onClickMove() {
        playNormalClickSound();
        if (mIsJ1) {
            if (!J1fabMoving.isShowing()) {
                J1fabMoving.show();
            }
        } else {
            if (!A400fabMoving.isShowing()) {
                A400fabMoving.setContent(mNewViewModel.isLocal() ? getString(R.string.all_browse_copying_to_usb) : getString(R.string.all_browse_copying_to_local));
                A400fabMoving.show();
            }
        }
        mNewViewModel.copySelectFiles()
                .takeUntil(operationResults -> true)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(operationResults -> {
                    if (mIsJ1) {
                        J1fabMoving.dismiss();
                    } else {
                        A400fabMoving.dismiss();
                    }
                    switch (operationResults.result) {
                        case 0:
                            if (mIsJ1) {
                                new SuperToastHelper.Builder()
                                        .setDrawable(R.drawable.ic_toast_success)
                                        .setMessage(getString(R.string.j1_copy_success))
                                        .build()
                                        .showToast(requireContext());
                            } else {
                                new SuperToastHelper.Builder()
                                        .setDrawable(R.drawable.ic_pic_a400_success_68x68)
                                        .setTitle(operationResults.message)
                                        .setMessage(getString(R.string.A400_copy_success))
                                        .build()
                                        .showToast(requireContext());
                            }
                            mTabLayout.selectTab(mTabLayout.getTabAt(1 - mNewViewModel.getNowStorage().ordinal()));
//                            switchStorage(1 - mNewViewModel.getNowStorage().ordinal());
                            break;
                        case 1:
                            onClickEditMode();
                            break;
                        case -1:
                        default:
                            if (mIsJ1) {
                                DecisionDialog.create(getContext())
                                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                        .setTitle(R.string.j1_file_details_failed_copy_title)
                                        .setContent(R.string.j1_file_details_failed_copy_content)
                                        .setContentColor(R.color.palette_grey_french)
                                        .setFirstTv(R.string.all_ok, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                                            dialog.dismiss();
                                            onClickEditMode();
                                        }))
                                        .show();
                            } else {
                                new SuperToastHelper.Builder()
                                        .setDrawable(R.drawable.ic_pic_a400_error_68x68)
                                        .setTitle(operationResults.message)
                                        .setMessage(getString(R.string.a400_browse_copy_error))
                                        .build()
                                        .showToast(requireContext());
                                onClickEditMode();
                            }
                            break;
                    }
                });
    }

    @Optional
    @OnClick(R2.id.btn_browse_cancel)
    public void onClickCancel() {
        onClickEditMode();
    }

    @Optional
    @OnClick({R2.id.tv_browse_delete, R2.id.btn_browse_delete})
    public void onClickDelete() {
        playNormalClickSound();
        fabDeleting.show();
        mNewViewModel.deleteSelectFiles()
                .takeUntil(operationResults -> true)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(operationResults -> {
                    fabDeleting.dismiss();
                    switch (operationResults.result) {
                        case 0:
                            if (mIsJ1) {
                                new SuperToastHelper.Builder()
                                        .setDrawable(R.drawable.ic_toast_success)
                                        .setMessage(getString(R.string.j1_deldete_success))
                                        .build()
                                        .showToast(requireContext());
                            } else {
                                new SuperToastHelper.Builder()
                                        .setDrawable(R.drawable.ic_pic_a400_success_68x68)
                                        .setTitle(operationResults.message)
                                        .setMessage(getString(R.string.a400_delete_success))
                                        .build()
                                        .showToast(requireContext());
                            }
                            onClickEditMode();
                            mNewViewModel.updateDirectory();
                            break;
                        case 1:
                            onClickEditMode();
                            break;
                        case -1:
                        default:
                            if (mIsJ1) {
                                // TODO: Ask the product manager
                                DecisionDialog.create(requireContext())
                                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                        .setTitle(R.string.j1_fail_to_delete_title)
                                        .setContent(R.string.j1_fail_to_delete_msg)
                                        .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();

                            } else {
                                new SuperToastHelper.Builder()
                                        .setDrawable(R.drawable.ic_pic_a400_error_68x68)
                                        .setTitle(operationResults.message)
                                        .setMessage(getString(R.string.a400_browse_delete_error))
                                        .build()
                                        .showToast(requireContext());

                            }
                            onClickEditMode();
                            mNewViewModel.updateDirectory();
                            break;
                    }

                });

    }

    @OnClick(R2.id.btn_browse_file_list_edit_mode)
    void onClickEditMode() {
        playNormalClickSound();
        if (mNewViewModel.isIsSelectMode()) {
            mNewViewModel.setIsSelectMode(false);
            mNewFileListAdapter.setMultipleSelection(false);
            mRlBottomOperationBar.setVisibility(View.INVISIBLE);
            mBtnExitMode.setBackgroundResource(mIsJ1 ? R.drawable.browse_j1_edit_background : R.drawable.browse_a400_edit_background);
        } else {
            mNewViewModel.setIsSelectMode(true);
            mNewFileListAdapter.setMultipleSelection(true);
            mRlBottomOperationBar.setVisibility(View.VISIBLE);
            mBtnExitMode.setBackgroundResource(mIsJ1 ? R.drawable.browse_j1_edit_exit_background : R.drawable.browse_a400_edit_exit_background);
        }
    }
}
