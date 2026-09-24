package fabscreen.platform.base.lib.print;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.helper.StringHelper;

public class OnAirGcodeModifier {
    // overrides
    private float mOverrideFeedRate = -1;
    private boolean mOverrideFeedRateDirty = false;

    private float mOverrideZOffset = 0;
    private boolean mOverrideZOffsetDirty = false;

    private float mOverrideNozzleTemperature = -1;
    private boolean mOverrideNozzleTemperatureDirty = false;

    private float mOverrideInitialNozzleTemperature = -1;
    private boolean mInitialM104Marker = false;
    private boolean mInitialM109Marker = false;

    private float mOverrideHeatedBedTemperature = -1;
    private boolean mOverrideHeatedBedTemperatureDirty = false;

    private float mOverrideInitialHeatedBedTemperature = -1;
    private boolean mInitialM140Marker = false;
    private boolean mInitialM190Marker = false;

    private float mOverrideLaserPower = -1;
    private boolean mOverrideLaserPowerDirty = false;

    public OnAirGcodeModifier() {

    }

    public void reset() {
        Logger.d("Reset overrides.");

        mOverrideFeedRate = 100;
        mOverrideFeedRateDirty = false;

        mOverrideZOffset = 0;
        mOverrideZOffsetDirty = false;

        mOverrideNozzleTemperature = -1;
        mOverrideNozzleTemperatureDirty = false;

        mOverrideInitialNozzleTemperature = -1;
        mInitialM104Marker = false;
        mInitialM109Marker = false;

        mOverrideHeatedBedTemperature = -1;
        mOverrideHeatedBedTemperatureDirty = false;

        mOverrideInitialHeatedBedTemperature = -1;
        mInitialM140Marker = false;
        mInitialM190Marker = false;

        mOverrideLaserPower = -1;
        mOverrideLaserPowerDirty = false;
    }

    public float getOverrideFeedRate() {
        return mOverrideFeedRate;
    }

    public void setOverrideFeedRate(float feedRate) {
        if (feedRate != mOverrideFeedRate) {
            Logger.i("Override feed rate " + feedRate);
            mOverrideFeedRate = feedRate;
            mOverrideFeedRateDirty = true;
        }
    }

    public float getOverrideZOffset() {
        return mOverrideZOffset;
    }

    /**
     * Z offset will be applied to all Z coordinates in absolute positioning.
     */

    public void setOverrideZOffset(float zOffset) {
        if (mOverrideZOffset != zOffset) {
            Logger.i("Override z offset " + zOffset);
            mOverrideZOffset = zOffset;
            mOverrideZOffsetDirty = true;
        }
    }

    public float getOverrideNozzleTemperature() {
        return mOverrideNozzleTemperature;
    }

    public void setOverrideNozzleTemperature(float temp) {
        if (temp != mOverrideNozzleTemperature) {
            Logger.i("Override nozzle temperature " + temp);
            mOverrideNozzleTemperature = temp;
            mOverrideNozzleTemperatureDirty = true;
        }
    }

    public boolean getOverrideNozzleTemperatureDirty() {
        return mOverrideNozzleTemperatureDirty;
    }

    public float getOverrideInitialNozzleTemperature() {
        return mOverrideInitialNozzleTemperature;
    }

    public void setOverrideInitialNozzleTemperature(float temp) {
        if (temp != mOverrideInitialNozzleTemperature) {
            Logger.i("Override initial nozzle temperature " + temp);
            mOverrideInitialNozzleTemperature = temp;
        }
    }

    public boolean getInitialM109Marker() {
        return mInitialM109Marker;
    }

    public float getOverrideHeatedBedTemperature() {
        return mOverrideHeatedBedTemperature;
    }

    public void setOverrideHeatedBedTemperature(float temp) {
        if (temp != mOverrideHeatedBedTemperature) {
            Logger.i("Override heated bed temperature " + temp);
            mOverrideHeatedBedTemperature = temp;
            mOverrideHeatedBedTemperatureDirty = true;
        }
    }

    public boolean getOverrideHeatedBedTemperatureDirty() {
        return mOverrideHeatedBedTemperatureDirty;
    }

    public boolean getInitialM190Marker() {
        return mInitialM190Marker;
    }

    public float getOverrideInitialHeatedBedTemperature() {
        return mOverrideInitialHeatedBedTemperature;
    }

    public void setOverrideInitialHeatedBedTemperature(float temp) {
        if (temp != mOverrideInitialHeatedBedTemperature) {
            Logger.i("Override initial heated bed temperature " + temp);
            mOverrideInitialHeatedBedTemperature = temp;
        }
    }

    public float getOverrideLaserPower() {
        return mOverrideLaserPower;
    }

