package fabscreen.platform.base.legacy.connection.fabpacket.sacp;

import androidx.annotation.IntDef;

import com.orhanobut.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import fabscreen.platform.base.legacy.connection.IPacket;
import fabscreen.platform.lib.ChecksumUtils;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class SACPPacket implements IPacket {

    public static final int REQUEST = 0;
    public static final int ACK = 1;
    private static final ByteString PROTO_MARKER = ByteString.decodeHex("aa55");
    private static final byte HARDWARE_ID_LB = 0X00;
    private static final byte HARDWARE_ID_MC = 0X01;
    private static final byte HARDWARE_ID_SC = 0X02;
    private ByteString marker;
    private int length;
    private byte version;
    private byte receiverId;
    private byte verify;// byte0-byte5 crc8
    private byte senderId;
    private byte attribute;
    private int sequence;
    private byte commandSet;
    private byte commandId;
    private byte[] payload;
    private int checksum;

    private SACPPacket() {
        marker = PROTO_MARKER;
        version = 0x02;
    }

    public SACPPacket(byte[] packet) throws IOException {
        Logger.d("new sacp packet is %s", new ByteString(packet).hex());
        BufferedSource source = Okio.buffer(Okio.source(new ByteArrayInputStream(packet)));
        marker = source.readByteString(2);
        length = source.readShort();
        version = source.readByte();
        receiverId = source.readByte();
        verify = source.readByte();
        senderId = source.readByte();
        attribute = source.readByte();
        sequence = source.readShort();
        commandSet = source.readByte();
        commandId = source.readByte();
//        Logger.d("sacppacket length is" + length);
        payload = source.readByteArray(length - 8);
        checksum = source.readShort();
    }

    public static SACPPacket create() {
        return new SACPPacket();
    }

    public void setAttribute(byte attribute) {
        this.attribute = attribute;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void build() {
        length = payload == null ? 8 : payload.length + 8;
        receiverId = HARDWARE_ID_MC;
        verify = 0x00;
        senderId = HARDWARE_ID_SC;
        checksum = 0;
        byte[] bytes = toByteArray();
        verify = ChecksumUtils.calculateCRC8(bytes, 0, 6);
        checksum = ChecksumUtils.calculateChecksum(bytes, 7, length - 2);
    }

    @Override
    public byte[] toByteArray() {
        Buffer buffer = new Buffer();
        buffer.write(marker);
        buffer.writeShort(length);
        buffer.writeByte(version);
        buffer.writeByte(receiverId);
        buffer.writeByte(verify);
        buffer.writeByte(senderId);
        buffer.writeByte(attribute);
        buffer.writeShort(sequence);
        buffer.writeByte(commandSet);
        buffer.writeByte(commandId);
        if (payload != null) {
            buffer.write(payload);
        }
        buffer.writeShort(checksum);
        return buffer.readByteArray();
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public int getKey() {
        return commandSet * 1000 + commandId;
    }

    // FIXME: 2021/12/31 to be deleted
    @Override
    public byte getEventId() {
        // not implemented
        return commandSet;
    }

    public byte getCommandSet() {
        return commandSet;
    }

    public void setCommandSet(byte commandSet) {
        this.commandSet = commandSet;
    }

    public byte getCommandId() {
        return commandId;
    }

    public void setCommandId(byte commandId) {
        this.commandId = commandId;
    }

    @Override
    public byte[] getContent() {
        // not implemented
        return new byte[0];
    }

    @Override
    public String toHexString() {
        return new ByteString(toByteArray()).hex();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REQUEST, ACK})
    public @interface Attribute {
    }
}
