package fabscreen.features.filemanager;

import static fabscreen.features.filemanager.NewBrowseViewModel.StorageMedium.PARTITION_USB;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_CNC;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_DIRECTORY;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_GCODE;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_LOG;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_NC;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_UNKNOWN;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_UPDATE;

import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import fabscreen.features.filemanager.entity.BrowseShowFile;
import fabscreen.features.filemanager.entity.FileType;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileInputStream;
import fabscreen.platform.base.lib.file.FabFileOutputStream;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.helper.ThumbnailExtractor;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class NewBrowseViewModel extends BaseViewModel {
    public static final int MODE_NORMAL = 101;
    public static final int MODE_DISABLE_DUAL_EXTRUSION = 102;
    public static final int MODE_OUT_OF_RANGE = 103;

    private IPartition mPartition;
    private IFileManagerService mFileManagerService;

    private Scheduler.Worker worker = Schedulers.io().createWorker();
    private StorageMedium mNowStorage = StorageMedium.PARTITION_CLOUD;

    private Set<FileType> mPrintFileType = new HashSet<>();
    private Set<FileType> mFilterType = Collections.singleton(FILE_TYPE_UNKNOWN);

    private boolean mIsJ1;
    private boolean mIsSelectMode;

    private Set<BrowseShowFile> mSelectFileSet = new HashSet<>();
    private FileCollation mFileCollation = FileCollation.FILTER_DATE_DESCENDING;

    private PublishSubject<Integer> mUpdateView = PublishSubject.create();
    private BehaviorSubject<ArrayList<BrowseShowFile>> mFileListItemsSubject = BehaviorSubject.createDefault(new ArrayList<>());

    private PublishSubject<BrowseShowFile> mUpdateFileViewSubject = PublishSubject.create();
    private PublishSubject<BrowseShowFile> mReadJpgViewSubject = PublishSubject.create();
    BehaviorSubject<DataProgress> mDataProgress = BehaviorSubject.create();
    private BehaviorSubject<Set<BrowseShowFile>> mSelectFileSetSubject = BehaviorSubject.createDefault(mSelectFileSet);
    private BehaviorSubject<Boolean> mIsHaveUSBState = BehaviorSubject.createDefault(false);
    private BehaviorSubject<String> mNowFolderSubject = BehaviorSubject.createDefault("");

    public NewBrowseViewModel() {
        super();
        mIsJ1 = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J;
        mFileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        // init mIsHaveUSBState value
        mIsHaveUSBState.onNext(mFileManagerService.getFileManagerStateSubjHolder().getValue());
        mFileManagerService.getFileManagerStateSubjHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    mIsHaveUSBState.onNext(aBoolean);
                }, LogHelper::log);
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        HashSet<FileType> fileTypes = new HashSet<>();
        fileTypes.add(FILE_TYPE_DIRECTORY);
        switch (workType) {
            case CNC:
                fileTypes.add(FILE_TYPE_CNC);
                break;
            case FDM:
                fileTypes.add(FILE_TYPE_GCODE);
                break;
            case LASER:
                fileTypes.add(FILE_TYPE_NC);
                break;
            case NONE:
            default:
                fileTypes.add(FILE_TYPE_UNKNOWN);
                break;
        }
        mFilterType = fileTypes;

        mUpdateFileViewSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(browseFile -> {
                    ArrayList<BrowseShowFile> value = mFileListItemsSubject.getValue();
                    for (int i = 0; i < value.size(); i++) {
                        if (value.get(i).getIFile().getName().equals(browseFile.getIFile().getName())) {
                            mUpdateView.onNext(i);
                            return;
                        }
                    }
                }, LogHelper::log);
        mPrintFileType.add(FILE_TYPE_CNC);
        mPrintFileType.add(FILE_TYPE_GCODE);
        mPrintFileType.add(FILE_TYPE_NC);
        mReadJpgViewSubject
                .observeOn(Schedulers.io())
                .as(bindToLifecycle())
                .subscribe(browseFile -> {
                    String extract = ThumbnailExtractor.extract(browseFile.getIFile());
                    browseFile.setShowPic(extract);
                    if (browseFile.isIsSetView()) {
                        mUpdateFileViewSubject.onNext(browseFile);
                    }
                }, LogHelper::log);
    }

    public Observable<Integer> getUpdateView() {
        return mUpdateView.hide();
    }

    public int setNowStorage(int index) {
        // Enumeration range exceeded
        if (index >= StorageMedium.values().length) return 2;
        return setNowStorage(StorageMedium.values()[index]);
    }

    public int setNowStorage(StorageMedium storageMedium) {
        if (storageMedium == PARTITION_USB && !mIsHaveUSBState.getValue()) {
            // Unable to switch without USB
            return 3;
        } else if (mNowStorage == null) {
            mNowStorage = storageMedium;
            mPartition = mFileManagerService.getDevice(isLocal());
            getFiles();
            return 0;
        } else if (mNowStorage != storageMedium) {
            mNowStorage = storageMedium;
            // TODO:Support more storage media
            mPartition = mFileManagerService.getDevice(isLocal());
            if (mPartition != null) {
                while (!mPartition.isRoot()) {
                    mPartition.popDirectory();
                }
            }
            getFiles();
            // Correct execution
            return 0;
        } else {
            // Unknown error (same pointing position?)
            return 1;
        }
    }

    public boolean isLocal() {
        return mNowStorage.ordinal() == 0;
    }

    public boolean isUSB() {
        return mNowStorage.ordinal() == 1;
    }

    public void switchStorage() {
        setNowStorage(1 - mNowStorage.ordinal());
    }

    public StorageMedium getNowStorage() {
        return mNowStorage;
    }

    public boolean isIsJ1() {
        return mIsJ1;
    }

    public Observable<String> nowFolderObservable() {
        return mNowFolderSubject.hide();
    }

    public void setFilterType(Set<FileType> fileTypeSet) {
        mFilterType = fileTypeSet;
    }

    public void addFilterType(FileType fileType) {
        mFilterType.add(fileType);
    }

    public void removeFilterType(FileType fileType) {
        mFilterType.remove(fileType);
    }

    public void setFileCollation(int fileCollationIndex) {
        mFileCollation = FileCollation.values()[fileCollationIndex];
    }

    public void setFileCollation(FileCollation fileCollation) {
        mFileCollation = fileCollation;
        mFileListItemsSubject.onNext(fileSorting(mFileListItemsSubject.getValue()));
    }


    private void getFiles() {
        worker.schedule(() -> {
            try {
                if (mPartition == null) {
                    return;
                }
                IFile currentDirectory = mPartition.getCurrentDirectory();
                ArrayList<IFile> files = currentDirectory.listFiles();
                ArrayList<BrowseShowFile> browseFiles = fileFiltering(files);
                documentProcessing(browseFiles);
                mFileListItemsSubject.onNext(fileSorting(browseFiles));
                String nowFolder = mPartition.isRoot() ? "" : currentDirectory.getName();
                mNowFolderSubject.onNext(nowFolder);
            } catch (IOException e) {
                LogHelper.log(e);
            }
        });
    }

    private ArrayList<BrowseShowFile> fileSorting(ArrayList<BrowseShowFile> browseFiles) {
        if (browseFiles == null || browseFiles.isEmpty()) return browseFiles;
        switch (mFileCollation) {
            case FILTER_NAME_ASCENDING:
                browseFiles.sort((o1, o2) -> o1.getIFile().getName().compareToIgnoreCase(o2.getIFile().getName()));
                break;
            case FILTER_NAME_DESCENDING:
                browseFiles.sort((o1, o2) -> o2.getIFile().getName().compareToIgnoreCase(o1.getIFile().getName()));
                break;
            case FILTER_DATE_ASCENDING:
                browseFiles.sort(Comparator.comparingLong(o -> o.getIFile().lastModified()));
                break;
            case FILTER_DATE_DESCENDING:
                browseFiles.sort((o1, o2) -> Long.compare(o2.getIFile().lastModified(), o1.getIFile().lastModified()));
                break;
            case FILTER_NONE:
            default:
                break;
        }
        return browseFiles;
    }

    private void documentProcessing(ArrayList<BrowseShowFile> browseFiles) {
        for (int i = 0; i < browseFiles.size(); i++) {
            BrowseShowFile browseFile = browseFiles.get(i);
            if (mPrintFileType.contains(browseFile.getFileType())) {
                mReadJpgViewSubject.onNext(browseFile);
            }
        }
    }

    private ArrayList<BrowseShowFile> fileFiltering(ArrayList<IFile> files) {
        ArrayList<BrowseShowFile> files1 = new ArrayList<>();
        for (IFile file : files) {
            BrowseShowFile browseShowFile = null;
            String name = file.getName();
            String suffix = name
                    .substring(name.lastIndexOf(".") + 1)
                    .toLowerCase();
            if (name.startsWith(".")
                    || (name.startsWith("System Volume") && file.isDirectory())
                    || ("LOST.DIR".equals(name) && file.isDirectory())) {
                continue;
            } else if ("gcode".equals(suffix) && mFilterType.contains(FILE_TYPE_GCODE)) {
                browseShowFile = new BrowseShowFile(FILE_TYPE_GCODE, file, mIsJ1 ? R.drawable.pic_file_normal_160x160 : R.drawable.pic_a400_file_error_160x160);
            } else if ("nc".equals(suffix) && mFilterType.contains(FILE_TYPE_NC)) {
                browseShowFile = new BrowseShowFile(FILE_TYPE_NC, file, mIsJ1 ? R.drawable.pic_file_error_160x160 : R.drawable.pic_a400_file_error_160x160);
            } else if ("cnc".equals(suffix) && mFilterType.contains(FILE_TYPE_CNC)) {
                browseShowFile = new BrowseShowFile(FILE_TYPE_CNC, file, mIsJ1 ? R.drawable.pic_file_error_160x160 : R.drawable.pic_a400_file_error_160x160);
            } else if ("bin".equals(suffix) && mFilterType.contains(FILE_TYPE_UPDATE)) {
                browseShowFile = new BrowseShowFile(FILE_TYPE_UPDATE, file, mIsJ1 ? R.drawable.pic_file_bin_160x160 : R.drawable.pic_a400_file_bin);
            } else if ("log".equals(suffix) && mFilterType.contains(FILE_TYPE_LOG)) {
                browseShowFile = new BrowseShowFile(FILE_TYPE_LOG, file, mIsJ1 ? R.drawable.pic_file_error_160x160 : R.drawable.pic_a400_file_error_160x160);
            } else if (file.isDirectory()) {
                browseShowFile = new BrowseShowFile(FILE_TYPE_DIRECTORY, file, mIsJ1 ? R.drawable.pic_folder_normal_160x160 : R.drawable.pic_a400_folder_normal_160x160);
            }

            if (browseShowFile != null) {
                files1.add(browseShowFile);
            }
        }
        return files1;
    }

    public Observable<ArrayList<BrowseShowFile>> getFileListObservable() {
        return mFileListItemsSubject.hide();
    }

    public ArrayList<BrowseShowFile> getFileListValues() {
        return mFileListItemsSubject.getValue();
    }

    public boolean gotoDirectory(int index) {
        try {
            BrowseShowFile browseFile = mFileListItemsSubject.getValue().get(index);
            if (browseFile.getFileType() != FILE_TYPE_DIRECTORY) {
                return false;
            }
            mPartition.gotoDirectory(browseFile.getIFile());
            getFiles();
            return true;
        } catch (Exception e) {
            LogHelper.log(e);
            return false;
        }
    }

    public void addSelectFileList(int position) {
        if (mSelectFileSet == null) {
            mSelectFileSet = new HashSet<>();
        }
        BrowseShowFile browseFile = mFileListItemsSubject.getValue().get(position);
        mSelectFileSet.add(browseFile);
        mSelectFileSetSubject.onNext(mSelectFileSet);
    }

    public void removeSelectFileList(int position) {
        if (mSelectFileSet == null) {
            mSelectFileSet = new HashSet<>();
        }
        BrowseShowFile browseFile = mFileListItemsSubject.getValue().get(position);
        mSelectFileSet.remove(browseFile);
        mSelectFileSetSubject.onNext(mSelectFileSet);
    }

    public void clearSelectFileList() {
        if (mSelectFileSet != null && !mSelectFileSet.isEmpty()) {
            mSelectFileSet.clear();
        }
        mSelectFileSetSubject.onNext(mSelectFileSet);
    }

    public Observable<Set<BrowseShowFile>> getSelectFileObservable() {
        return mSelectFileSetSubject.hide();
    }

    public Set<BrowseShowFile> getSelectFileSet() {
        return mSelectFileSetSubject.getValue();
    }

    public boolean getFileManagerStateValue() {
        return mFileManagerService.getFileManagerStateSubjHolder().getValue();
    }

    public void popDirectory() {
        mPartition.popDirectory();
        getFiles();
    }

    public void updateDirectory() {
        getFiles();
    }

    public String nowFolderValue() {
        return mNowFolderSubject.getValue();
    }

    public boolean isIsSelectMode() {
        return mIsSelectMode;
    }

    public void setIsSelectMode(boolean isSelectMode) {
        mIsSelectMode = isSelectMode;
        if (!mIsSelectMode) {
            clearSelectFileList();
        }
    }

    public Observable<OperationResults> deleteSelectFiles() {
        PublishSubject<OperationResults> mDeleteResultSubject = PublishSubject.create();
        worker.schedule(() -> {
            OperationResults operationResults = new OperationResults();
            if (mSelectFileSet.isEmpty()) {
                operationResults.result = 1;
                mDeleteResultSubject.onNext(operationResults);
                return;
            }
            String name = "";
            StringBuilder deleteName = new StringBuilder("File:");
            try {
                for (BrowseShowFile file : mSelectFileSet) {
                    name = file.getIFile().getName();
                    ThumbnailExtractor.deleteExtractCache(file.getIFile());
                    file.getIFile().removeFile();
                    deleteName.append(name);
                }
                operationResults.result = 0;
                operationResults.message = deleteName.toString();
                mDeleteResultSubject.onNext(operationResults);
            } catch (Exception e) {
                LogHelper.log(e);
                operationResults.result = -1;
                operationResults.message = name;
                mDeleteResultSubject.onNext(operationResults);
            }
        });
        return mDeleteResultSubject;
    }

    long StartTime = 0;
    long EndTime = 0;

    public Observable<OperationResults> copySelectFiles() {
        PublishSubject<OperationResults> mCopyResultSubject = PublishSubject.create();
        worker.schedule(() -> {
            OperationResults operationResults = new OperationResults();
            if (mSelectFileSet.isEmpty()) {
                operationResults.result = 1;
                mCopyResultSubject.onNext(operationResults);
                return;
            }
            String name = "";
            try {
                long SumSize = 0;
                for (BrowseShowFile file : mSelectFileSet) {
                    SumSize += getFileLength(file.getIFile());
                }
                DataProgress dataProgress = new DataProgress();
                dataProgress.setTotalSize(SumSize);
                mDataProgress.onNext(dataProgress);
                Logger.d("Check sum size: " + SumSize);
                int wantMoveTo = 1 - mNowStorage.ordinal();
                // Copy to local, the processing file is too large, and start to delete
                if (wantMoveTo == 0) {
                    IPartition device = mFileManagerService.getDevice(true);
                    if (SumSize >= device.getTotalSpace()) {
//                        Logger.d("---FDT--- delete all start:" + SystemClock.elapsedRealtime());
                        device.removeFile(device.getRootFile());
//                        Logger.d("---FDT--- delete all end:" + SystemClock.elapsedRealtime());
                    } else if (SumSize >= device.getFreeSpace() - 30 * 1024 * 1024) {
//                        Logger.d("---FDT--- delete space to copy start:" + SystemClock.elapsedRealtime());
                        while (SumSize >= device.getFreeSpace() - 30 * 1024 * 1024) {
                            ArrayList<IFile> iFiles = device.getRootFile().listFiles();
                            iFiles.sort(Comparator.comparingLong(IFile::lastModified));
                            iFiles.get(0).removeFile();
                        }
                        Logger.d("Free up space done, available " + device.getFreeSpace());
//                        Logger.d("---FDT--- delete space to copy end:" + SystemClock.elapsedRealtime());
                    }
                }
                StartTime = SystemClock.elapsedRealtime();
                Logger.d("Start copying files, start time " + StartTime);
                StringBuilder deleteName = new StringBuilder("File:");
                for (BrowseShowFile file : mSelectFileSet) {
                    name = file.getIFile().getName();
                    IPartition device = mFileManagerService.getDevice(wantMoveTo == 0);
                    while (!device.isRoot()) {
                        Logger.d("Current was not in the root path, popping...");
                        device.popDirectory();
                    }
                    copyToFilePath(file.getIFile(), device);
                    deleteName.append(name);
                }
                operationResults.result = 0;
                operationResults.message = deleteName.toString();
                mCopyResultSubject.onNext(operationResults);
            } catch (Exception e) {
                LogHelper.log(e);
                operationResults.result = -1;
                operationResults.message = name;
                mCopyResultSubject.onNext(operationResults);
            }
        });
        return mCopyResultSubject;
    }

    private long getFileLength(IFile iFile) throws Exception {
        long SumSize = 0;
        if (iFile.isDirectory()) {
            for (IFile flie : iFile.listFiles()) {
                SumSize += getFileLength(flie);
            }
        } else {
            return iFile.length();
        }
        return SumSize;
    }

    public Observable<OperationResults> checkAvailableSpace(BrowseShowFile file) {
        OperationResults operationResults = new OperationResults();
        // Skip directory file type because iFile length with directory return 0(means not supported)
        if (file.getFileType() == FILE_TYPE_DIRECTORY) {
            operationResults.result = 1;
            operationResults.message = "Target file is a directory.";
            return Observable.just(operationResults);
        }

        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        // Bytes
        long estimatedAvailableSpace = stat.getAvailableBytes() - file.getIFile().length();
        if (estimatedAvailableSpace < 0) {
            // Lack of available space for file
            operationResults.result = 2;
            operationResults.message = ServiceContainer.getInstance().getService(IAppService.class).getAppContext().getString(R.string.j1_file_detail_dialog_insufficient_system_storage_desc);
        } else if (estimatedAvailableSpace < 300 * 1024 * 1024) {
            // Available space is less than 300 MB
            operationResults.result = 3;
            operationResults.message = ServiceContainer.getInstance().getService(IAppService.class).getAppContext().getString(R.string.j1_file_detail_dialog_low_system_storage_desc);
        } else {
            operationResults.result = 0;
        }
        return Observable.just(operationResults);
    }

    public void freeUpStorageSpace(BrowseShowFile file) {
        long expectLength = file.getIFile().length();
        IFileManagerService fileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        IPartition device = fileManagerService.getDevice(true);
        try {
            if (expectLength >= device.getTotalSpace()) {
                device.removeFile(device.getRootFile());
            } else if (expectLength >= device.getFreeSpace() - 30 * 1024 * 1024) {
                while (expectLength >= device.getFreeSpace() - 30 * 1024 * 1024) {
                    ArrayList<IFile> iFiles = device.getRootFile().listFiles();
                    iFiles.sort(Comparator.comparingLong(IFile::lastModified));
                    iFiles.get(0).removeFile();
                }
                Logger.d("Free up space done, available " + device.getFreeSpace());
            }

        } catch (IOException e) {
            Logger.e("Clear local file error!");
            LogHelper.log(e);
        }
    }


    private void copyToFilePath(IFile iFile, IPartition iPartition) throws Exception {
        if (iFile.isDirectory()) {
            IFile newFile = iPartition.search(iPartition.getCurrentDirectory().getAbsolutePath() + iFile.getName());
            if (newFile != null) {
                iPartition.gotoDirectory(newFile);
            } else {
                IFile file = iPartition.createDirectory(iPartition.getCurrentDirectory(), iFile.getName());
                file.setLastModified(iFile.lastModified());
                iPartition.gotoDirectory(file);
            }
            for (IFile f : iFile.listFiles()) {
                copyToFilePath(f, iPartition);
            }
            iPartition.popDirectory();
        } else {
            copyToOtherStorage(iFile, iPartition);
        }
    }

    public Observable<DataProgress> getDataProgressObservable() {
        return mDataProgress.hide();
    }

    private void copyToOtherStorage(IFile iFile, IPartition iPartition) throws Exception {
        OutputStream outputStream;
        FabFileInputStream fabFileInputStream = null;
        InputStream inputStream = null;
        IFile newFile = null;
        try {
            IFile rootFile = iPartition.getCurrentDirectory();
            newFile = iPartition.search(rootFile.getAbsolutePath() + iFile.getName());
            if (newFile != null) {
                newFile.removeFile();
            }
            newFile = iPartition.createFile(rootFile, iFile.getName());
            outputStream = new FabFileOutputStream(newFile.getOutputStream().getOutputStream()).getOutputStream();
            fabFileInputStream = iFile.getInputStream();
            inputStream = fabFileInputStream.getInputStream();
            byte[] buf = new byte[20480];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
                DataProgress value = mDataProgress.getValue();
                value.setCurrentSize(value.getCurrentSize() + len);
                mDataProgress.onNext(value);
                if (value.getCurrentSize() == value.getTotalSize()) {
                    EndTime = SystemClock.elapsedRealtime();
                    long l = EndTime - StartTime;
                    Logger.d("EndTime:" + EndTime +
                            "\tTotalTimeUsed:" + l +
                            "\tSpeed:" + Math.round(value.getTotalSize() / 1024.0 / (l / 1000.0)) + " kb/s"
                    );
                }
            }
            newFile.setLastModified(iFile.lastModified());
        } catch (Exception e) {
            try {
                if (newFile != null) {
                    newFile.removeFile();
                }
            } catch (Exception e1) {

            }
            throw e;
        } finally {
            if (fabFileInputStream != null) {
                try {
                    fabFileInputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (fabFileInputStream != null) {
                try {
                    fabFileInputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public boolean isErrorUsb() {
        return ((mNowStorage == PARTITION_USB) && mPartition == null);
    }

    public Observable<Boolean> getUSBStateObservable() {
        return mFileManagerService.getFileManagerStateSubjHolder().getObservable();
    }

    public Boolean getUSBStateValue() {
        return mFileManagerService.getFileManagerStateSubjHolder().getValue();
    }

    public enum StorageMedium {
        /**
         * Use Storage Medium
         */
        PARTITION_LOCAL,
        PARTITION_USB,
        PARTITION_LUBAN,
        PARTITION_CLOUD
    }

    public enum FileCollation {
        FILTER_NONE,
        FILTER_NAME_ASCENDING,
        FILTER_NAME_DESCENDING,
        FILTER_DATE_ASCENDING,
        FILTER_DATE_DESCENDING,
        FILTER_SIZE_ASCENDING,
        FILTER_SIZE_DESCENDING
    }

    public class OperationResults {
        public int result;
        public String message;
    }

    public class DataProgress {
        private long mTotalSize;
        private long mCurrentSize;

        public long getTotalSize() {
            return mTotalSize;
        }

        public void setTotalSize(long totalSize) {
            mTotalSize = totalSize;
        }

        public long getCurrentSize() {
            return mCurrentSize;
        }

        public void setCurrentSize(long currentSize) {
            mCurrentSize = currentSize;
        }
    }
}
