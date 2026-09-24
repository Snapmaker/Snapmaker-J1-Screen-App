package fabscreen.platform.base.service.machine.structure;

import androidx.annotation.NonNull;

import fabscreen.platform.base.service.machine.structure.prop.Int8Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;

//FIXME: implementation is not reasonable, need to modify, eg :CoordinateStructure
public class SubscribeStructure extends BaseStructure {
    public int commandSet;
    public int commandId;
    public int interval;
    public static final String COMMAND_SET = "commandSet";
    public static final String COMMAND_ID = "commandId";
    public static final String INTERVAL = "interval";

    public SubscribeStructure(int commandSet, int commandId, int interval) {
        this.commandSet = commandSet;
        this.commandId = commandId;
        this.interval = interval;
        init();
    }

    @Override
    protected void init() {
        addProp(COMMAND_SET, new Int8Prop((byte) commandSet));
        addProp(COMMAND_ID, new Int8Prop((byte) commandId));
        if (interval != 0) {
            addProp(INTERVAL, new UInt16Prop(interval));
        }
    }

    public int getCommandSet() {
        return commandSet;
    }

    public void setCommandSet(int commandSet) {
        this.commandSet = commandSet;
    }

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @NonNull
    @Override
    public String toString() {
        return "SubscribeStructure{" +
                "commandSet=" + Integer.toHexString(commandSet) +
                ", commandId=" + Integer.toHexString(commandId) +
                ", interval=" + interval +
                '}';
    }
}
