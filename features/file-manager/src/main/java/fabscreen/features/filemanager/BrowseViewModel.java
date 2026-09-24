package fabscreen.features.filemanager;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabUsbPartition;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.helper.ThumbnailExtractor;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class BrowseViewModel extends BaseViewModel {
    public static final int FILTER_NONE = 0;
    public static final int FILTER_NAME_ASCENDING = 1;
    public static final int FILTER_NAME_DESCENDING = 2;
    public static final int FILTER_DATE_ASCENDING = 3;
    public static final int FILTER_DATE_DESCENDING = 4;
    public static final int FILTER_SIZE_ASCENDING = 5;
    public static final int FILTER_SIZE_DESCENDING = 6;

    public enum FileType {
        FILE_TYPE_UNKNOWN,
        FILE_TYPE_DIRECTORY,
        FILE_TYPE_GCODE,
        FILE_TYPE_NC,
        FILE_TYPE_CNC,
        FILE_TYPE_LOG,
        FILE_TYPE_UPDATE
    }


    public enum FsPartition {
        PARTITION_LOCAL,
        PARTITION_EXTERNAL
    }

    private CompositeDisposable disposables = new CompositeDisposable();

    // Represent files on current directory, can be subscribed on view.
    private BehaviorSubject<ArrayList<IFile>> mFilesSubject = BehaviorSubject.create();

    private BehaviorSubject<ArrayList<BrowseJ1FileItem>> mFileListItemsSubject = BehaviorSubject.createDefault(new ArrayList<>());
    private int mFilter = FILTER_NONE;
    private BehaviorSubject<ArrayList<IFile>> mFilteredFilesSubject = BehaviorSubject.create();

    // return count of files being selected
    private BehaviorSubject<Integer> mSelectedFilesCountSubject = BehaviorSubject.createDefault(0);
    private ArrayList<IFile> mSelectFileList = new ArrayList<>();


    private BehaviorSubject<FsPartition> mCurrentPartition = BehaviorSubject.createDefault(FsPartition.PARTITION_LOCAL);

    private IPartition mPartition;
    private IFileManagerService mFileManagerService;
    private FileType mFilterType = FileType.FILE_TYPE_UNKNOWN;
    private BehaviorSubject<String> mNowFolderSubject = BehaviorSubject.createDefault("");

    public BrowseViewModel(boolean isLocal, FileType filterType) {
        super();
        mFileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        mPartition = mFileManagerService.getDevice(isLocal);
        mFilesSubject.as(bindToLifecycle()).subscribe(files -> applyFilter());
        mCurrentPartition.onNext(isLocal ? FsPartition.PARTITION_LOCAL : FsPartition.PARTITION_EXTERNAL);
        mFilterType = filterType;
        mCurrentPartition
                .as(bindToLifecycle())
                .subscribe(fsPartition -> {
                    if (fsPartition == FsPartition.PARTITION_EXTERNAL) {
                        Logger.d("Loading external partition...");
                        listenUsbState();
                    }
                });
    }

    public boolean isLocal() {
        return mCurrentPartition.getValue() == FsPartition.PARTITION_LOCAL;
    }

    public Observable<String> nowFolderObservable() {
        return mNowFolderSubject.hide();
    }

    public String nowFolderValue() {
        return mNowFolderSubject.getValue();
    }

    public Observable<ArrayList<IFile>> getFiles() {
        return mFilesSubject;
    }

    public ArrayList<BrowseJ1FileItem> getFileListItems() {
        return mFileListItemsSubject.getValue();
    }

    public Observable<ArrayList<BrowseJ1FileItem>> getFileListItemsObservable() {
        return mFileListItemsSubject.hide();
    }

    void listenUsbState() {
        getFileManagerStateObservable()
                .as(bindToLifecycle())
                .subscribe();
    }

    public Observable<Boolean> getFileManagerStateObservable() {
        return mFileManagerService.getFileManagerStateSubjHolder().getObservable();
    }

    public boolean getFileManagerStateValue() {
        return mFileManagerService.getFileManagerStateSubjHolder().getValue();
    }


    /**
     * List files on current directory.
     */
    public void listFiles() {
        Logger.d("current head: %s", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType);
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            try {
                ArrayList<IFile> files;
                // FIXME: 2022/6/29 mPartition may be null here!
                IFile currentDirectory = mPartition.getCurrentDirectory();
                files = currentDirectory.listFiles();
                ArrayList<IFile> files2 = handleFiles(files);
                mFilesSubject.onNext(files2);
                mFileListItemsSubject.onNext(updateFileItems(files2));
                String s = mPartition.isRoot() ? "" : currentDirectory.getName();
                Logger.d("---FDT--- listFiles " + s);
                mNowFolderSubject.onNext(s);
            } catch (IOException e) {
                LogHelper.log(e);
            }
        });
    }

    private ArrayList<BrowseJ1FileItem> updateFileItems(ArrayList<IFile> files) {
        ArrayList<BrowseJ1FileItem> fileItems = new ArrayList<>();
        for (IFile file : files) {
            FileType fileHeadType = getFileHeadType(file);
            if (mFilterType != FileType.FILE_TYPE_UNKNOWN && mFilterType != fileHeadType && fileHeadType != FileType.FILE_TYPE_DIRECTORY)
                continue;
            BrowseJ1FileItem item = new BrowseJ1FileItem(null, file);
            item.fileType = fileHeadType;
            item.thumbnailPath = ThumbnailExtractor.extract(item.filePath, isLocal());
            fileItems.add(item);
        }
        return fileItems;
    }


    private ArrayList<IFile> handleFiles(ArrayList<IFile> files) {
        Collections.reverse(files);

        ArrayList<IFile> files2 = new ArrayList<>();
        for (IFile file : files) {
            String name = file.getName();
            if (name.startsWith(".") || (name.startsWith("System Volume") && file.isDirectory())) {
                continue;
            }

//            final FileType fileType = getFileHeadType(file);
            // Allows log files to be displayed
            files2.add(file);
        }

        return files2;
    }

    private FileType getFileHeadType(IFile file) {
        // FIXME
        if (file == null) return FileType.FILE_TYPE_UNKNOWN;

        if (file.isDirectory()) {
            return FileType.FILE_TYPE_DIRECTORY;
        } else {
            String suffix = file.getName()
                    .substring(file.getName().lastIndexOf(".") + 1)
                    .toLowerCase();
            switch (suffix) {
                case "gcode":
                    return FileType.FILE_TYPE_GCODE;
                case "nc":
                    return FileType.FILE_TYPE_NC;
                case "cnc":
                    return FileType.FILE_TYPE_CNC;
                case "bin":
                    return FileType.FILE_TYPE_UPDATE;
                case "log":
                    return FileType.FILE_TYPE_LOG;
                default:
                    return FileType.FILE_TYPE_UNKNOWN;
            }
        }
    }

    // -- Filter
    public void setFilter(int filter) {
        if (filter != mFilter) {
            mFilter = filter;

            applyFilter();
        }
    }

    private void applyFilter() {
        ArrayList<IFile> files = mFilesSubject.getValue();
        if (files == null || files.isEmpty()) return;
        switch (mFilter) {
            case FILTER_NAME_ASCENDING:
                Collections.sort(files, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                break;
            case FILTER_NAME_DESCENDING:
                Collections.sort(files, (o1, o2) -> o2.getName().compareToIgnoreCase(o1.getName()));
                break;
            case FILTER_DATE_ASCENDING:
                Collections.sort(files, (o1, o2) -> Long.compare(o1.lastModified(), o2.lastModified()));
                break;
            case FILTER_DATE_DESCENDING:
                Collections.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
                break;
            case FILTER_NONE:
            default:
                break;
        }
        mFilteredFilesSubject.onNext(files);
        mFileListItemsSubject.onNext(updateFileItems(files));
    }

    // -- Directory

    /**
     * Goto subdirectory.
     */
    public Observable<Boolean> gotoDirectory(String filePath) {
        IFile file = mPartition.search(filePath);
        if (!file.isDirectory()) {
            return Observable.just(false);
        }

        mPartition.gotoDirectory(file);
        listFiles();
        return Observable.just(true);
    }

    public void popDirectory() {
        mPartition.popDirectory();
        listFiles();
    }

    Observable<Boolean> removeFile(IFile file) {
        return Observable.fromCallable(
                        () -> {
                            mPartition.removeFile(file);
                            return true;
                        })
                .flatMap(success -> {
                    if (success) {
                        listFiles();
                        return Observable.just(true);
                    } else {
                        return Observable.just(false);
                    }
                });
    }

    Observable<Boolean> renameFile(IFile fabFile, @NonNull String name) {
        return Observable.fromCallable(
                        () -> {
                            mPartition.renameFile(fabFile, name);
                            return true;
                        })
                .flatMap(success -> {
                    if (success) {
                        listFiles();
                        return Observable.just(true);
                    } else {
                        return Observable.just(false);
                    }
                });
    }

    void dispose() {
        if (mPartition instanceof FabUsbPartition) {
//            mPartition = null;
        } else {
//            mFileManager.close();
        }
        disposables.dispose();
    }

    public static class BrowseJ1FileItem {
        public Bitmap fileThumbnail;

        //        // TODO: refactor this.
//        //  We decide not to store IFile in item, only view relative data will be set in.
//        //  Besides that, we should use hashmap or other structure to get file from item list.
//        @Deprecated
//        public IFile mFile;
        public String filePath;

        public String filename;
        public long lastModified;
        // file length (check @IFile length())
        public long fileLength;
        public FileType fileType;
        public String thumbnailPath;

        public BrowseJ1FileItem(Bitmap fileThumbnail, IFile file) {
            this.fileThumbnail = fileThumbnail;
            filePath = file.getAbsolutePath();
            filename = file.getName();
            lastModified = file.lastModified();
            fileLength = file.length();
        }

        //        @Deprecated
//        public IFile getFile() {
//            return mFile;
//        }
//
//        @Deprecated
//        public void setFile(IFile file) {
//            mFile = file;
//        }
//
        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String path) {
            filePath = path;
        }

        public Bitmap getFileThumbnail() {
            return fileThumbnail;
        }

        public void setFileThumbnail(Bitmap fileThumbnail) {
            this.fileThumbnail = fileThumbnail;
        }

        public String getFilename() {
            return filename;
        }

        public long getFileLastModified() {
            return lastModified;
        }

        public long getFileLength() {
            return fileLength;
        }
    }

}
