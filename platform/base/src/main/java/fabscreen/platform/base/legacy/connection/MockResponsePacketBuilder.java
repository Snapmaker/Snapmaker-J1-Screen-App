package fabscreen.platform.base.legacy.connection;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.Module;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okio.Buffer;

public class MockResponsePacketBuilder {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static Random mRandom = new Random(System.currentTimeMillis());
    private static byte mCoordinateAligned = 0;
    private static byte mHomed = 1;
    private static int mMachineType = IMachine.DeprecatedMachineSeries.MACHINE_J_1;
    private static int mHeadType = Module.ModuleType.HEAD_UNPLUGGED;

    private static short mLeftHeadTargetTemp = (short) mRandom.nextInt(300);
    private static short mLeftHeadTemperature = (short) mRandom.nextInt(mLeftHeadTargetTemp);
    private static short mRightHeadTargetTemp = (short) mRandom.nextInt(300);
    private static short mRightHeadTemperature = (short) mRandom.nextInt(mRightHeadTargetTemp);

    private static short mBedTargetTemp = (short) mRandom.nextInt(120);
    private static short mBedTemperature = (short) mRandom.nextInt(mBedTargetTemp);
    private static byte mRotaryModuleStatus = 0;
    private static MockResponsePacketBuilder mInstance;
    private CompositeDisposable disposables = new CompositeDisposable();

    private MockResponsePacketBuilder() {

    }

    public static MockResponsePacketBuilder getInstance() {
        if (mInstance == null) {
            mInstance = new MockResponsePacketBuilder();
        }

        return mInstance;
    }

    public SSTPPacket buildMachineStatus() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x01);
        buffer.writeInt(mRandom.nextInt());
        buffer.writeInt(mRandom.nextInt());
        buffer.writeInt(mRandom.nextInt());
        buffer.writeInt(mRandom.nextInt());
        // Bed Temperature
        buffer.writeShort(getTemperature(0));
        buffer.writeShort(mBedTargetTemp);
        // Left Head Temperature
        buffer.writeShort(getTemperature(1));
        buffer.writeShort(mLeftHeadTargetTemp);

        buffer.writeShort(666);
        buffer.writeInt(100);
        buffer.writeInt(555);
        buffer.writeByte(100);
        buffer.writeByte(0);
        // headStatus
        buffer.writeByte(mHeadType);
        // performLineNumber
        buffer.writeInt(mRandom.nextInt());
        // Right Head Temperature
        buffer.writeShort(getTemperature(2));
        buffer.writeShort(mRightHeadTargetTemp);


        packet.setEventId((byte) 0x08);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    private short getTemperature(int index) {
        short temprature = 0;
        short targetTemp = 0;
        switch (index) {
            case 0:
                temprature = mBedTemperature;
                targetTemp = mBedTargetTemp;
                mBedTemperature = aradual(temprature, targetTemp);
                return mBedTemperature;
            case 1:
                temprature = mLeftHeadTemperature;
                targetTemp = mLeftHeadTargetTemp;
                mLeftHeadTemperature = aradual(temprature, targetTemp);
                return mLeftHeadTemperature;
            case 2:
                temprature = mRightHeadTemperature;
                targetTemp = mRightHeadTargetTemp;
                mRightHeadTemperature = aradual(temprature, targetTemp);
                return mRightHeadTemperature;
            default:
                return temprature;
        }
    }

    private short aradual(short headTemprature, short headTargetTemp) {
        return (short) (headTargetTemp >= headTemprature ? headTemprature + 5 : headTemprature - 5);
    }

    public SSTPPacket buildCoordinateSystem() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0e);
        buffer.writeByte(mHomed);
        buffer.writeByte(mCoordinateAligned);
        buffer.writeInt(0);
        buffer.writeInt(1);
        buffer.writeInt(2);
        buffer.writeInt(3);
        packet.setEventId((byte) 0x08);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildTrue(int eventId, int operation) {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte((byte) operation);
        buffer.writeByte(0);
        packet.setEventId((byte) ++eventId);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildFalse(int eventId, int operation) {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte((byte) operation);
        buffer.writeByte(1);
        packet.setEventId((byte) ++eventId);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildGcodeResponse() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeInt(0);
        buffer.writeString("", UTF_8);
        packet.setEventId((byte) 0x02);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildPrintGcodeResponse() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeInt(0);
        buffer.writeString("", UTF_8);
        packet.setEventId((byte) 0x04);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildPrintGcodeLine() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x08);
        buffer.writeByte((byte) 0x00);
        buffer.writeByte((byte) 0x00);
        buffer.writeInt(1000);
        packet.setEventId((byte) 0x08);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildMachineSize() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x14);
        buffer.writeByte(0);
        buffer.writeByte(IMachine.DeprecatedMachineModel.MACHINE_MODEL_SNAPMAKER_A400);
        // size
        buffer.writeInt(0);
        buffer.writeInt(0);
        buffer.writeInt(0);
        // homeDir
        buffer.writeInt(0);
        buffer.writeInt(0);
        buffer.writeInt(0);
        // stepperDir
        buffer.writeInt(0);
        buffer.writeInt(0);
        buffer.writeInt(0);
        // homeOffset
        buffer.writeInt(0);
        buffer.writeInt(0);
        buffer.writeInt(0);
        packet.setEventId((byte) 0x0a);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildEnclosureStatus() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x01);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(0x01);
        packet.setEventId((byte) 0x12);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildAirPurifierStatus() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x09);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        packet.setEventId((byte) 0x12);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }


    public SSTPPacket buildMachineType() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x01);
        buffer.writeInt(mMachineType);
        packet.setEventId(SSTPPacket.MOCK_RESPONSE_EVENT_ID);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public SSTPPacket buildRotaryModuleStatus() {
        SSTPPacket packet = SSTPPacket.create();
        Buffer buffer = new Buffer();
        buffer.writeByte(0x08);
        buffer.writeByte(mRotaryModuleStatus);
        packet.setEventId(SSTPPacket.ADD_ON_OPERATION_RESPONSE_EVENT_ID);
        packet.setContent(buffer.readByteArray());
        packet.build();
        return packet;
    }

    public int getMachineType() {
        return mMachineType;
    }

    public void setMachineType(int machineType) {
        mMachineType = machineType;
    }

    public int getHeadType() {
        return mHeadType;
    }

    public void setHeadType(int headType) {
        mHeadType = headType;
    }

    public byte getCoordinateAligned() {
        return mCoordinateAligned;
    }

    public void setCoordinateAligned(byte coordinateAligned) {
        mCoordinateAligned = coordinateAligned;
    }

    public void setHomed(byte homed) {
        Disposable sub = Observable.timer(10, TimeUnit.SECONDS)
                .subscribe(time -> {
                    mHomed = homed;
                });
        disposables.add(sub);

    }

    public void setHeadTargetTemp(short headTargetTemp) {
        mLeftHeadTargetTemp = headTargetTemp;
        mRightHeadTargetTemp = headTargetTemp;
    }

    public byte getRotaryModuleStatus() {
        return mRotaryModuleStatus;
    }

    public void setRotaryModuleStatus(byte rotaryModuleStatus) {
        mRotaryModuleStatus = rotaryModuleStatus;
    }
}
