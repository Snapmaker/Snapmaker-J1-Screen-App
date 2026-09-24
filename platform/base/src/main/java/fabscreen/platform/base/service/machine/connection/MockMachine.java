package fabscreen.platform.base.service.machine.connection;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockAirPurifier;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockCNCToolHead;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockDryBox;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockEmergencyButton;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockEnclosure;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockExtruder;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockFan;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockFdmToolHead;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockHeatedBed;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockLaserToolHead;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockLinear;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockModule;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockZone;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.CoordinateStructure;
import fabscreen.platform.base.service.machine.structure.OpenDoorDetectionState;
import fabscreen.platform.base.service.machine.structure.StructureVectorMapper;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_AIR_PURIFIER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_DRY_BOX;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_EMERGENCY_BUTTON;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_J1;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_S20;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_2;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_2_5;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_A400;

//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED;
//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_2;
//import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_3;


public class MockMachine {
    public Vector MachineSize;

    public Vector mechanicalCoordinates;
    public Vector originWork;
    private int mCoordinateSystemId = 0;
    private int homed = 1;
    // logicalCoordinates = mechanicalCoordinates - originWork;
    private Vector logicalCoordinates;

    public String controllerVersion;
    // series MachineType  //A150&A250 is a series
    public int seriesId;
    // model under the series 150,250 etc
    public int modelId;
    // SN machine code
    public String serialNo;
    private List<MockModule> moduleList = new ArrayList<>();

    public MockMachine(int seriesId, int modelId) {
        this.seriesId = seriesId;
        this.modelId = modelId;
        serialNo = "" + (int) (Math.random() * Integer.MAX_VALUE);
        controllerVersion = "3.1415926535897932384626433832795";
        Vector vector = new Vector();
        // FIXME: J1 model size
        if (this.seriesId == IMachine.MachineSeries.J) {
            vector.setX(200);
            vector.setY(200);
            vector.setZ(200);
        } else {
            switch (modelId) {
                // FIXME: Correct machine size
                case IMachine.MachineModel.A150:
                    vector.setX(150);
                    vector.setY(150);
                    vector.setZ(150);
                    break;
                case IMachine.MachineModel.A250:
                    vector.setX(250);
                    vector.setY(250);
                    vector.setZ(250);
                    break;
                case IMachine.MachineModel.A350:
                    vector.setX(350);
                    vector.setY(350);
                    vector.setZ(350);
                    break;
                case IMachine.MachineModel.A400:
                    vector.setX(400);
                    vector.setY(400);
                    vector.setZ(400);
                    break;
                default:
            }
        }
        MachineSize = vector;
        //Initialize the line module
        for (int i = 0; i < 4; i++) {
            moduleList.add(createLinear(Module.ModuleType.LINEAR_2, i));
        }

        mechanicalCoordinates = new Vector(80, 13, 50, 0, 0);
        logicalCoordinates = new Vector(80, 13, 50, 0, 0);
        originWork = new Vector(9, 3, 2, 0, 0);
    }

    /**
     * Adds a default module to a machine based on the given module type and index
     *
     * @param moduleId Module type
     * @param index    Module index
     */
    public void addModel(int moduleId, int index) {
        MockModule module = new MockModule();
        switch (moduleId) {
            case HEAD_3DP:
            case HEAD_3DP_DOUBLE_EXTRUDER:
                module = createFDMToolhead(moduleId, index);
                break;
            case HEAD_CNC:
            case HEAD_CNC_200W:
                module = createCncToolHerad(moduleId, index);
                break;
            case HEAD_LASER:
            case HEAD_LASER_10W:
                module = createLaserToolhead(moduleId, index);
                break;
            case LINEAR_2:
            case LINEAR_2_5:
            case LINEAR_A400:
                module = createLinear(moduleId, index);
                break;
            case ADDON_HEATED_BED_S20:
            case ADDON_HEATED_BED_A400:
            case ADDON_HEATED_BED_J1:
                module = createHeatedBed(moduleId, index);
                break;
            case ADDON_ENCLOSURE:
            case ADDON_ENCLOSURE_A400:
                module = createEnclosure(moduleId, index);
                break;
            case ADDON_AIR_PURIFIER:
                module = createAirPurifier(moduleId, index);
                break;
            case ADDON_EMERGENCY_BUTTON:
                module = createEnclosure(moduleId, index);
                break;
            case ADDON_DRY_BOX:
                module = createDryBox(moduleId, index);
                break;
            default:
                initMockModule(module, moduleId, index);
                break;
        }
        moduleList.add(module);
    }


