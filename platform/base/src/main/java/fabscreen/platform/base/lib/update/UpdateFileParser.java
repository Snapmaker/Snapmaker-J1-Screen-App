package fabscreen.platform.base.lib.update;


import android.content.Context;

import androidx.annotation.IntDef;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.VersionResponse;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class UpdateFileParser {
    private static final String TAG = "UpdateFileParser";
    private static final int BUFFER_SIZE = 8192;// okio's fixed buffer size, bigger sizes result in nonsense.
    IFileManagerService mFileManager;
    IAppService mApp;
    private final BehaviorSubject<Updater.Progress> mProgressSubject = BehaviorSubject.create();

    public static UpdateFileParser getInstance() {
        return ParserHolder.INSTANCE;
    }

    private static class ParserHolder {
        private static final UpdateFileParser INSTANCE = new UpdateFileParser();
    }

    private UpdateFileParser() {
        mFileManager = ServiceContainer.getInstance().getService(IFileManagerService.class);
        mApp = ServiceContainer.getInstance().getService(IAppService.class);
    }

    /**
     * Search, copy, and parse bytes to UpdateFile.
     */
    public Observable<HashMap<Integer, String>> parseFile(String filePath, boolean isLocal) {
        Logger.d("Parsing %1$s file... filePath is \"%2$s\"", isLocal ? "local" : "usb", filePath);
        return Observable.create(emitter -> {
            try {
                // search() method only support files under "/files" and usb
                IFile inputFile = isLocal ? new FabLocalFile(new File(filePath)) : mFileManager.getDevice(false).search(filePath);
                if (!inputFile.exists()) {
                    emitter.onError(new IllegalArgumentException("Null input file!"));
                }
                IFile localFile = copyFileToCache(inputFile);
                Logger.d("File copied to local.");
                HashMap<Integer, String> updateFiles = realParseFile(localFile);
                Logger.d("File parsed.");
                emitter.onNext(updateFiles);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public static byte[] getChunk(String filePath, int index, int maxSpace) {
        try {
            File file = new File(filePath);
            BufferedSource source = Okio.buffer(Okio.source(new FileInputStream(file)));
            if (file.length() > index + 256) {
                source.skip(index + 256);
                int bufSize;
                if (file.length() - (index + 256) >= maxSpace) {
                    bufSize = maxSpace;
                } else {
                    bufSize = (int) (file.length() - (index + 256));
                }
                byte[] buf = new byte[bufSize];
                source.readFully(buf);
                Logger.d("Update: chunk read, size is %d", bufSize);
                return buf;
            } else {
                throw new EOFException("No more buffer!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getMachineFileHeader(String path) {
        try {
            BufferedSource source = Okio.buffer(Okio.source(new FileInputStream(path)));
            byte[] buffer = new byte[256];
            source.readFully(buffer);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Observable<Updater.Progress> getParseProgressObservable() {
        return mProgressSubject.distinctUntilChanged();
    }

    public static boolean isBigBinAvailable(Context context) {
        File file = new File(context.getCacheDir(), "update/update.bin");
        return file.exists();
    }

    public static String getBigBinVersion(Context context) {
        try (BufferedSource source = Okio.buffer(Okio.source(new File(context.getCacheDir(), "update/update.bin")))) {
            source.skip(2);
            byte[] versionBytes = new byte[32];
            source.readFully(versionBytes);
            String version = new String(versionBytes, StandardCharsets.UTF_8);
            return version.trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBigBinPath(Context context) {
        File bigBinFolder = new File(context.getCacheDir(), File.separator + "update");
        if (bigBinFolder.exists() || bigBinFolder.mkdir()) {
            return new File(bigBinFolder, "update.bin").getAbsolutePath();
        } else {
            return null;
        }
    }

    public static List<Integer> parseEMBinIndexes(File emFile) {
        List<Integer> indexes = new ArrayList<>();
        if (!emFile.exists()) return indexes;
        try (BufferedSource source = Okio.buffer(Okio.source(emFile))) {
            source.skip(21);
            source.skip(1);
            source.skip(2);
            source.skip(1);
            int l = source.readShortLe();
            int h = source.readShortLe();
            for (int i = l; i <= h; i++) {
                indexes.add(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexes;
    }

    public static String parseEMVersion(File emFile) {
        if (!emFile.exists()) return "";
        try (BufferedSource source = Okio.buffer(Okio.source(emFile))) {
            source.skip(21);
            source.skip(1);
            source.skip(2);
            source.skip(1);
            source.skip(4);
            byte[] bytes = source.readByteArray(32);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getEMBinPath(Context context) {
        File emFolder = new File(context.getCacheDir(), File.separator + "update");
        if (emFolder.exists() || emFolder.mkdir()) {
            return new File(emFolder, "em.bin").getAbsolutePath();
        }
        return null;
    }

    private HashMap<Integer, String> realParseFile(IFile file) throws Exception {
        HashMap<Integer, String> updateFiles = new HashMap<>();
        BufferedSource source = Okio.buffer(Okio.source(file.getInputStream().getInputStream()));
        // 2 + 32 + 4 + 1 + 3 + 12 + 12
        byte[] headerBytes = source.readByteArray(39 + 4 * 9);
        Buffer buffer = new Buffer();
        buffer.write(headerBytes);
        buffer.skip(38);
        int count = buffer.readByte();// 38
        Logger.d("UpdateFileParser: file count is %d", count);
        source.close();

        Logger.d("Extracting... header is %s", ByteString.of(headerBytes).hex());
        for (int i = 0; i < count; i++) {
            byte type = buffer.readByte();
            int startPos = buffer.readInt();
            int size = buffer.readInt();
            Logger.d("Parse file %1$d, type is %2$d", i, type);

            switch (type) {
                case UpdateFile.MC:
                    String mcPath = extractSmallPackage(type, startPos, size, file, new File(mApp.getCacheDir(), "update/mc.bin").getPath());
                    updateFiles.put((int) type, mcPath);
                    break;
                case UpdateFile.EM:
                    String emPath = extractSmallPackage(type, startPos, size, file, new File(mApp.getCacheDir(), "update/em.bin").getPath());
                    updateFiles.put((int) type, emPath);
                    break;
                case UpdateFile.BT:
                    String btPath = extractSmallPackage(type, startPos, size, file, new File(mApp.getCacheDir(), "update/bt.bin").getPath());
                    updateFiles.put((int) type, btPath);
                    break;
                case UpdateFile.SC:
                    String scPath = extractSmallPackage(type, startPos, size, file, new File(mApp.getCacheDir(), "update/sc.apk").getPath());
                    updateFiles.put((int) type, scPath);
                    break;
            }
        }

        return updateFiles;
    }

    private String extractSmallPackage(int fileType, int startPos, int byteCount, IFile srcFile, String destFilePath) throws IOException {
        long startTime = System.currentTimeMillis();

        BufferedSource readSource = Okio.buffer(Okio.source(srcFile.getInputStream().getInputStream()));
        readSource.skip(startPos);

        BufferedSink sink = Okio.buffer(Okio.sink(new FileOutputStream(destFilePath)));
        byte[] buffer = new byte[BUFFER_SIZE];
        int targetFullReadCount = byteCount / buffer.length;
        int fullReadCount = 0;
        int readBytesCount = 0;
        int read;

        while ((read = readSource.read(buffer)) != -1) {
            fullReadCount++;
            readBytesCount += read;
            // Always comment out logger lines for better performance!
            // Logger.d("full read count is %1$d, target full read count is %2$d, read byte count is %3$d", fullReadCount, targetFullReadCount, readBytesCount);
            sink.write(buffer, 0, read);

            updateProgress(getProgressTypeBy(fileType), readBytesCount, srcFile.length());

            if (fullReadCount == targetFullReadCount) {
                int byteCountLeft = byteCount - readBytesCount;
                Logger.d("byte count left is %d", byteCountLeft);
                byte[] lastBuffer = new byte[byteCountLeft];
                // Use readFully to ensure all left bytes are read at last time.
                readSource.readFully(lastBuffer);
                sink.write(lastBuffer, 0, byteCountLeft);
                readBytesCount += lastBuffer.length;
                break;
            }
        }

        long finishTime = System.currentTimeMillis();
        Logger.d("Copied %1$d file, cost %2$dms", readBytesCount, finishTime - startTime);

        readSource.close();
        sink.close();

        return destFilePath;
    }

    private int getProgressTypeBy(int fileType) {
        switch (fileType) {
            case UpdateFile.MC:
                return -2;
            case UpdateFile.EM:
                return -3;
            case UpdateFile.BT:
                return -4;
            case UpdateFile.SC:
                return -5;
        }
        return -1;
    }

    private void updateProgress(int progressType, int readBytesCount, long length) {
        Updater.Progress progress = new Updater.Progress();
        progress.type = progressType;
        progress.progress = (int) (((float) readBytesCount / length) * 100);
        mProgressSubject.onNext(progress);
    }

    private IFile copyFileToCache(IFile updateFile) throws Exception {
        IFileManagerService fileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        IPartition device = fileManagerService.getDevice(true);
        if (device.getFreeSpace() < updateFile.length()) {
            Logger.d("Start free up space for cache file.");
            try {
                if (updateFile.length() >= device.getTotalSpace()) {
                    device.removeFile(device.getRootFile());
                } else if (updateFile.length() >= device.getFreeSpace() - 30 * 1024 * 1024) {
                    while (updateFile.length() >= device.getFreeSpace() - 30 * 1024 * 1024) {
                        ArrayList<IFile> iFiles = device.getRootFile().listFiles();
                        iFiles.sort(Comparator.comparingLong(IFile::lastModified));
                        iFiles.get(0).removeFile();
                    }
                    Logger.d("Free up space done, available " + device.getFreeSpace());
                }
            } catch (IOException e) {
                Logger.d("Clear local file error!");
                LogHelper.log(e);
            }
        }
        long startTime = System.currentTimeMillis();

        File folder = new File(mApp.getCacheDir(), File.separator + "update");
        if (folder.exists()) {
            if (new File(folder, "update.bin").getAbsolutePath().equals(updateFile.getAbsolutePath())) {
                mProgressSubject.onNext(new Updater.Progress(-1, 100));
                return updateFile;
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdir();
        }

        deleteFilesUnder(folder);
        File localFile = new File(folder, "update.bin");

        BufferedSource source = Okio.buffer(Okio.source(updateFile.getInputStream().getInputStream()));
        BufferedSink sink = Okio.buffer(Okio.sink(new FileOutputStream(localFile)));

        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        int readLen = 0;
        while ((len = source.read(buffer)) != -1) {
            sink.write(buffer, 0, len);
            readLen += len;
            updateProgress(-1, readLen, updateFile.length());
        }
        long finishTime = System.currentTimeMillis();
        Logger.d("Copied %1$d byte bin file, cost %2$dms", localFile.length(), finishTime - startTime);

        source.close();
        sink.close();

        return new FabLocalFile(localFile);
    }

    private void deleteFilesUnder(File folder) {
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
    }


    public static class UpdateFile {
        public static final int MC = 0;
        public static final int EM = 1;
        public static final int BT = 2;
        public static final int SC = 3;

        @IntDef({MC, EM, BT, SC})
        public @interface FileType {
        }

        @FileType
        public int type;
        public String path;

        public UpdateFile(@FileType int type, String path) {
            this.type = type;
            this.path = path;
        }
    }

    public static void cacheVersionInfoToDisk(VersionResponse.NewVersionData newVersion) {
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> FileHelper.saveJSONToFile(
                new Gson().toJson(newVersion),
                new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), "version_check.json")));
    }
}
