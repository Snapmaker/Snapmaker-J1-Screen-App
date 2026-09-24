package fabscreen.platform.base.helper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class Md5Util {
    public static String fileToMD5(String filePath) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024 * 8];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static String inputStreamToMD5(InputStream inputStream) {
        try {
            byte[] buffer = new byte[1024 * 8];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertHashToString(byte[] hashBytes) {
        StringBuilder returnVal = new StringBuilder();
        for (byte hashByte : hashBytes) {
            returnVal.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return returnVal.toString().toLowerCase();
    }
}
