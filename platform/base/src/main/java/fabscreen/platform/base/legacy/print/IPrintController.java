package fabscreen.platform.base.legacy.print;

import java.io.InputStream;

import fabscreen.platform.base.legacy.ISlaveComputer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.lib.print.TickCounter;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

//import fabscreen.libraries.core.connection.fabpacket.sstp.SSTPPacketContent;

@Deprecated
public interface IPrintController {
    int STATE_IDLE = 0;
    int STATE_PRINTING = 1;
    int STATE_PAUSED = 2;
    int STATE_COMPLETED = 3;

    void reset();

    void start();

    void pause();

    void resume();

    void stop();

    void recover();

    void finish();

    void setActionDisposable(Disposable disposable);

    Observable<Integer> getPrintStateObservable();

    void setListener(PrintListener listener);

    int getProgressCount();

    float getProgress();

    void setFile(IFile file);

    ISlaveComputer getSlaveComputer();

    /**
     * Pass in the Gcode file inputStream.
     * Apk built-in print files are entered as input streams
     * Network input streams are supported, but the correct number of lines cannot be displayed
     *
     * @param inputStream
     */
    void setInputStream(InputStream inputStream);

    void pauseOnFilamentUsedOut();

    void pauseOnEnclosureDoorDetected();

    void onEmergencyStop();

    boolean getOverrideNozzleTemperatureDirty();

    float getOverrideNozzleTemperature();

    void setOverrideNozzleTemperature(float temp);

    boolean getOverrideHeatedBedTemperatureDirty();

    float getOverrideHeatedBedTemperature();

    void setOverrideHeatedBedTemperature(float temp);

    boolean getOverrideLaserPowerDirty();

    float getOverrideLaserPower();

    void setOverrideLaserPower(float power);

    boolean getRecoveryFlag();

    float getOverrideInitialHeatedBedTemperature();

    void setOverrideInitialHeatedBedTemperature(float temp);

    void setPowerOutageFlag(boolean flag);

    Observable<Boolean> getResumeObservable();

    Integer getPrintState();

    boolean getInitialM190Flag();

    float getOverrideFeedRate();

    void setOverrideFeedRate(float feedRate);

    float getOverrideInitialNozzleTemperature();

    void setOverrideInitialNozzleTemperature(float temp);

    boolean getInitialM109Flag();

    float getOverrideZOffset();

    void setOverrideZOffset(float zOffset);

    ModelBoundary getModelBoundary();

    void setModelBoundary(ModelBoundary boundary);

    void setResume();

    void disposeAll();

    TickCounter getTickCounter();

    Observable<Integer> getPauseState();

    void masterPause();

    Observable<SSTPPacketContent.HeaderSecurity> getHeaderSecurityStatus();

    int getTotalLines();

    void setTotalLines(int lines);
}
