package fabscreen.platform.base.lib;

public interface DownloadProgressListener {
    void progress(long read, long contentLength, boolean isDone);
}
