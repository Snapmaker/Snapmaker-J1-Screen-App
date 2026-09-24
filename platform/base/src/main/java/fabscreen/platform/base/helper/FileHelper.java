package fabscreen.platform.base.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import fabscreen.platform.lib.LogHelper;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class FileHelper {
    private static final int BUFFER_SIZE = 8192;// okio's fixed buffer size, bigger sizes result in nonsense.

    public static File getFilesDir(Context context) {
        return context.getFilesDir();
    }

    private static File getConfigDir(Context context) {
        return context.getDir("config", Context.MODE_PRIVATE);
    }

    public static File getMachineVersionFile(Context context) {
        return new File(getConfigDir(context), "machineVersion");
    }

    public static File getCacheDir(Context context) {
        return context.getCacheDir();
    }

    public static File getCachedUpdateFilesDir(Context context) {
        return new File(getCacheDir(context), "update");
    }

    public static File getPersistUpdateFilesDir(Context context) {
        return context.getDir("update", Context.MODE_PRIVATE);
    }

    public static boolean removeFile(File file) {
        if (file == null) return false;

        // If is file then delete it directly.
        if (file.isFile()) {
            return file.delete();
        }

        // If is directory then recursively remove child files.
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return file.delete();
            }
            for (File f : files) {
                if (!removeFile(f)) {
                    return false;
                }
            }
            return file.delete();
        }

        return true;
    }

    public static boolean deleteFilesUnder(@NonNull File dir) {
        if (!dir.isDirectory()) throw new IllegalArgumentException("Not directory.");
        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {
            for (File f : files) {
                return removeFile(f);
            }
        }

        return true;
    }

    public static boolean moveFiles(@NonNull File fromUnderDir, @NonNull File toUnderDir) {
        if (!fromUnderDir.isDirectory() || !toUnderDir.isDirectory())
            throw new IllegalArgumentException("Not directory");
        File[] fromFiles = fromUnderDir.listFiles();
        if (fromFiles == null || fromFiles.length == 0) return false;
        for (File fromFile : fromFiles) {
            File toUnderFile = new File(toUnderDir, fromFile.getName());
            if (fromFile.isDirectory()) {
                if (moveFiles(fromFile, toUnderFile)) {
                    if (!fromFile.delete()) return false;
                }
            } else {
                try (BufferedSource bufferedSource = Okio.buffer(Okio.source(fromFile));
                     BufferedSink bufferedSink = Okio.buffer(Okio.sink(toUnderFile))) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = bufferedSource.read(buffer)) != -1) {
                        bufferedSink.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    LogHelper.log(e);
                    return false;
                }
                if (!fromFile.delete()) return false;
            }
        }
        return true;
    }

    public static void saveJSONToFile(String json, File file) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                return;
            }
            BufferedSink buffer = Okio.buffer(Okio.sink(file));
            buffer.write(json.getBytes(StandardCharsets.UTF_8));
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readJSONFromFile(File file) {
        StringBuilder builder = new StringBuilder();
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        try (BufferedSource source = Okio.buffer(Okio.source(file))) {
            while (true) {
                String line = source.readUtf8Line();
                if (line == null) {
                    break;
                } else {
                    builder.append(line);
                }
            }
        } catch (Exception ignore) {
        }
        return builder.toString();
    }
}
