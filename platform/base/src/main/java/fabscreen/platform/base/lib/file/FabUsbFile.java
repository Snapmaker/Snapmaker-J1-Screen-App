package fabscreen.platform.base.lib.file;

import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FabUsbFile implements IFile {
    private static final String TAG = "FabUsbFile";
    private static final String ILLEGAL_CHAR_REGEX = "[/|\\\\:*\"<>?]";
    private UsbFile mFile;
    private FabFileStreamListener mFabFileStreamListener;

    public FabUsbFile(UsbFile file, FabFileStreamListener fabFileStreamListener) {
        mFile = file;
        mFabFileStreamListener = fabFileStreamListener;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public long length() {
        if (mFile.isDirectory()) {
            return 0; // not supported
        } else {
            return mFile.getLength();
        }
    }

    @Override
    public boolean exists() {
        return mFile != null;
    }

    @Override
    public FabFileInputStream getInputStream() throws IOException {
        if (mFile.isDirectory()) {
            throw new IOException("Could not get InputStream, " + this.getClass().getName() + " isn't a file.");
        } else {
            return new FabFileInputStream(new UsbFileInputStream(mFile), () -> mFabFileStreamListener.close());
        }
    }

    @Override
    public FabFileOutputStream getOutputStream() throws IOException {
        if (mFile.isDirectory()) {
            throw new IOException("Could not get OutputStream, " + this.getClass().getName() + " isn't a file.");
        } else {
            return new FabFileOutputStream(new UsbFileOutputStream(mFile), () -> mFabFileStreamListener.close());
        }
    }

    @Override
    public ArrayList<IFile> listFiles() throws IOException {
        // List files on current directory
        ArrayList<IFile> files = new ArrayList<IFile>();
        try {
            for (UsbFile usbFile1 : mFile.listFiles()) {
                files.add(new FabUsbFile(usbFile1, () -> mFabFileStreamListener.close()));
            }
        } catch (IOException e) {
            Logger.d("listFiles error! \n" + e);
            // deviceHang();
            throw new IOException("Failed to list file on current directory.");
        }
        return files;
    }

    @Override
    public void removeFile() throws IOException {
        if (mFile == null) {
            throw new IOException("file could not be found.");
        }
        mFile.delete();
    }

    @Override
    public void renameFile(String name) throws IOException {
        if (name.isEmpty() || name.length() > 255) {
            throw new IOException("Filename too long.");
        }

        // Check if there is any illegal characters exists
        final Pattern illegalCharacters = Pattern.compile(ILLEGAL_CHAR_REGEX);
        Matcher matcher = illegalCharacters.matcher(name);
        if (matcher.find()) {
            throw new IOException("String name contains illegal character!");
        }
        mFile.setName(name);
    }

    @Override
    public IFile createFile(String name) throws IOException {
        UsbFile usbFile1 = mFile.createFile(name);
        return new FabUsbFile(usbFile1, () -> mFabFileStreamListener.close());
    }

    @Override
    public IFile createDirectory(String name) throws IOException {
        UsbFile usbFile1 = mFile.createDirectory(name);
        return new FabUsbFile(usbFile1, () -> mFabFileStreamListener.close());
    }

    @Override
    public String getAbsolutePath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public String getPath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public long lastModified() {
        return mFile.lastModified();
    }

    @Override
    public boolean setLastModified(long timeInMillis) {
        return false;
    }

    public interface FabFileStreamListener {
        void close();
    }
}
