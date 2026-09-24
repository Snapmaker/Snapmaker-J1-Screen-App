package fabscreen.platform.base.lib.print;

import android.os.SystemClock;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileInputStream;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IFileManagerService;
import okio.BufferedSource;
import okio.Okio;

//import fabscreen.libraries.core.BaseApplication;

/**
 * G-code Player.
 * <p>
 * State Machine:
 * IDLE -> PLAYING <-> PAUSED
 * ^                     |
 * |_____________________|
 */
public class GcodePlayer {
    private static final String TAG = "GcodePlayer";

    private IFile mFile;
    private InputStream mInputStream;
    private BufferedSource source;
    private FabFileInputStream inputStream;

    private String line;

    private int sentCount;
    private int receivedCount;
    private int totalCount;

    private IPartition mIPartition;

    public GcodePlayer() {
        reset();
    }

    public void reset() {
        totalCount = sentCount = receivedCount = 0;
        line = null;

        if (source != null) {
            try {
//                mIPartition.deviceHang();
                source.close();
            } catch (IOException e) { /* ignore */ }
            source = null;
        }
        if (inputStream != null) {
            try {
//                mIPartition.deviceHang();
                inputStream.close();
            } catch (IOException e) { /* ignore */ }
            inputStream = null;
        }

    }

    public void setGcodeFile(IFile file) {
        mFile = file;
    }

    public void setInputStream(InputStream inputStream) {
        mInputStream = inputStream;
    }

    private void openFile() {
        if (source != null) {
            try {
//                mIPartition.deviceHang();
                source.close();
            } catch (IOException e) { /* ignore */ }
            source = null;
        }

        try {
            if (mFile != null) {
                mIPartition = ServiceContainer.getInstance().getService(IFileManagerService.class).getDevice(mFile.isLocal());
                inputStream = mFile.getInputStream();
                source = Okio.buffer(Okio.source(inputStream.getInputStream()));
            } else {
                source = Okio.buffer(Okio.source(mInputStream));
            }

        } catch (Exception e) {
            Log.e(TAG, "File does not exists.");
            e.printStackTrace();
        }
    }

    public void setLineno(int startLineno) {
        String line;
        int pos;

        // rewind
        // setGcodeFile(mFile);
        openFile();

        sentCount = receivedCount = startLineno;
        pos = 0;

        // run to start point
        while (pos < startLineno) {
            try {
                line = source.readUtf8Line();
            } catch (IOException e) {
                e.printStackTrace();
                // reset();
                return;
            }

            if (line == null) {
                break;
            }

            pos++;
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Get next line.
     */
    public String nextLine() {
        try {
            line = source.readUtf8Line();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        sentCount++;
        if (sentCount % 1000 == 0) {
            Logger.i(String.format("line %s: %s", sentCount, line));
        }
        return line;
    }

    /**
     * Get current line.
     */
    public String getLine() {
        return line;
    }

    /**
     * Get current line number (start from 0).
     *
     * @return int the line number
     */
    public int getLineNo() {
        return sentCount - 1;
    }

    public void onAck() {
        receivedCount++;
    }

    public void skipLine() {
        receivedCount++;
    }

    public float getProgress() {
        if (totalCount == 0) return 0;
        int nowCount = getProgressCount();
        return 1.f * nowCount / totalCount;
    }

    public int getSentCount() {
        return sentCount;
    }

    public int getReceivedCount() {
        return receivedCount;
    }

    public int getProgressCount() {
        // In batch mode, receivedCount will not be modified
        // in single mode, sentCount may be greater than receivedCount
        // GcodePlayer takes the larger number and does not need to determine the mode
        return Math.max(receivedCount, sentCount - 1);
    }

    public boolean gcodeIsEmpty() {
        return mFile == null;
    }
}
