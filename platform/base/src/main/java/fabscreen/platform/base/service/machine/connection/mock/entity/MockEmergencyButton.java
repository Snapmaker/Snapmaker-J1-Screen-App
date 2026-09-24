package fabscreen.platform.base.service.machine.connection.mock.entity;

import fabscreen.platform.base.service.machine.structure.prop.BoolProp;

public class MockEmergencyButton extends MockModule {
    private boolean isEmergency = false;

    public BoolProp getEmergencyInfo() {
        return new BoolProp(isEmergency);
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
    }
}
