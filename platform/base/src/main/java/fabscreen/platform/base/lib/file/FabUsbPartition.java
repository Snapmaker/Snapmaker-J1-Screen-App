package fabscreen.platform.base.lib.file;

import android.content.Context;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.partition.Partition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fabscreen.platform.base.FabException;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;


public class FabUsbPartition extends AbstractPartition {
    private final static String TAG = "FabUsbFileManager";
    private static final String ILLEGAL_CHAR_REGEX = "[/|\\\\:*\"<>?]";
    private Context mContext;


    private FabUsbMassStorageDevice mUsbDevice;
    private Partition mPartition;

    private boolean mDevicesHang = false;

    // Partition only created when device permission granted
    public FabUsbPartition(Context context, FabUsbMassStorageDevice usbDevice, Partition partition) {
        mContext = context;
        mUsbDevice = usbDevice;
        mPartition = partition;
        init();
    }

    @Override
    public IFile getRootFile() {
        return new FabUsbFile(getFileSystem().getRootDirectory(), () -> mUsbDevice.close());
    }

    @Override
    public long getUsedSpace() {
        return getFileSystem().getOccupiedSpace();
    }

    @Override
    public long getFreeSpace() {
        return getFileSystem().getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return getFileSystem().getCapacity();
    }

    @Override
    public IFile search(String path) {
        if ("/".equals(path)) {
            return getRootFile();
        }

        if (!path.startsWith("/")) {
            LogHelper.log(new IllegalArgumentException("Only absolute path is supported."));
            return null;
        }

        try {
            int i = path.lastIndexOf(File.separatorChar);
            String pathName = path;
            if (i != -1) pathName = pathName.substring(i + 1);
            ArrayList<IFile> files = getCurrentDirectory().listFiles();
            for (IFile file : files) {
                if (pathName.equals(file.getName())) {
                    return file;
                }
            }
//            mUsbDevice.restart();
//            UsbFile usbFile = getFileSystem().getRootDirectory().search(path.substring(1));
//            Logger.d("---FDT--- search usbFile " + (usbFile == null));
            // TODO: 2022/6/29 Need to be woken up
//            return search(getRootFile(), path);
            return null;
        } catch (IOException e) {
            LogHelper.log(e);
            return null;
        }
    }

    private IFile search(IFile nowFile, String path) throws IOException {
        if (path.isEmpty()) return nowFile;
        ArrayList<IFile> files = nowFile.listFiles();
        int startIndex = path.indexOf(File.separatorChar);
        int endIndex = path.indexOf(File.separatorChar, startIndex + 1);
        String pathName = path;
        if (endIndex != -1) {
            pathName = path.substring(startIndex + 1, endIndex);
        } else if (startIndex != -1) {
            pathName = path.substring(startIndex + 1);
        }
        for (IFile file : files) {
            if (pathName.equals(file.getName())) {
                if (endIndex == -1) {
                    return file;
                }
                return search(file, path.substring(endIndex));
            }
        }
        return null;
    }

    public Observable<IFile> asyncSearch(String path) {
        BehaviorSubject<IFile> subject = BehaviorSubject.create();
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            if ("/".equals(path)) {
                subject.onNext(getRootFile());
            }

            if (!path.startsWith("/")) {
                subject.onError(new FabException("Only absolute path is supported."));
            }

            try {
                UsbFile usbFile = getFileSystem().getRootDirectory().search(path.substring(1));
                subject.onNext(new FabUsbFile(usbFile, () -> mUsbDevice.close()));
            } catch (IOException e) {
                subject.onError(new FabException("Unable to open file."));
            }
        });
        return subject.hide();
    }

    /**
     * If the init is unsuccessful, the resulting mStack may be 0
     *
     * @return
     */
    @Override
    public boolean isRoot() {
        return mStack.size() == 1;
    }

    @Override
    public IFile getCurrentDirectory() {
        return mStack.isEmpty() ? getRootFile() : mStack.peek();
    }

    @Override
    public void gotoDirectory(IFile file) {
        mStack.push(file);
    }

    @Override
    public void popDirectory() {
        if (mStack.size() > 1)
            mStack.pop();
    }

    @Override
    public IFile createFile(IFile file, String name) throws IOException {
        return file.createFile(name);
    }

    @Override
    public IFile createDirectory(IFile file, String name) throws IOException {
        return file.createDirectory(name);
    }

    @Override
    public void removeFile(IFile file) throws IOException {
        UsbFile mFile = getFileSystem().getRootDirectory().search(file.getPath().substring(1));
        if (mFile == null) {
            throw new IOException("file could not be found.");
        }
        mFile.delete();
    }

    @Override
    public void renameFile(IFile file, String name) throws IOException {
        if (name.isEmpty() || name.length() > 255) {
            throw new IOException("Filename too long.");
        }

        // Check if there is any illegal characters exists
        final Pattern illegalCharacters = Pattern.compile(ILLEGAL_CHAR_REGEX);
        Matcher matcher = illegalCharacters.matcher(name);
        if (matcher.find()) {
            throw new IOException("String name contains illegal character!");
        }

        UsbFile usbFile = getFileSystem().getRootDirectory().search(file.getPath().substring(1));
        if (usbFile == null || usbFile.getParent() == null) {
            return;
        }
        usbFile.setName(name);
    }

    private FileSystem getFileSystem() {
        return mUsbDevice.getFileSystem(mPartition);
    }

    private ArrayList<UsbFile> readDevice(UsbMassStorageDevice device) {
        ArrayList<UsbFile> usbFiles = new ArrayList<>();
        UsbFile root = getFileSystem().getRootDirectory();

        try {
            Collections.addAll(usbFiles, root.listFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usbFiles;
    }

    public void close() {
        mUsbDevice.close();
    }
}
