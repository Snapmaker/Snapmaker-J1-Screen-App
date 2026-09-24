package fabscreen.features.filemanager.s20Platform;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

class BrowseViewModel extends BaseViewModel {
    static final int FILTER_NONE = 0;
    static final int FILTER_NAME_ASCENDING = 1;
    static final int FILTER_NAME_DESCENDING = 2;
    static final int FILTER_DATE_ASCENDING = 3;
    static final int FILTER_DATE_DESCENDING = 4;
    static final int FILTER_SIZE_ASCENDING = 5;
    static final int FILTER_SIZE_DESCENDING = 6;
    private static final String TAG = "BrowseViewModel";
    private CompositeDisposable disposables = new CompositeDisposable();

    // Represent files on current directory, can be subscribed on view.
    private BehaviorSubject<ArrayList<IFile>> mFilesSubject = BehaviorSubject.create();

    private int mFilter = FILTER_NONE;
    private BehaviorSubject<ArrayList<IFile>> mFilteredFilesSubject = BehaviorSubject.create();

    private IMachine.WorkType mHeadType = IMachine.WorkType.NONE;

    // return count of files being selected
    private BehaviorSubject<Integer> mSelectedFilesCountSubject = BehaviorSubject.createDefault(0);
    private ArrayList<IFile> mSelectFileList = new ArrayList<>();

    private PublishSubject<Boolean> mRouteSubject = PublishSubject.create();

    private IPartition mDevice;
    private IFileManagerService mFileManager;

    // TODO: 2021/8/24  viewModel must never reference activity context.
    @SuppressLint("AutoDispose")
    BrowseViewModel(Context context, boolean isLocal) {
        super();
        mFileManager = ServiceContainer.getInstance().getService(IFileManagerService.class);
        if (isLocal) {
            mDevice = mFileManager.getFabLocalStorageDevice();
        } else {
            ServiceContainer.getInstance().getService(IFileManagerService.class).getFileManagerStateSubjHolder().getObservable()
                    .as(bindToLifecycle())
                    .subscribe(have -> {
                        mDevice = have ? mFileManager.getDevice(isLocal) : null;
                    });
        }
        mHeadType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;

        // every time files changed, apply filter.
        disposables.add(mFilesSubject.subscribe(files -> applyFilter()));
    }

    public Observable<ArrayList<IFile>> getFiles() {
        return mFilesSubject;
    }

    public IPartition getPartition() {
        return mDevice;
    }

    /**
     * List files on current directory.
     */
    Observable<ArrayList<IFile>> listFiles() {
        Logger.d("current head: %s", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType);
        ArrayList<IFile> iFiles = null;
        try {
            iFiles = mDevice.getRootFile().listFiles();
            mFilesSubject.onNext(iFiles);
            return Observable.just(iFiles);
        } catch (Exception e) {
            LogHelper.log(e);
        }
        return Observable.just(iFiles);
    }

    private ArrayList<IFile> handleFiles(ArrayList<IFile> files) {
        Collections.reverse(files);

        ArrayList<IFile> files2 = new ArrayList<>();
        for (IFile file : files) {
            String name = file.getName();
            if (name.startsWith(".") || (name.startsWith("System Volume") && file.isDirectory())) {
                continue;
            }

            final int fileType = getFileHeadType(file);
            // Allows log files to be displayed
//            if (fileType == Constants.FILE_TYPE_UPDATE || fileType == mHeadType. || file.isDirectory() || fileType == Constants.FILE_TYPE_LOG) {
            files2.add(file);
//            } else if (mHeadType == Module.ModuleType.HEAD_LASER_10W && fileType == Constants.FILE_TYPE_LASER) {
//                files2.add(file);
//            }
        }

        return files2;
    }

    private int getFileHeadType(IFile file) {
        String suffix = file.getName()
                .substring(file.getName().lastIndexOf(".") + 1)
                .toLowerCase();
        switch (suffix) {
            case "gcode":
                return Constants.FILE_TYPE_3DP;
            case "nc":
                return Constants.FILE_TYPE_LASER;
            case "cnc":
                return Constants.FILE_TYPE_CNC;
            case "bin":
                return Constants.FILE_TYPE_UPDATE;
            case "log":
                return Constants.FILE_TYPE_LOG;
            default:
                return Constants.FILE_TYPE_UNKNOWN;
        }
    }

