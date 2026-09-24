package fabscreen.platform.base.legacy;

import android.content.Context;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

import fabscreen.platform.base.lib.file.FabFileInputStream;
import fabscreen.platform.base.lib.file.FabFileOutputStream;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.service.Preferences;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

@Deprecated
public class Workspace {
    private Context mContext;
    private Preferences mPreferences;

    private String mWorkspaceDirPath;
    private IFile mSourceFile;
    private IFile mPrintFile = null;

    private Scheduler.Worker mCopyFileWorker;
    private BehaviorSubject<Boolean> mCopyResultSubject = BehaviorSubject.createDefault(false);

    public Workspace(Context context, Preferences preferences) {
        mContext = context;
        mPreferences = preferences;
        mWorkspaceDirPath = context.getCacheDir() + "/workspace";
    }

    public void initLastPrintFile() {
        String printFilePath = mPreferences.getHelper().getPrintFilePath();
        if (TextUtils.isEmpty(printFilePath)) {
            mPrintFile = null;
        } else {
            mPrintFile = new FabLocalFile(new File(printFilePath));
        }

    }

    public IFile getPrintFile() {
        return mPrintFile;
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
        return mPrintFile.getName();
    }

    public Observable<Boolean> addFileToWorkspace(IFile sourceFile) {
        // Reset
        if (mCopyFileWorker != null) {
            mCopyFileWorker.dispose();
        }
        if (mCopyResultSubject != null) {
            mCopyResultSubject = BehaviorSubject.createDefault(false);
        }
        clearWorkspaceFile();
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
        // Create new print file
        mPrintFile = new FabLocalFile(new File(getWorkspaceDir().getPath(), mSourceFile.getName()));
        mPreferences.getHelper().setPrintFilePath(mPrintFile.getPath());

        BufferedSource bufferedSource = null;
        BufferedSink bufferedSink = null;
        FabFileInputStream inputStream = null;
        FabFileOutputStream outputStream = null;
        try {
            inputStream = mSourceFile.getInputStream();
            outputStream = mPrintFile.getOutputStream();
            bufferedSource = Okio.buffer(Okio.source(inputStream.getInputStream()));
            bufferedSink = Okio.buffer(Okio.sink(outputStream.getOutputStream()));
            // copy file from source with buffer
            int len;
            byte[] buffer = new byte[1024 * 16];
            while ((len = bufferedSource.read(buffer)) != -1) {
                bufferedSink.write(buffer, 0, len);
            }
            bufferedSink.close();
            bufferedSource.close();
            inputStream.close();
            outputStream.close();
            Logger.d("Copy file into workspace completed.");
            mCopyResultSubject.onNext(true);
        } catch (IOException e) {
            mCopyResultSubject.onError(e);
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
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                LogHelper.log(e);
            }
        }
    }

    public File getWorkspaceDir() {
        File fileDir = new File(mWorkspaceDirPath);
        if (!fileDir.exists()) {
            boolean ret = fileDir.mkdir();
            if (!ret) {
                return null;
            }
        }
        return fileDir;
    }

    public void dispose() {
        if (mCopyFileWorker != null && !mCopyFileWorker.isDisposed()) {
            mCopyFileWorker.dispose();
        }
    }
}
