package fabscreen.platform.base.lib.file;

import java.io.IOException;
import java.util.ArrayList;

public interface IFile {
    /**
     * Return the actual file or directory name or '/' for root directory.
     *
     * @return The name of the file or directory.
     */
    String getName();

    /**
     * @return True if representing a directory.
     */
    boolean isDirectory();

    /**
     * @return True if file is not in external storage.
     */
    boolean isLocal();

    /**
     * Return the file length.
     *
     * @return File length in bytes.
     */
    long length();

    /**
     * @return Return the file path according to where the file is.
     */
    String getPath();

    /**
     * Return the time this directory or file was last modified.
     *
     * @return Time in milliseconds since January 1 00:00:00, 1970 UTC
     */
    long lastModified();

    boolean setLastModified(long timeInMillis);

    /**
     * @return True if file exists.
     */
    boolean exists();

    FabFileInputStream getInputStream() throws IOException;

    FabFileOutputStream getOutputStream() throws IOException;

    ArrayList<IFile> listFiles() throws IOException;

    void removeFile() throws IOException;

    void renameFile(String name) throws IOException;

    IFile createFile(String name) throws IOException;

    IFile createDirectory(String name) throws IOException;

    String getAbsolutePath();
}
