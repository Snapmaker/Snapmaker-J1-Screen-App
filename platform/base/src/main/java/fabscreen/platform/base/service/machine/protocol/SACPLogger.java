package fabscreen.platform.base.service.machine.protocol;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.machine.IStructure;
import okio.ByteString;

public class SACPLogger {
    public static void logScreenRequest(IProtocol.MessageHeader header, IStructure requestBody, byte[] encoded) {
        Logger.d("sacp-debug:screen-request writing:" +
                        "\n commandSet is: %1$s\tcommandId is: %2$s\tsequence is: %3$s" +
                        "\n structure is:%4$s" +
                        "\n encoded is %5$s",
                Integer.toHexString(header.commandSet), Integer.toHexString(header.commandId), Integer.toHexString(header.sequence),
                requestBody,
                ByteString.of(encoded).hex()
        );
    }

    public static void logPacket(IProtocol.Packet p) {
        IProtocol.MessageHeader header = p.header;
        if (header.commandSet == 0xb0 && header.commandId == 0x01) return;
        // Here to control what to log.
        if (!SACPProtocol.isPush(p.header.commandId) /*|| (header.commandSet == 0x01 && header.commandId == 0xa0)*/) {
            Logger.d("sacp-debug: response:" +
                            "\n commandSet is: %1$s\tcommandId is: %2$s\tsequence is: %3$s" +
                            "\n senderId is: %4$s\treceiverId is: %5$s" +
                            "\n encoded is %6$s",
                    Integer.toHexString(header.commandSet), Integer.toHexString(header.commandId), Integer.toHexString(header.sequence),
                    Integer.toHexString(header.senderId), Integer.toHexString(header.receiverId),
                    ByteString.of(p.rawBytes).hex()
            );
        }
    }

    public static void logStructurePacket(IProtocol.Packet p, IStructure structure) {
        IProtocol.MessageHeader header = p.header;
        if (header.commandSet == 0xb0 && header.commandId == 0x01) return;
        // Here to control what to log.
        if (!SACPProtocol.isPush(p.header.commandId) /*|| (header.commandSet == 0x01 && header.commandId == 0xa2)*/) {
            Logger.d("sacp-debug: response:" +
                            "\n commandSet is: %1$s\tcommandId is: %2$s\tsequence is: %3$s" +
                            "\n senderId is: %4$s\treceiverId is: %5$s" +
                            "\n encoded is %6$s" +
                            "\n structure is %7$s",
                    Integer.toHexString(header.commandSet), Integer.toHexString(header.commandId), Integer.toHexString(header.sequence),
                    Integer.toHexString(header.senderId), Integer.toHexString(header.receiverId),
                    ByteString.of(p.rawBytes).hex(),
                    structure
            );
        }
    }

    public static void logReadableErrors(IProtocol.Packet p, IStructure responseStructure, Exception e) {
        Logger.e("sacp-debug-e %1$s : " +
                        "\n PARSE ERROR!" +
                        "\n byte[] is: %2$s" +
                        "\n payload is %3$s" +
                        "\n structure is: %4$s" +
                        "\n error msg is: %5$s" +
                        "\n commandSet is: %6$s" +
                        "\n commandId is: %7$s",
                SACPProtocol.isPush(p.header.commandId) ? "pushACK" : "normalACK",
                ByteString.of(p.rawBytes).hex(),
                ByteString.of(p.payload).hex(),
                responseStructure,
                e.toString(),
                Integer.toHexString(p.header.commandSet),
                Integer.toHexString(p.header.commandId));
    }
}