    public void setOverrideLaserPower(float power) {
        if (power != mOverrideLaserPower) {
            Logger.i("Override laser power " + power);
            mOverrideLaserPower = power;
            mOverrideLaserPowerDirty = true;
        }
    }

    public boolean getOverrideLaserPowerDirty() {
        return mOverrideLaserPowerDirty;
    }

    /*
    // TODO: Remove getModifyAction or implement this in a better way, comments temporary for safe.
    public Observable<Integer> getModifyAction() {
        if (mOverrideFeedRateDirty) {
            mOverrideFeedRateDirty = false;
            Logger.d("Setting feed rate to " + mOverrideFeedRate);
            return slaveComputer.requestAdjustSettingFeedRate(mOverrideFeedRate);
        }
        if (mOverrideZOffsetDirty) {
            float zOffset = mOverrideZOffset;
            mOverrideZOffsetDirty = false;
            Logger.d("Adjust z offset " + zOffset);
            return slaveComputer.requestAdjustSettingZOffset(zOffset);
        }
        if (mOverrideNozzleTemperatureDirty) {
            mOverrideNozzleTemperatureDirty = false;
            Logger.d("Setting nozzle temperature to " + mOverrideNozzleTemperature);
            return slaveComputer.requestAdjustSettingNozzleTemp(mOverrideNozzleTemperature);
        }
        if (mOverrideHeatedBedTemperatureDirty) {
            mOverrideHeatedBedTemperatureDirty = false;
            Logger.d("Setting heated bed temperature to " + mOverrideHeatedBedTemperature);
            return slaveComputer.requestAdjustSettingHeatedBedTemp(mOverrideHeatedBedTemperature);
        }
        if (mOverrideLaserPowerDirty) {
            mOverrideLaserPowerDirty = false;
            Logger.d("Setting laser power to " + mOverrideLaserPower);
            return slaveComputer.requestAdjustSettingLaserPower(mOverrideLaserPower);
        }

        return null;
    }
    */

    public String override(String line) {
        if (line.isEmpty()) {
            return line;
        }

        // Straight comment
        if (line.charAt(0) == ';') {
            return "";
        }
        return newOverride(line);
    }

    public boolean getIsOverride() {
        // FIXME：Gcode changes may occur due to historical requirements
        if (mInitialM104Marker) return true;
        if (mInitialM109Marker) return true;
        if (mInitialM140Marker) return true;
        return mInitialM190Marker;
    }

    public String newOverride(String line) {
        // Parse line to separate arguments
        final int length = line.length();

        int lineArgCount = 0;
        String[] lineArgs = new String[20];

        int pos = 0;
        while (pos < length) {
            while (pos < length && line.charAt(pos) == ' ') pos++;

            if (pos == length) break;
            if (line.charAt(pos) == ';') break;

            int start = pos;
            pos++;
            while (pos < length) {
                char c = line.charAt(pos);
                if (c == ' ' || c == ';' || StringHelper.isAlphabetic(c)) break;
                pos++;
            }

            lineArgs[lineArgCount++] = line.substring(start, pos);
        }

        if (lineArgCount == 0) return "";

        switch (lineArgs[0]) {
            case "M104": {
                if (!mInitialM104Marker) {
                    mInitialM104Marker = true;
                    if (mOverrideInitialNozzleTemperature != -1) {
                        Logger.d("Override M104 S" + mOverrideInitialNozzleTemperature);
                        return lineArgs[0] + " S" + mOverrideInitialNozzleTemperature;
                    }
                }
                break;
            }
            case "M109": {
                if (!mInitialM109Marker) {
                    mInitialM109Marker = true;
                    if (mOverrideInitialNozzleTemperature != -1) {
                        Logger.d("Override M109 S" + mOverrideInitialNozzleTemperature);
                        return lineArgs[0] + " S" + mOverrideInitialNozzleTemperature;
                    }
                }
                break;
            }
            case "M140": {
                if (!mInitialM140Marker) {
                    mInitialM140Marker = true;
                    if (mOverrideInitialHeatedBedTemperature != -1) {
                        Logger.d("Override M140 S" + mOverrideInitialHeatedBedTemperature);
                        return lineArgs[0] + " S" + mOverrideInitialHeatedBedTemperature;
                    }
                }
                break;
            }
            case "M190": {
                if (!mInitialM190Marker) {
                    mInitialM190Marker = true;
                    if (mOverrideInitialHeatedBedTemperature != -1) {
                        Logger.d("Override M190 S" + mOverrideInitialHeatedBedTemperature);
                        return lineArgs[0] + " S" + mOverrideInitialHeatedBedTemperature;
                    }
                }
                break;
            }
        }

        // unmodified
        return line;
    }
}
