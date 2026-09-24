package fabscreen.platform.base.service.machine.connection.mock.entity;

import fabscreen.platform.base.service.machine.entity.parts.Fan;

public class MockFan {
    private int mId;
    private int mType;
    private int mSpeedLevel;

    public MockFan(int id, int type, int speedLevel) {
        mId = id;
        mType = type;
        mSpeedLevel = speedLevel;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getSpeedLevel() {
        return mSpeedLevel;
    }

    public void setSpeedLevel(int speedLevel) {
        this.mSpeedLevel = speedLevel;
    }

    public Fan getFan() {
        return new Fan(mId, mType, mSpeedLevel);
    }
}
