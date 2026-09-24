package fabscreen.platform.base;

import java.io.IOException;

import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.lib.LogHelper;
import okio.Buffer;

public class ModuleVersion {
    final public static int TYPE_MODULE_TOOL_HEAD_3DP = 0x00;
    final public static int TYPE_MODULE_TOOL_HEAD_CNC = 0x01;
    final public static int TYPE_MODULE_TOOL_HEAD_LASER_1600 = 0x02;
    final public static int TYPE_MODULE_LINEAR_MODULE_2_0 = 0x03;
    final public static int TYPE_MODULE_ADD_ON_LED_STRIP = 0x04;
    final public static int TYPE_MODULE_ADD_ON_ENCLOSURE = 0x05;
    final public static int TYPE_MODULE_ADD_ON_ROTARY_MODULE = 0x06;
    final public static int TYPE_MODULE_ADD_ON_AIR_PURIFIER = 0x07;
    final public static int TYPE_MODULE_ADD_ON_EMERGENCY_STOP_BUTTON = 0x08;
    final public static int TYPE_MODULE_ADD_ON_CNC_TOOL_SETTER = 0x09;
    final public static int TYPE_MODULE_CHANGEOVER_TOOL_HEAD_ORIGINAL = 0x0A;
    final public static int TYPE_MODULE_ADD_ON_FAN = 0x0B;
    final public static int TYPE_MODULE_LINEAR_MODULE_2_5 = 0x0C;
    final public static int TYPE_MODULE_TOOL_HEAD_3DP_DUAL_NOZZLE = 0x0D;
    public int moduleID;
    public int moduleType;
    public String version;

    public static ModuleVersion parse(byte[] content) {
        Buffer buffer = new Buffer();
        buffer.write(content);

        ModuleVersion moduleVersion = new ModuleVersion();

        try {
            buffer.readByte();
            moduleVersion.moduleID = buffer.readInt();
            moduleVersion.version = buffer.readString(SSTPPacketContent.UTF_8).trim();

            moduleVersion.moduleType = (moduleVersion.moduleID & 0x1FF00000) >> 20;
        } catch (IOException e) {
            LogHelper.log(e);
            return null;
        }

        return moduleVersion;
    }

    public boolean equalType(int type) {
        return (moduleType & type) > 0;
    }
}
