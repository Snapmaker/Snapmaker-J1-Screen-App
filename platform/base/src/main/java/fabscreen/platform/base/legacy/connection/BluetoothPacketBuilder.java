package fabscreen.platform.base.legacy.connection;

import com.orhanobut.logger.Logger;

import okio.Buffer;

public class BluetoothPacketBuilder {

    public static final byte LASER_CAMERA_CHECK_AUTO_WHITE_BALANCE_REQUEST_EVENT_ID = 0x03;
    public static final byte LASER_CAMERA_CHECK_AUTO_WHITE_BALANCE_RESPONSE_EVENT_ID = 0x04;
    public static final byte LASER_CAMERA_SET_AUTO_WHITE_BALANCE_REQUEST_EVENT_ID = 0x05;
    public static final byte LASER_CAMERA_SET_AUTO_WHITE_BALANCE_RESPONSE_EVENT_ID = 0x06;
    public static final byte LASER_CAMERA_SET_EXPOSE_TIME_REQUEST_EVENT_ID = 0x07;
    public static final byte LASER_CAMERA_SET_EXPOSE_TIME_RESPONSE_EVENT_ID = 0x08;
    public static final byte LASER_CAMERA_SET_PHOTO_RESOLUTION_REQUEST_EVENT_ID = 0x09;
    public static final byte LASER_CAMERA_SET_PHOTO_RESOLUTION_RESPONSE_EVENT_ID = 0x0a;
    public static final byte LASER_CAMERA_SET_PHOTO_QUALITY_REQUEST_EVENT_ID = 0x0b;
    public static final byte LASER_CAMERA_SET_PHOTO_QUALITY_RESPONSE_EVENT_ID = 0x0c;
    public static final byte LASER_CAMERA_PHOTO_REQUEST_EVENT_ID = 0x0d;
    public static final byte LASER_CAMERA_PHOTO_RESPONSE_EVENT_ID = 0x0e;
    public static final byte LASER_CAMERA_SET_CAMERA_LIGHTING_REQUEST_EVENT_ID = 0x17;
    public static final byte LASER_CAMERA_SET_CAMERA_LIGHTING_RESPONSE_EVENT_ID = 0x18;

    private static SSTPPacket buildPacket(byte eventId, byte[] content) {
        SSTPPacket packet = SSTPPacket.create();
        packet.setEventId(eventId);
        packet.setContent(content);
        packet.build();
        return packet;
    }

    public static SSTPPacket isAutoWhiteBalanceActivated() {
        return buildPacket(LASER_CAMERA_CHECK_AUTO_WHITE_BALANCE_REQUEST_EVENT_ID, new byte[]{(byte) 0x00});
    }

    public static SSTPPacket setAutoWhiteBalanceActivated(boolean activated) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x00);
        buffer.writeByte(activated ? 0x01 : 0x00);
        byte[] content = buffer.readByteArray();
        return buildPacket(LASER_CAMERA_SET_AUTO_WHITE_BALANCE_REQUEST_EVENT_ID, content);
    }

    /**
     * @param time Time for exposure, 0 for "auto(default)"
     */
    public static SSTPPacket setExposeTime(int time) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x00);
        buffer.writeByte((time << 8) & 0xFF);
        buffer.writeByte(time & 0xFF);
        Logger.d("time %08X %08X", (time << 8) & 0xFF, time & 0xFF);
        byte[] content = buffer.readByteArray();
        return buildPacket(LASER_CAMERA_SET_EXPOSE_TIME_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket setPhotoQuality(int value) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x00);
        buffer.writeByte(value);
        byte[] content = buffer.readByteArray();
        return buildPacket(LASER_CAMERA_SET_PHOTO_QUALITY_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket setCameraLighting(boolean enabled) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x00);
        buffer.writeByte(enabled ? 0x00 : 0x01);
        byte[] content = buffer.readByteArray();
        return buildPacket(LASER_CAMERA_SET_CAMERA_LIGHTING_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket setPhotoResolution(int frameSize) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x00);
        buffer.writeByte(frameSize);
        byte[] content = buffer.readByteArray();
        return buildPacket(LASER_CAMERA_SET_PHOTO_RESOLUTION_REQUEST_EVENT_ID, content);
    }

    /**
     * Bluetooth Protocol
     * Laser Camera Operation: Request Laser Camera capture photo
     */
    public static SSTPPacket requestCapturePhoto() {
        return buildPacket(LASER_CAMERA_PHOTO_REQUEST_EVENT_ID, new byte[]{(byte) 0x00});
    }

    /**
     * Bluetooth Protocol
     * Laser Camera Operation: Request Laser Camera capture photo
     */
    public static SSTPPacket requestCapturePhoto(int flashTime, int flashDelay) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x00);
        buffer.writeShort(flashTime);
        buffer.writeShort(flashDelay);
        byte[] content = buffer.readByteArray();
        return buildPacket(LASER_CAMERA_PHOTO_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket watchPhotoReceive() {
        return buildPacket(LASER_CAMERA_PHOTO_REQUEST_EVENT_ID, new byte[]{(byte) 0x02});
    }
}
