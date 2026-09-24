package fabscreen.platform.base.service.machine.connection.mock.entity;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.service.machine.structure.ExtruderOffsetStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;

public class MockFdmToolHead extends MockModule {
    private int mFilamentStatus;
    private int mHeadStatus;
    private boolean mHeadActive;
    private List<MockExtruder> mExtruderList;
    private List<MockFan> mFanList;
    private List<ExtruderOffset> list;

    public MockFdmToolHead(int filamentStatus, int headStatus, boolean headActive, List<MockExtruder> extruderList, List<MockFan> fanList) {
        mFilamentStatus = filamentStatus;
        mHeadStatus = headStatus;
        mHeadActive = headActive;
        mExtruderList = extruderList;
        mFanList = fanList;
    }

    public int getFilamentStatus() {
        return mFilamentStatus;
    }

    public void setFilamentStatus(int filamentStatus) {
        this.mFilamentStatus = filamentStatus;
    }

    public int getHeadStatus() {
        return mHeadStatus;
    }

    public void setHeadStatus(int headStatus) {
        this.mHeadStatus = headStatus;
    }

    public boolean isHeadActive() {
        return mHeadActive;
    }

    public void setHeadActive(boolean headActive) {
        this.mHeadActive = headActive;
    }

    public List<MockExtruder> getExtruderList() {
        return mExtruderList;
    }

    public void setExtruderList(List<MockExtruder> extruderList) {
        this.mExtruderList = extruderList;
    }

    public List<MockFan> getFanList() {
        return mFanList;
    }

    public void setFanList(List<MockFan> fanList) {
        this.mFanList = fanList;
    }

    public ArrayProp<ExtruderOffsetStructure> getExtruderOffsetsInfo() {
        ArrayProp<ExtruderOffsetStructure> extruderOffsetStructureArrayProp = new ArrayProp<>();
        for (int i = 0; i < list.size(); i++) {
            ExtruderOffset extruderOffset = list.get(i);
            extruderOffsetStructureArrayProp.addElement(new ExtruderOffsetStructure(extruderOffset.index, extruderOffset.axis, extruderOffset.value));
        }
        return extruderOffsetStructureArrayProp;
    }

    public void setExtruderOffset(List<ExtruderOffsetStructure> extruderOffsetStructureList) {
        ArrayList<ExtruderOffset> extruderOffsets = new ArrayList<>();
        for (int i = 0; i < extruderOffsetStructureList.size(); i++) {
            ExtruderOffsetStructure extruderOffset = extruderOffsetStructureList.get(i);
            extruderOffsets.add(new ExtruderOffset(extruderOffset.getExtruderIndex(), extruderOffset.getDirection(), extruderOffset.getDistance()));
        }
        this.list = extruderOffsets;
    }

    public class ExtruderOffset {
        private int index;
        private int axis;
        private float value;

        public ExtruderOffset() {
        }

        public ExtruderOffset(int index, int axis, float value) {
            this.index = index;
            this.axis = axis;
            this.value = value;
        }
    }
}
