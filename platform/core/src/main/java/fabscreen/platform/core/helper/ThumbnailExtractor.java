package fabscreen.platform.core.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import okio.BufferedSource;
import okio.Okio;

/**
 * Extract gcode thumbnail and save the image to cache.
 */
public class ThumbnailExtractor {
    public static String extract(IFile iFile) {
        if (iFile == null) return null;
        if (!isPathGcode(iFile.getName())) return null;
        File thumbnailFile = getCacheThumbnailFile(iFile);
        if (thumbnailFile != null) {
//            Logger.d("Thumbnail already cached!");
            return thumbnailFile.getAbsolutePath();
        }
        try (BufferedSource source = Okio.buffer(Okio.source(iFile.getInputStream().getInputStream()))) {
            while (true) {
                String s = source.readUtf8Line();
                if (s == null || (s.toLowerCase().contains(";header end")) || (s.length() > 0 && !s.contains(";"))) {
                    break;
                } else {
                    String[] param = s.split(":", 2);
                    if (param.length < 2) continue;
                    if ((";thumbnail".equalsIgnoreCase(param[0]))) {
                        return saveThumbnailToCache(getBitmapFromBase64(param[1].trim()), iFile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read gcode file from path and extract the thumbnail to cache folder.
     *
     * @param gcodePath Path of the gcode file.
     * @param isLocal   The gcode file is stored locally or in USB disk.
     * @return The thumbnail file path on screen device.
     */
    public static String extract(String gcodePath, boolean isLocal) {
        if (!isPathGcode(gcodePath)) return null;
        IFile iFile = ServiceContainer.getInstance().getService(IFileManagerService.class).getDevice(isLocal).search(gcodePath);
        if (iFile == null) return null;
        File thumbnailFile = getCacheThumbnailFile(iFile);
        if (thumbnailFile != null) {
//            Logger.d("Thumbnail already cached!");
            return thumbnailFile.getAbsolutePath();
        }
        try (BufferedSource source = Okio.buffer(Okio.source(iFile.getInputStream().getInputStream()))) {
            while (true) {
                String s = source.readUtf8Line();
                if (s == null || (s.toLowerCase().contains(";header end")) || (s.length() > 0 && !s.contains(";"))) {
                    break;
                } else {
                    String[] param = s.split(":", 2);
                    if (param.length < 2) continue;
                    if ((";thumbnail".equalsIgnoreCase(param[0]))) {
                        return saveThumbnailToCache(getBitmapFromBase64(param[1].trim()), iFile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File getCacheThumbnailFile(IFile iFile) {
        File cacheDir = ServiceContainer.getInstance().getService(IAppService.class).getAppContext().getCacheDir();
        String folderPath = cacheDir.getAbsolutePath() + "/gcode_thumbnail" + (iFile.isLocal() ? "/Local" : "/USB");
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
            return null;
        }
        if (iFile.isLocal()) {

        }
        return search(folder, getThumbnailFileName(iFile.getAbsolutePath()));
    }

    private static File search(File nowFile, String path) {
        if (path.isEmpty()) {
            return null;
        }
        int startIndex = path.indexOf(File.separatorChar);
        int endIndex = path.indexOf(File.separatorChar, startIndex + 1);
        String pathName = path;
        if (endIndex != -1) {
            pathName = path.substring(startIndex + 1, endIndex);
        } else if (startIndex != -1) {
            pathName = path.substring(startIndex + 1);
        }
        for (File file : nowFile.listFiles()) {
            if (pathName.equals(file.getName())) {
                if (endIndex == -1) {
                    return file;
                }
                return search(file, path.substring(endIndex));
            }
        }
        return null;
    }


    private static boolean isPathGcode(String gcodePath) {
        return gcodePath.contains(".gcode") || gcodePath.contains(".nc") || gcodePath.contains(".cnc");
    }

    private static String getThumbnailFileName(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf(".")) + ".png";
    }

    private static String saveThumbnailToCache(Bitmap thumbnail, IFile iFile) throws IOException {
        File cacheDir = ServiceContainer.getInstance().getService(IAppService.class).getAppContext().getCacheDir();
        String folderPath = cacheDir.getAbsolutePath() + "/gcode_thumbnail" + (iFile.isLocal() ? "/Local" : "/USB");
        String absolutePath = iFile.getAbsolutePath();
        int endIndex = absolutePath.lastIndexOf(File.separatorChar);
        if (endIndex != -1) {
            folderPath += absolutePath.substring(0, endIndex);
        }
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = absolutePath.substring(absolutePath.lastIndexOf("/"), absolutePath.lastIndexOf(".")) + ".png";
        File thumbnailFile = new File(folder, fileName);
        if (thumbnailFile.exists() && !thumbnailFile.delete()) {
            return thumbnailFile.getAbsolutePath();
        }
        if (!thumbnailFile.createNewFile()) {
            return null;
        }

        try (FileOutputStream out = new FileOutputStream(thumbnailFile)) {
            if (thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                return thumbnailFile.getAbsolutePath();
            } else {
                return null;
            }
        }
    }

    private static Bitmap getBitmapFromBase64(String base64img) {
        byte[] bitmapArray = Base64.decode(base64img.split(",")[1], Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
    }

    public static void deleteExtractCache(IFile iFile) {
        if (iFile == null) {
            return;
        }
        if (!isPathGcode(iFile.getName())) {
            return;
        }
        File thumbnailFile = getCacheThumbnailFile(iFile);
        if (thumbnailFile != null) {
            thumbnailFile.delete();
        }
    }
}