    /**
     * Goto subdirectory.
     *
     * @param file The subdirectory.
     */
    Observable<Boolean> gotoDirectory(IFile file) {
        if (!file.isDirectory()) {
            return Observable.just(false);
        }

        mDevice.gotoDirectory(file);

        return listFiles().map(files -> true);
    }

    Observable<Boolean> popDirectory() {
        mDevice.popDirectory();

        return listFiles().map(files -> true);
    }

    Observable<Boolean> removeFile(IFile file) {
        return Observable.fromCallable(
                () -> {
                    mDevice.removeFile(file);
                    return true;
                })
                .flatMap(success -> {
                    if (success) {
                        return listFiles().map(files -> true);
                    } else {
                        return Observable.just(false);
                    }
                });
    }

    Observable<Boolean> renameFile(IFile fabFile, @NonNull String name) {
        return Observable.fromCallable(
                () -> {
                    mDevice.renameFile(fabFile, name);
                    return true;
                })
                .flatMap(success -> {
                    if (success) {
                        return listFiles().map(files -> true);
                    } else {
                        return Observable.just(false);
                    }
                });
    }

    // -- filter

    void setFilter(int filter) {
        if (filter != mFilter) {
            mFilter = filter;

            applyFilter();
        }
    }

    private void applyFilter() {
        ArrayList<IFile> files = mFilesSubject.getValue();

        switch (mFilter) {
            case FILTER_NAME_ASCENDING:
                Collections.sort(files, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                break;
            case FILTER_DATE_DESCENDING:
                Collections.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
                break;
            case FILTER_NONE:
            default:
                break;
        }
        mFilteredFilesSubject.onNext(files);
    }

    Observable<ArrayList<IFile>> watchFilteredFiles() {
        return mFilteredFilesSubject;
    }

    // -- select

    Observable<Integer> getSelectedCount() {
        return mSelectedFilesCountSubject;
    }

    void clearSelectedCount() {
        mSelectedFilesCountSubject.onNext(0);
    }

    void addSelectedFile(IFile file) {
        mSelectFileList.add(file);
        mSelectedFilesCountSubject.onNext(mSelectFileList.size());
    }

    void removeSelectedFile(IFile file) {
        // does remove can apply with IFile?
        mSelectFileList.remove(file);
        mSelectedFilesCountSubject.onNext(mSelectFileList.size());
    }

    void removeAllSelectedFile() {
        if (mSelectFileList.size() != 0) {
            mSelectFileList.clear();
        }
        mSelectedFilesCountSubject.onNext(mSelectFileList.size());
    }

    ArrayList<String> getSelectedFileNames() {
        if (mSelectFileList.size() == 0) return null;

        ArrayList<String> filenames = new ArrayList<>();
        for (IFile file : mSelectFileList) {
            filenames.add(file.getName());
        }
        return filenames;
    }

    Observable<Boolean> deleteSelectedFiles() {
        return Observable.fromCallable(
                () -> {
                    for (IFile file : mSelectFileList) {
                        mDevice.removeFile(file);
                    }
                    return true;
                })
                .flatMap(success -> listFiles().map(files -> true));
    }

    Observable<Boolean> renameSelectedFile(String filename) {
        if (mSelectFileList.size() != 1) {
            return Observable.just(false);
        }

        IFile file = mSelectFileList.get(0);

        return Observable.fromCallable(
                () -> {
                    mDevice.renameFile(file, filename);
                    return true;
                })
                .flatMap(success -> listFiles().map(files -> true));
    }

    void dispose() {
        // TODO: TBD, device status should not affected by UI changing
//        if (mDevice instanceof FabUsbFileDevice) {
//            mDevice = null;
//        } else {
//            mDevice.close();
//        }
        disposables.dispose();
    }

    public Observable<Boolean> subscribeFileManagerState() {
        return mFileManager.getFileManagerStateSubjHolder().getObservable();
    }

    // TODO: remove this method
    public void routeToBrowseInfo() {
        mRouteSubject.onNext(true);
    }

    public Observable<Boolean> getRouteObservable() {
        return mRouteSubject.hide();
    }

}
