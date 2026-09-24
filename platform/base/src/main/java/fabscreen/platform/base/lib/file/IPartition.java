package fabscreen.platform.base.lib.file;

import java.io.IOException;

public interface IPartition {

    Boolean init();

    IFile getRootFile();

    long getUsedSpace();

    long getFreeSpace();

    long getTotalSpace();

    IFile search(String path);

    boolean isRoot();

    IFile getCurrentDirectory();

    void gotoDirectory(IFile file);

    void popDirectory();

    IFile createFile(IFile file, String name) throws IOException;

    IFile createDirectory(IFile file, String name) throws IOException;
//    Observable<ArrayList<IFile>> listFiles();

    /**
     * Remove one file from FileSystem.
     *
     * @param file remove file
     * @throws IOException
     */
    void removeFile(IFile file) throws IOException;

    /**
     * Rename file.
     */
    void renameFile(IFile file, String name) throws IOException;

}