    public ArrayProp<Module.ModuleInfo> getModuleInfos() {
        ArrayProp<Module.ModuleInfo> prop = new ArrayProp<>();
        for (int i = 0; i < moduleList.size(); i++) {
            MockModule mockModule = moduleList.get(i);
            Module.ModuleInfo moduleInfo = new Module.ModuleInfo();
            moduleInfo.setKey(mockModule.key);
            moduleInfo.setModuleId(mockModule.moduleId);
            moduleInfo.setModuleIndex(mockModule.moduleIndex);
            prop.addElement(moduleInfo);
        }
        return prop;
    }

    public MockModule getModuleByKey(int key) {
        if (key >= moduleList.size() || key <= -1) return null;
//        Logger.d("getModuleByKey " + moduleList.toString());
        return moduleList.get(key);
    }

    public List<MockFdmToolHead> getFDMToolHead() {
        ArrayList<MockFdmToolHead> mockFdmToolHeads = new ArrayList<>();
        for (MockModule module : moduleList) {
            if (module.moduleId == Module.ModuleType.HEAD_3DP || module.moduleId == HEAD_3DP_DOUBLE_EXTRUDER) {
                mockFdmToolHeads.add((MockFdmToolHead) module);
            }
        }
        return mockFdmToolHeads;
    }

    public MockHeatedBed getHeatedBed() {
        for (MockModule module : moduleList) {
            if (module.moduleId == ADDON_HEATED_BED_A400 || module.moduleId == ADDON_HEATED_BED_J1 || module.moduleId == ADDON_HEATED_BED_S20) {
                return (MockHeatedBed) module;
            }
        }
        return null;
    }

    public MockAirPurifier getAirPurifier() {
        for (MockModule module : moduleList) {
            if (module.moduleId == ADDON_AIR_PURIFIER) {
                return (MockAirPurifier) module;
            }
        }
        return null;
    }

    public MockCNCToolHead getCncToolHead() {
        for (MockModule module : moduleList) {
            if (module.moduleId == Module.ModuleType.HEAD_CNC || module.moduleId == HEAD_CNC_200W) {
                return (MockCNCToolHead) module;
            }
        }
        return null;
    }

    private MockHeatedBed createHeatedBed(int moduleId, int index) {
        ArrayList<MockZone> zoneInfos = new ArrayList<>();
        MockZone zoneInfo = new MockZone(index, 0, 0);
        zoneInfos.add(zoneInfo);
        MockHeatedBed mockHeatedBed = new MockHeatedBed(0, zoneInfos);
        initMockModule(mockHeatedBed, moduleId, index);
        return mockHeatedBed;
    }

    private MockEnclosure createEnclosure(int moduleId, int index) {
        MockEnclosure mockEnclosure = new MockEnclosure();
        initMockModule(mockEnclosure, moduleId, index);
        mockEnclosure.setStatus(0);
        mockEnclosure.setLedvalue(0);
        ArrayList<OpenDoorDetectionState> openDoorDetectionStates = new ArrayList<>();
        openDoorDetectionStates.add(new OpenDoorDetectionState(1, true));
        openDoorDetectionStates.add(new OpenDoorDetectionState(2, true));
        openDoorDetectionStates.add(new OpenDoorDetectionState(3, true));
        mockEnclosure.setDoorDetectionEnableds(openDoorDetectionStates);
        mockEnclosure.setDoorOpen(true);
        mockEnclosure.setFanSpeed(1);
        return mockEnclosure;
    }

