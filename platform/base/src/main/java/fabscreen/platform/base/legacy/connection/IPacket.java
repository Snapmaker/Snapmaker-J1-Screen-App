package fabscreen.platform.base.legacy.connection;

public interface IPacket {
    String toHexString();

    byte[] toByteArray();

    int getSequence();

    void setSequence(int sequence);

    void setPayload(byte[] bytes);

    int getKey();

    byte getEventId();

    byte[] getContent();
}
