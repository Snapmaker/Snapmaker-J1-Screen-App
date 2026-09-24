package fabscreen.platform.base.lib.file;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fabscreen.platform.base.helper.FileHelper;

public class FabLocalFile implements IFile {
    private static final String TAG = "FabLocalFile";
    private static final String ILLEGAL_CHAR_REGEX = "[/|\\\\:*\"<>?]";

    private File mFile;

    public FabLocalFile(File file) {
        mFile = file;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public long length() {
        if (mFile.isDirectory()) {
            // not supported
            return 0;
        } else {
            return mFile.length();
        }
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public FabFileInputStream getInputStream() throws IOException {
        return new FabFileInputStream(new FileInputStream(mFile));
    }

    @Override
    public FabFileOutputStream getOutputStream() throws FileNotFoundException {
        return new FabFileOutputStream(new FileOutputStream(mFile));
    }

    @Override
    public ArrayList<IFile> listFiles() throws IOException {
        ArrayList<IFile> fileList = new ArrayList<>();
        File[] files = mFile.listFiles();
        for (File file : files) {
            fileList.add(new FabLocalFile(file));
        }
        return fileList;
    }

    @Override
    public void removeFile() throws IOException {
        FileHelper.removeFile(mFile);
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
            Log.d(TAG, "String name contains illegal character!");
            throw new IOException("");
        }
        String newPath = mFile.getParent().concat("/" + name);
        File dest = new File(newPath);
        // duplicate
        if (dest.exists()) {
            // File already exists.
            throw new IOException("File with the given name already exists!");
        }
        // rename file
        if (!mFile.renameTo(dest)) {
            throw new IOException("Failed to rename file.");
        }
    }

    @Override
    public IFile createFile(String name) throws IOException {
        File file = new File(mFile, name);
        return new FabLocalFile(file);
    }

    @Override
    public IFile createDirectory(String name) throws IOException {
        File file = new File(mFile, name);
        file.mkdirs();
        return new FabLocalFile(file);
    }

    @Override
    public String getAbsolutePath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public String getPath() {
        return mFile.getPath();
    }

    @Override
    public long lastModified() {
        return mFile.lastModified();
    }

    @Override
    public boolean setLastModified(long timeInMillis) {
        return mFile.setLastModified(timeInMillis);
    }
}
