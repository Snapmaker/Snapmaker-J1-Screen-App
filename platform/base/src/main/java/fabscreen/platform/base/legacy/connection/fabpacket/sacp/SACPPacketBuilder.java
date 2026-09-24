package fabscreen.platform.base.legacy.connection.fabpacket.sacp;

public class SACPPacketBuilder {
    private int mSequence = -1;

    private SACPPacketBuilder() {
    }

    public static SACPPacketBuilder getInstance() {
        return Holder.INSTANCE;
    }

    public SACPPacket buildSubscribeHeartBeat(int receiverOpId) {
        return buildPacket((byte) SACPPacket.REQUEST, (byte) 0xF0, (byte) 0x01, new byte[]{(byte) 0xF0, (byte) receiverOpId});
    }

    public SACPPacket buildWatchHeartbeat() {
        return buildRequestPacket((byte) 0xF0, (byte) 0x02);
    }

    private SACPPacket buildRequestPacket(byte commandSet, byte commandId) {
        return this.buildPacket(SACPPacket.REQUEST, commandSet, commandId);
    }

    private SACPPacket buildPacket(@SACPPacket.Attribute int attribute, byte commandSet, byte commandId) {
        return this.buildPacket((byte) attribute, commandSet, commandId, null);
    }

    private SACPPacket buildPacket(byte attribute, byte commandSet, byte commandId, byte[] payload) {
        SACPPacket packet = SACPPacket.create();
        packet.setAttribute(attribute);
        packet.setCommandSet(commandSet);
        packet.setCommandId(commandId);
        packet.setSequence(generateSequence());
        packet.setPayload(payload);
        packet.build();
        return packet;
    }

    private int generateSequence() {
        if (mSequence == 0xFFFF) {
            mSequence = -1;
        }
        mSequence++;
        return mSequence;
    }

    private static class Holder {
        public static final SACPPacketBuilder INSTANCE = new SACPPacketBuilder();
    }
}