    private MockDryBox createDryBox(int moduleId, int index) {
        MockDryBox mockDryBox = new MockDryBox();
        initMockModule(mockDryBox, moduleId, index);
        mockDryBox.setDryState(1);
        mockDryBox.setTempCurrentChamber(1);
        mockDryBox.setTempTargetChamber(1);
        mockDryBox.setTempWindHole(1);
        mockDryBox.setCurrentHumidity(1);
        mockDryBox.setTargetHumidity(1);
        mockDryBox.setResidualHeatingTime(1);
        mockDryBox.setTargetHeatingTime(1);
        mockDryBox.setCumulativeHeatingTime(1);
        mockDryBox.setLidState(1);
        mockDryBox.setHeaterBlockState(1);
        return mockDryBox;
    }

    private MockModule createAirPurifier(int moduleId, int index) {
        MockAirPurifier mockAirPurifier = new MockAirPurifier();
        initMockModule(mockAirPurifier, moduleId, index);
        mockAirPurifier.setPowerSwitch(true);
        mockAirPurifier.setBlowerSwitch(true);
        mockAirPurifier.setFanSpeedLevel(2);
        mockAirPurifier.setFilterAlive(true);
        mockAirPurifier.setFilterLife(1);
        return mockAirPurifier;
    }

    private MockLinear createLinear(int moduleId, int index) {
        MockLinear mockLinear = new MockLinear();
        initMockModule(mockLinear, moduleId, index);

        mockLinear.setHome(false);
        mockLinear.setLimitSwitch(true);
        mockLinear.setLimitSwitchState(true);
        mockLinear.setLead(50.0f);
        return mockLinear;
    }

    private MockFdmToolHead createFDMToolhead(int moduleId, int index) {
        MockFdmToolHead mockFdmToolHead = new MockFdmToolHead(1, 1, true, null, null);
        initMockModule(mockFdmToolHead, moduleId, index);

        MockExtruder mockExtruder = new MockExtruder(0, 1, 0.514f, 0, 0);
        List<MockExtruder> mockExtruders = new ArrayList<>();
        mockExtruders.add(mockExtruder);
        if (moduleId == HEAD_3DP_DOUBLE_EXTRUDER) {
            mockExtruder = new MockExtruder(1, 1, 0.514f, 0, 0);
            mockExtruders.add(mockExtruder);
        }
        mockFdmToolHead.setExtruderList(mockExtruders);

        MockFan mockFan = new MockFan(0, 0, 1);
        List<MockFan> mockFans = new ArrayList<MockFan>();
        mockFans.add(mockFan);
        mockFdmToolHead.setFanList(mockFans);
        return mockFdmToolHead;
    }

    private MockLaserToolHead createLaserToolhead(int moduleId, int index) {
        MockLaserToolHead mockLaserToolHead = new MockLaserToolHead(0, 1, 10, 0, 0, null);
        initMockModule(mockLaserToolHead, moduleId, index);

        MockFan mockFan = new MockFan(0, 1, 1);
        List<MockFan> mockFans = new ArrayList<MockFan>();
        mockFans.add(mockFan);
        mockLaserToolHead.setFanList(mockFans);
        return mockLaserToolHead;
    }

    private MockCNCToolHead createCncToolHerad(int moduleId, int index) {
        MockCNCToolHead mockCNCToolHead = new MockCNCToolHead(1, true, 1, 1, 0, 0, 0, 0);
        initMockModule(mockCNCToolHead, moduleId, index);
        return mockCNCToolHead;
    }

