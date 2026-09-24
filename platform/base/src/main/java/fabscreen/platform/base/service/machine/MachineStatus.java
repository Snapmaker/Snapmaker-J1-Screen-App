package fabscreen.platform.base.service.machine;

import androidx.annotation.NonNull;

import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import io.reactivex.Observable;

// machine status can changed rapidly
public class MachineStatus {
    public static MachineStatus initialValue = new MachineStatus(false, false, false, 0, false, 0, null, null, null);
    public final boolean connected;
    public final boolean isHomed;
    public final boolean isHoming;
    public final int status; // mPrintStateSubject, STATE_IDLE etc...
    public final boolean isCoordinateAligned;
    public final int coordinateID;
    public final Vector currentPosition;
    public final Vector originOffset;
    // set last value to recognize if the property changed
    MachineStatus mLastStatus;

    public MachineStatus(boolean connected, boolean isHomed, boolean isHoming, int status, boolean isCoordinateAligned, int coordinateID, Vector currentPosition, Vector offsetPosition, MachineStatus lastStatus) {
        this.connected = connected;
        this.isHomed = isHomed;
        this.isHoming = isHoming;
        this.status = status;
        this.isCoordinateAligned = isCoordinateAligned;
        this.coordinateID = coordinateID;
        this.currentPosition = currentPosition;
        this.originOffset = offsetPosition;
        mLastStatus = lastStatus;
    }

    public Builder CreateBuilder() {
        return new Builder(this);
    }

    public void reset() {
        // TODO: 2022/5/13 reset values
    }

    public class Builder {
        // set last value to recognize if the property changed
        MachineStatus mLastStatus;
        private boolean connected;
        private boolean isHomed;
        private boolean isHoming;
        private int status; // mPrintStateSubject, STATE_IDLE etc...
        private boolean isCoordinateAligned;
        private int coordinateID;
        private Vector currentPosition;
        private Vector originOffset;
        private boolean isChange = false;

        public Builder(MachineStatus lastStatus) {
            connected = lastStatus.connected;
            isHomed = lastStatus.isHomed;
            isHoming = lastStatus.isHoming;
            status = lastStatus.status;
            isCoordinateAligned = lastStatus.isCoordinateAligned;
            coordinateID = lastStatus.coordinateID;
            currentPosition = lastStatus.currentPosition;
            originOffset = lastStatus.originOffset;
            mLastStatus = lastStatus;
        }

        public Builder changeConnected(Boolean newStatus) {
            if (mLastStatus.connected != newStatus) isChange = true;
            connected = newStatus;
            return this;
        }

        public Builder changeHomed(Boolean newStatus) {
            if (mLastStatus.isHomed != newStatus) isChange = true;
            isHomed = newStatus;
            return this;
        }

        public Builder changeHoming(Boolean newStatus) {
            if (mLastStatus.isHoming != newStatus) isChange = true;
            isHoming = newStatus;
            return this;
        }

        public Builder changeStatus(int newStatus) {
            if (mLastStatus.status != newStatus) isChange = true;
            status = newStatus;
            return this;
        }

        public Builder changeCoordinateAligned(Boolean newStatus) {
            if (mLastStatus.isCoordinateAligned != newStatus) isChange = true;
            isCoordinateAligned = newStatus;
            return this;
        }

        public Builder changeCoordinateId(int newStatus) {
            if (mLastStatus.coordinateID != newStatus) isChange = true;
            coordinateID = newStatus;
            return this;
        }

        public Builder changeCurrentPosition(@NonNull Vector newStatus) {
            if (mLastStatus.currentPosition != newStatus) isChange = true;
            currentPosition = newStatus;
            return this;
        }

        public Builder changeOriginOffset(Vector newStatus) {
            if (mLastStatus.originOffset != newStatus) isChange = true;
            originOffset = newStatus;
            return this;
        }

        public MachineStatus build() {
            if (isChange) {
                mLastStatus.mLastStatus = null;
                return new MachineStatus(connected, isHomed, isHoming, status, isCoordinateAligned, coordinateID, currentPosition, originOffset, mLastStatus);
            } else {
                return mLastStatus;
            }
        }
    }

    public MachineStatus getLastStatus() {
        return mLastStatus;
    }

    // FIXME: Remove this method later.
    public Observable<DeprecatedMachineInfo> requestMachineStatus() {
        return null;
    }

    @Override
    public String toString() {
        return "MachineStatus{" +
                "connected=" + connected +
                ", isHomed=" + isHomed +
                ", isHoming=" + isHoming +
                ", status=" + status +
                ", isCoordinateAligned=" + isCoordinateAligned +
                ", coordinateID=" + coordinateID +
                ", currentPosition=" + currentPosition +
                ", OffsetPosition=" + originOffset +
                ", mLastStatus=" + mLastStatus +
                '}';
    }
}
