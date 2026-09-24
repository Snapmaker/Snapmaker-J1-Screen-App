package fabscreen.platform.base.legacy.connection.fabpacket.sacp;

import java.io.IOException;
import java.io.InputStream;

import fabscreen.platform.base.legacy.connection.IPacket;
import fabscreen.platform.base.legacy.connection.fabpacket.PacketInputStream;
import fabscreen.platform.lib.ChecksumUtils;

public class SACPPacketInputStream extends PacketInputStream {

    public SACPPacketInputStream(InputStream in) {
        super(in);
    }

    @Override
    public byte[] readRawPacket() throws IOException {
        byte[] buffer = getBufIfOpen();
//        Logger.d("read buffer: %s", ByteString.of(buffer).hex());
        while (true) {
            // Scan for marker
            while (pos + 1 < count) {
                if (buffer[pos] == (byte) 0xaa && buffer[pos + 1] == (byte) 0x55) {
                    break;
                }
//                Logger.d("Debug buffer[pos] %2X, buffer[pos+1] %2X", buffer[pos], buffer[pos + 1]);
                pos++;
//                Logger.d("Marker not found, pos added to %1$d, count is %2$d", pos, count);
            }

            // Throw away bytes before pos
            if (pos < count && pos >= count / 2) {
//                Logger.d("Buffer cutting, old buffer is %s", new ByteString(buffer).hex());
//                Logger.d("before cut pos %d, count %d", pos, count);
                System.arraycopy(buffer, pos, buffer, 0, count - pos);
                count -= pos;
                pos = 0;
//                Logger.d("cut completed, buffer preview \n%s", new ByteString(buffer).hex());
//                Logger.d("after cut pos %d, count %d", pos, count);
            }

            // Marker not found, fill 128 bytes and start over
            // FIXME: 2022/1/28 temporary change to 256 to avoid data loss, need investigation
            if (pos + 1 >= count) {
//                Logger.d("Marker not found, filling and starting over.");
                int f = fill(DEFAULT_FILL_BUFFER_SIZE);
//                Logger.d("fill raw data %s", new ByteString(buffer).hex());
                if (f == -1) {
                    return null;
                }
                continue;
            }

            // Smallest packet(without payload) size is 15
            if (pos + 15 > count) {
//                Logger.d("Buffer count too small, full filling...");
                int f = fullfill(pos + 15 - count);
                if (f == -1) {
                    return null;
                }
            }

            // check crc8
            if (isCRC8OK(buffer, pos)) {
                int length = getPacketLength(buffer, pos);
//                Logger.d("packet length is %s", Integer.toHexString(length));

                if (pos + 7 + length > count) {
                    int f = fullfill(pos + 7 + length - count);
                    if (f == -1) {
                        return null;
                    }
                }

                if (isChecksumOK(buffer, pos, length)) {
                    byte[] packet = new byte[7 + length];
                    System.arraycopy(buffer, pos, packet, 0, 7 + length);
                    pos += 7 + length;
//                    Logger.d("return packet, now pos %d, count %d, packet length %s, as is %d+7", pos, count, Integer.toHexString(length), length);
                    return packet;
                } else {
                    pos++;
                }
            } else {
                pos++;
            }
        }
    }

    @Deprecated
    @Override
    public IPacket readPacket() throws IOException {
        return new SACPPacket(readRawPacket());
    }

    public static boolean isCRC8OK(byte[] buffer, int offset) throws IOException {
        int crc8 = buffer[offset + 6];
        int crc8Calculated = ChecksumUtils.calculateCRC8(buffer, offset, 6);
//        Logger.d("crc8 is %1$s\n, crc8 cal is %2$s", Integer.toHexString(crc8), Integer.toHexString(crc8Calculated));
        return crc8 == crc8Calculated;
    }

    public static boolean isChecksumOK(byte[] buffer, int offset, int length) {
//        byte[] packet = new byte[7 + length];
//        System.arraycopy(buffer, offset, packet, 0, 7 + length);
//        Logger.d("rcv buff is %s", ByteString.of(packet).hex());
//        Logger.d("rcv buff len is %d", length);
        int checksum = (buffer[offset + 7 + length - 2] & 0xFF) | ((buffer[offset + 7 + length - 1] & 0xFF) << 8);
        int checksumCalculated = ChecksumUtils.calculateChecksum(buffer, offset + 7, length - 2);
//        Logger.d("checksum is %1$s, cal checksum is %2$s", Integer.toHexString(checksum), Integer.toHexString(checksumCalculated));
        return checksum == checksumCalculated;
    }

    public static int getPacketLength(byte[] buffer, int offset) {
        return (buffer[offset + 2] & 0xFF) | ((buffer[offset + 3] & 0xFF) << 8);
    }
}
