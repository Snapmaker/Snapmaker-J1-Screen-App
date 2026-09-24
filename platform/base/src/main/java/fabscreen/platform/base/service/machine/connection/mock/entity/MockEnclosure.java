package fabscreen.platform.base.service.machine.connection.mock.entity;

import java.util.List;

import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.structure.OpenDoorDetectionState;

public class MockEnclosure extends MockModule {
    private int status;
    private int ledvalue;
    private List<OpenDoorDetectionState> openDoorDetectionStates;
    private boolean isDoorOpen;
    private int fanSpeed;

    public MockEnclosure() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLedvalue() {
        return ledvalue;
    }

    public void setLedvalue(int ledvalue) {
        this.ledvalue = ledvalue;
    }

    public List<OpenDoorDetectionState> getDoorDetectionEnabled() {
        return openDoorDetectionStates;
    }

    public void setDoorDetectionEnableds(List<OpenDoorDetectionState> doorDetectionEnableds) {
        openDoorDetectionStates = doorDetectionEnableds;
    }

    public void setDoorDetectionEnabled(OpenDoorDetectionState openDoorDetectionState) {
        for (int i = 0; i < openDoorDetectionStates.size(); i++) {
            if (openDoorDetectionStates.get(i).getWorkType() == openDoorDetectionState.getWorkType())
                openDoorDetectionStates.get(i).setState(openDoorDetectionState.getState());
        }
    }

    public boolean isDoorOpen() {
        return isDoorOpen;
    }

    public void setDoorOpen(boolean doorOpen) {
        isDoorOpen = doorOpen;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(int fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public Enclosure.EnclosureStatus getBedInfo() {
        Enclosure.EnclosureStatus enclosureStatus = new Enclosure.EnclosureStatus();
        enclosureStatus.setKey(key);
        enclosureStatus.setStatus(getStatus());
        enclosureStatus.setLedValue(getLedvalue());
        enclosureStatus.setDoorDetectionEnabled(getDoorDetectionEnabled());
        enclosureStatus.setDoorOpen(isDoorOpen());
        enclosureStatus.setFanSpeed(getFanSpeed());
        return enclosureStatus;
    }
}
