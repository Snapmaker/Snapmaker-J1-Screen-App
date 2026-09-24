package fabscreen.platform.lib;

public class ChecksumUtils {
    public static int calculateChecksum(byte[] buffer, int offset, int length) {
        // TCP/IP checksum
        // https://locklessinc.com/articles/tcp_checksum/
        int sum = 0;

        for (int i = 0; i < length - 1; i += 2) {
            sum += (buffer[offset + i] & 0xFF) * 0x100 + (buffer[offset + i + 1] & 0xFF);
        }

        if ((length & 1) > 0) {
            sum += (buffer[offset + length - 1] & 0xFF);
        }

        while ((sum >> 16) > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }

        return ((~sum) & 0xFFFF);
    }

    public static byte calculateCRC8(byte[] bytes, int offset, int length) {
        int crc = 0x00;
        int poly = 0x07;
        for (int i = offset; i < offset + length; i++) {
            for (int j = 0; j < 8; j++) {
                boolean bit = ((bytes[i] >> (7 - j) & 1) == 1);
                boolean c07 = ((crc >> 7 & 1) == 1);
                crc <<= 1;
                if (c07 ^ bit) {
                    crc ^= poly;
                }
            }
        }
        crc &= 0xff;
        return (byte) crc;
    }
}
