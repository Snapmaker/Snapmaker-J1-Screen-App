package fabscreen.platform.base.lib.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import fabscreen.platform.lib.LogHelper;

public class FabLocalPartition extends AbstractPartition {
    private final static String TAG = "FabUsbFileManager";
    private static final String ILLEGAL_CHAR_REGEX = "[/|\\\\:*\"<>?]";
    private String rootPath;
    private Stack<IFile> mStack = new Stack<>();

    public FabLocalPartition(String rootPath) {
        this.rootPath = rootPath;
        init();
    }

    @Override
    public Boolean init() {
        mStack.clear();
        IFile rootFile = getRootFile();
        mStack.add(rootFile);
        return true;
    }

    @Override
    public IFile getRootFile() {
        File file = new File(rootPath);
        return new FabLocalFile(file);
    }

    /**
     * partition size, in bytes.
     */
    @Override
    public long getUsedSpace() {
        // TODO: dynamically change according to file changes (delete)?
        File rootDir = new File(rootPath);
        return rootDir.getTotalSpace() - rootDir.getFreeSpace();
    }

    @Override
    public long getFreeSpace() {
        File rootDir = new File(rootPath);
        return rootDir.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        File rootDir = new File(rootPath);
        return rootDir.getTotalSpace();
    }

    @Override
    public IFile search(String path) {

        if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
        }

        if ("/".equals(path)) {
            return getRootFile();
        }
        try {
            if (!path.startsWith("/")) {
                ArrayList<IFile> files = getCurrentDirectory().listFiles();
                for (IFile file : files) {
                    if (path.equals(file.getName())) {
                        return file;
                    }
                }
                return null;
            } else {
                return search(getRootFile(), path);
            }
        } catch (Exception e) {
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
        file.removeFile();
    }

    @Override
    public void renameFile(IFile file, String name) throws IOException {
        file.renameFile(name);
    }

}
