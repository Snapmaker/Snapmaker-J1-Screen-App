package fabscreen.platform.base.lib.print;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileInputStream;
import fabscreen.platform.base.lib.file.FabFileOutputStream;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.FabUsbFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class BasePrintWorkspace implements IPrintWorkspace, IServiceIdentifier {

    private IPreferences mPreferences;

    //    private String mWorkspaceDirPath;
    private String mFilesDirPath;
    private IFile mSourceFile;
    private IFile mPrintFile = null;
    private String mPrintFileMD5Value;

    private Scheduler.Worker mCopyFileWorker;
    private BehaviorSubject<Boolean> mCopyResultSubject = BehaviorSubject.create();
    private int mPrintMode = PRINT_MODE_NORMAL;
    private boolean mApplyMultiExtruder;

    private float[] mExtruderTargetTemperature;
    private float mPrintModeXOffset = 0f;

    private ModelBoundary mModelBoundary;

    @Keep
    public BasePrintWorkspace(IPreferences preferences) {
        mPreferences = preferences;
        // Should not place in cache directory.
        Context context = ServiceContainer.getInstance().getService(IAppService.class).getAppContext();

//        mWorkspaceDirPath = context.getFilesDir() + "/.workspace";
        mFilesDirPath = context.getFilesDir().getAbsolutePath();
    }

    public void initLastPrintFile() {
        String printFilePath = mPreferences.getHelper().getPrintFilePath();

        if (TextUtils.isEmpty(printFilePath)) {
            mPrintFile = null;
        } else {
            mPrintFile = new FabLocalFile(new File(printFilePath));
        }

        int lastPrintMode = mPreferences.getHelper().getPrintSelectedMode();
        float lastPrintOffsetX = mPreferences.getHelper().getPrintOffsetX();
        mPrintMode = lastPrintMode;
        mPrintModeXOffset = lastPrintOffsetX;

    }

    public IFile getPrintFile() {
        return mPrintFile;
    }

    @Override
    public void setPrintFile(IFile file) {
        mPrintFile = file;
    }


    public String getFileMD5Value() {
        return mPrintFileMD5Value;
    }

    public void setFileMD5Value(String value) {
        mPrintFileMD5Value = value;
    }

    public int getPrintMode() {
        return mPrintMode;
    }

    @Override
    public void setPrintMode(int printMode) {
        mPrintMode = printMode;
        mPreferences.getHelper().setPrintSelectedMode(mPrintMode);
    }

    public float getEstimatedTime() {
        return mPreferences.getHelper().getPrintFileEstimatedTime();
    }

    public void setEstimatedTime(float estimatedTime) {
        mPreferences.getHelper().setPrintFileEstimatedTime(estimatedTime);
    }

    public int getFileTotalLineCount() {
        return mPreferences.getHelper().getPrintFileTotalLines();
    }

    public void setFileTotalLineCount(int totalCount) {
        mPreferences.getHelper().setPrintFileTotalLines(totalCount);
    }

    public int getPrintSource() {
        return mPreferences.getHelper().getPrintSource();
    }

    public void setPrintSource(int source) {
        mPreferences.getHelper().setPrintSource(source);
    }

    public String getFileName() {
        if (mPrintFile == null) return "";
        return mPrintFile.getName();
    }

    public Observable<Boolean> addFileToWorkspace(IFile sourceFile) {
        // Reset
        if (mCopyFileWorker != null) {
            mCopyFileWorker.dispose();
        }
        if (mCopyResultSubject != null) {
            mCopyResultSubject = BehaviorSubject.create();
        }
//        clearWorkspaceFile();
        mSourceFile = sourceFile;

        Logger.d("Start copy file into workspace…");

        // Create worker for copy file into workspace
        mCopyFileWorker = Schedulers.io().createWorker();
        mCopyFileWorker.schedule(this::startCopyFile);

        return mCopyResultSubject.hide();
    }

    private void clearWorkspaceFile() {
        File workspaceDir = getWorkspaceDir();
        File[] files = workspaceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                boolean ret = file.delete();
            }
        }
    }

    private void startCopyFile() {
        if (mSourceFile instanceof FabUsbFile) {
            IPartition device = ServiceContainer.getInstance().getService(IFileManagerService.class).getDevice(true);
            if (mSourceFile.length() >= device.getTotalSpace()) {
                Logger.w("Target file is too large.\nFile Size:" + mSourceFile.length() + "\nDevice TotalSpace:" + device.getTotalSpace());
                mCopyResultSubject.onNext(false);
                return;
            } else if (mSourceFile.length() >= device.getFreeSpace()) {
                Logger.d("Try deleting files to get space: " + SystemClock.elapsedRealtime());
                try {
                    while (mSourceFile.length() >= device.getFreeSpace()) {
                        ArrayList<IFile> iFiles = device.getRootFile().listFiles();
                        iFiles.sort(Comparator.comparingLong(IFile::lastModified));
                        device.removeFile(iFiles.get(0));
                    }
                } catch (Exception e) {
                    LogHelper.log(e);
                    mCopyResultSubject.onNext(false);
                    return;
                }
                Logger.d("Files deleted, recording: " + SystemClock.elapsedRealtime());
            }
            mPrintFile = new FabLocalFile(new File(getWorkspaceDir(), mSourceFile.getName()));
            mPreferences.getHelper().setPrintFilePath(mPrintFile.getPath());
            BufferedSource bufferedSource = null;
            BufferedSink bufferedSink = null;
            FabFileInputStream fabFileInputStream = null;
            FabFileOutputStream fabFileOutputStream = null;
            try {
                fabFileInputStream = mSourceFile.getInputStream();
                fabFileOutputStream = mPrintFile.getOutputStream();
                bufferedSource = Okio.buffer(Okio.source(fabFileInputStream.getInputStream()));
                bufferedSink = Okio.buffer(Okio.sink(fabFileOutputStream.getOutputStream()));
                // copy file from source with buffer
                int len;
                byte[] buffer = new byte[1024 * 16];
                while ((len = bufferedSource.read(buffer)) != -1) {
                    bufferedSink.write(buffer, 0, len);
                }
                bufferedSink.close();
                bufferedSource.close();
                fabFileInputStream.close();
                fabFileOutputStream.close();
                Logger.d("Copy file into workspace completed.");
                mCopyResultSubject.onNext(true);
            } catch (Exception e) {
                mCopyResultSubject.onNext(false);
                LogHelper.log(e);
            } finally {
                // TODO: close the device when  input stream destroyed
//            iPartition.deviceHang();
                try {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }
                    if (bufferedSource != null) {
                        bufferedSource.close();
                    }
                    if (fabFileInputStream != null) {
                        fabFileInputStream.close();
                    }
                    if (fabFileOutputStream != null) {
                        fabFileOutputStream.close();
                    }
                } catch (IOException e) {
                    LogHelper.log(e);
                }
            }
        } else if (mSourceFile instanceof FabLocalFile) {
            mPrintFile = mSourceFile;
            mPreferences.getHelper().setPrintFilePath(mPrintFile.getPath());
            mCopyResultSubject.onNext(true);
        } else {
            mCopyResultSubject.onNext(false);
        }
    }

    public File getWorkspaceDir() {
        File fileDir = new File(mFilesDirPath);
        if (!fileDir.exists()) {
            boolean ret = fileDir.mkdir();
            if (!ret) {
                return null;
            }
        }
        return fileDir;
    }

    @Override
    public void setModelBoundary(ModelBoundary boundary) {
        mModelBoundary = boundary;
    }

    public ModelBoundary getModelBoundary() {
        return mModelBoundary;
    }

    @Override
    public float getWorkTemperature(int index) {
        return mExtruderTargetTemperature[index];
    }

    @Override
    public void setWorkTemperature(float[] extruderTargetTemperature) {
        mExtruderTargetTemperature = extruderTargetTemperature;
    }

    @Override
    public void setPrintModeXOffset(float xOffset) {
        Logger.d("set PrintModeXOffset " + xOffset);
        mPrintModeXOffset = xOffset;
        mPreferences.getHelper().setPrintOffsetX(mPrintModeXOffset);
    }

    @Override
    public float getPrintModeXOffset() {
        return mPrintModeXOffset;
    }

    public void dispose() {
        if (mCopyFileWorker != null && !mCopyFileWorker.isDisposed()) {
            mCopyFileWorker.dispose();
        }
    }

    public void clearAllWorkSpaceFiles() {
        File fileDir = new File(mFilesDirPath);
        if (fileDir.exists()) {
            clearWorkspaceFile();
            fileDir.delete();
        }
    }

    @Override
    public boolean isApplyMultiExtruder() {
        return mApplyMultiExtruder;
    }

    @Override
    public void setApplyMultiExtruder(boolean applyMultiExtruder) {
        mApplyMultiExtruder = applyMultiExtruder;
    }

}