    private <T extends MockModule> T initMockModule(T module, int moduleId, int index) {
        module.key = moduleList.size();
        module.moduleId = moduleId;
        module.moduleIndex = index;
        module.moduleState = 1;
        module.snProp = (int) (Math.random() * Integer.MAX_VALUE);
        module.hardwareVersion = 1;
        module.moduleFirmwareVersion = module.moduleId + "" + module.snProp;
        return module;
    }

    @Override
    public String toString() {
        return "MockMachine{" +
                "size=" + MachineSize +
                ", controllerVersion='" + controllerVersion + '\'' +
                ", seriesId=" + seriesId +
                ", modelId=" + modelId +
                ", serialNo='" + serialNo + '\'' +
                ", moduleList=" + moduleList +
                '}';
    }

    public IStructure getMachineSizeInfo() {
        List<CoordinateStructure> AxisLength = new ArrayList<>();
        List<CoordinateStructure> originOffset = new ArrayList<>();

        if (this.seriesId == IMachine.MachineSeries.J) {
            AxisLength.add(new CoordinateStructure(0, 100));
            AxisLength.add(new CoordinateStructure(1, 100));
            AxisLength.add(new CoordinateStructure(2, 100));
            AxisLength.add(new CoordinateStructure(6, 100));

            originOffset.add(new CoordinateStructure(0, 50));
            originOffset.add(new CoordinateStructure(1, 50));
            originOffset.add(new CoordinateStructure(2, 50));
            originOffset.add(new CoordinateStructure(6, 50));
        } else {
            switch (modelId) {
                // FIXME: Correct machine size
                case IMachine.MachineModel.A150:
                    AxisLength.add(new CoordinateStructure(0, 100));
                    AxisLength.add(new CoordinateStructure(1, 100));
                    AxisLength.add(new CoordinateStructure(2, 100));

                    originOffset.add(new CoordinateStructure(0, 50));
                    originOffset.add(new CoordinateStructure(1, 50));
                    originOffset.add(new CoordinateStructure(2, 50));
                    break;
                case IMachine.MachineModel.A250:
                    AxisLength.add(new CoordinateStructure(0, 100));
                    AxisLength.add(new CoordinateStructure(1, 100));
                    AxisLength.add(new CoordinateStructure(2, 100));

                    originOffset.add(new CoordinateStructure(0, 50));
                    originOffset.add(new CoordinateStructure(1, 50));
                    originOffset.add(new CoordinateStructure(2, 50));
                    break;
                case IMachine.MachineModel.A350:
                    AxisLength.add(new CoordinateStructure(0, 100));
                    AxisLength.add(new CoordinateStructure(1, 100));
                    AxisLength.add(new CoordinateStructure(2, 100));

                    originOffset.add(new CoordinateStructure(0, 50));
                    originOffset.add(new CoordinateStructure(1, 50));
                    originOffset.add(new CoordinateStructure(2, 50));
                    break;
                case IMachine.MachineModel.A400:
                    AxisLength.add(new CoordinateStructure(0, 100));
                    AxisLength.add(new CoordinateStructure(1, 100));
                    AxisLength.add(new CoordinateStructure(2, 100));

                    originOffset.add(new CoordinateStructure(0, 50));
                    originOffset.add(new CoordinateStructure(1, 50));
                    originOffset.add(new CoordinateStructure(2, 50));
                    break;
                default:
                    AxisLength.add(new CoordinateStructure(0, 100));
                    AxisLength.add(new CoordinateStructure(1, 100));
                    AxisLength.add(new CoordinateStructure(2, 100));

                    originOffset.add(new CoordinateStructure(0, 50));
                    originOffset.add(new CoordinateStructure(1, 50));
                    originOffset.add(new CoordinateStructure(2, 50));
            }
        }
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("AxisLength", new ArrayProp<>(AxisLength));
                addProp("homeOffset ", new ArrayProp<>(originOffset));
            }
        };
        return baseStructure;
    }

    public IStructure getCoordinateSystemInformation() {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("homed", new UInt8Prop());
                addProp("CoordinateSystemId", new UInt8Prop());
                addProp("isoriginOffsetCoordinateSystem", new BoolProp());
                addProp("coordinates", new ArrayProp<CoordinateStructure>());
                addProp("originOffset", new ArrayProp<CoordinateStructure>());
            }
        };
        baseStructure.getProp("homed").setValue(homed);
        baseStructure.getProp("CoordinateSystemId").setValue(mCoordinateSystemId);
        baseStructure.getProp("isoriginOffsetCoordinateSystem").setValue(mCoordinateSystemId == 0);

        List<CoordinateStructure> coordinates = StructureVectorMapper.vectorToStructureList(mechanicalCoordinates);
        List<CoordinateStructure> originOffset = StructureVectorMapper.vectorToStructureList(originWork);
        baseStructure.getProp("coordinates").setValue(coordinates);
        baseStructure.getProp("originOffset").setValue(originOffset);
        return baseStructure;
    }

    public void updateCoordinateSystem(int coordinateSystemId) {
        mCoordinateSystemId = coordinateSystemId;
        Logger.d("updateCoordinateSystem: " + mCoordinateSystemId);
    }

    public void setCoordinate(Vector vector) {
        originWork = vector;
    }

    public void machineMoving(int i, List<CoordinateStructure> absoluteMove) {
        Vector tempVector = mCoordinateSystemId == 0 ? mechanicalCoordinates : logicalCoordinates;
        if (i == 0) {
            for (int j = 0; j < absoluteMove.size(); j++) {
                float valueByAxis = tempVector.getValueByAxis(absoluteMove.get(j).getAxis());
                tempVector.setValueByAxis(absoluteMove.get(j).getAxis(), valueByAxis - absoluteMove.get(j).getVector());
            }
        } else {
            for (int j = 0; j < absoluteMove.size(); j++) {
                tempVector.setValueByAxis(absoluteMove.get(j).getAxis(), absoluteMove.get(j).getVector());
            }
        }
        if (mCoordinateSystemId == 0) {
            mechanicalCoordinates = tempVector;
        } else {
            logicalCoordinates = tempVector;
        }
    }

    public void goHome(int home) {
        if (mCoordinateSystemId != 0) return;
        Vector tempVector = mechanicalCoordinates;
        switch (home) {
            case 0:
                homed = home;
                tempVector.setX(0);
                tempVector.setY(0);
                tempVector.setZ(0);
                tempVector.setX2(0);
                tempVector.setB(0);
                break;
            case 1:
                homed = 1;
                tempVector.setX(0);
                break;
            case 2:
                homed = 1;
                tempVector.setY(0);
                break;
            case 3:
                homed = 1;
                tempVector.setZ(0);
                break;
            default:
        }
        mechanicalCoordinates = tempVector;
    }

    public BoolProp getEmergencyInfo() {
        for (int i = 0; i < moduleList.size(); i++) {
            if (moduleList.get(i) instanceof MockEmergencyButton) {
                return ((MockEmergencyButton) moduleList.get(i)).getEmergencyInfo();
            }

        }
        return null;
    }

    public MockLaserToolHead getLaserToolHead() {
        for (int i = 0; i < moduleList.size(); i++) {
            if (moduleList.get(i) instanceof MockLaserToolHead) {
                return ((MockLaserToolHead) moduleList.get(i));
            }
        }
        return null;
    }

    public MockDryBox getDryBox() {
        for (int i = 0; i < moduleList.size(); i++) {
            if (moduleList.get(i) instanceof MockDryBox) {
                return ((MockDryBox) moduleList.get(i));
            }
        }
        return null;
    }


    public void lineCanMake(boolean isCan) {
        for (int i = 0; i < moduleList.size(); i++) {
            if (moduleList.get(i) instanceof MockLinear) {
                ((MockLinear) moduleList.get(i)).setLimitSwitch(isCan);
            }
            homed = 1;
        }
    }
}
