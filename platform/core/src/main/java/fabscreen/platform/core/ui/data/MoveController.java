package fabscreen.platform.core.ui.data;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import io.reactivex.Observable;


public class MoveController {

    private final MachineController mMachineController;

    private MoveController() {
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineController = machine.getMachineController();
    }

    public static MoveController getInstance() {
        return XYZMoveControllerHolder.INSTANCE;
    }

    @Deprecated
    public Observable<ResponseStructure> moveByStep(Direction direction, float stepWidth) {
        return stepToPosition(direction, stepWidth);
    }

    public Observable<ResponseStructure> stepToPosition(Direction direction, float stepWidth) {
        return stepToPosition(direction, stepWidth, 1800);
    }

    public Observable<ResponseStructure> stepToPosition(Direction direction, float stepWidth, int feedrate) {
        // Get latest position and move to new position.
        return mMachineController.getCurrentCoordinateObservable().flatMap(vector -> {
            // Assemble xyz temp vector, we assume that every move should locate single position(xyz coordinate),
            // not X1 plus X2 with two location.
            Vector tempVector = new Vector();
            if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                tempVector.setX(vector.getX());
            } else {
                tempVector.setX2(vector.getX2());
            }
            tempVector.setY(vector.getY());
            tempVector.setZ(vector.getZ());
            // check step change.
            switch (direction) {
                case FORWARD:
                    tempVector.setY(vector.getY() - stepWidth);
                    break;
                case BACKWARD:
                    tempVector.setY(vector.getY() + stepWidth);
                    break;
                case LEFT:
                    tempVector.setX(vector.getX() - stepWidth);
                    break;
                case RIGHT:
                    tempVector.setX(vector.getX() + stepWidth);
                    break;
                case UP:
                    tempVector.setZ(vector.getZ() + stepWidth);
                    break;
                case DOWN:
                    tempVector.setZ(vector.getZ() - stepWidth);
                    break;
                case B_CLOCKWISE:
                    tempVector.setB(vector.getB() + stepWidth);
                    break;
                case B_COUNTERCLOCKWISE:
                    tempVector.setB(vector.getB() - stepWidth);
                    break;
                case X2_LEFT:
                    tempVector.setX2(vector.getX2() - stepWidth);
                    break;
                case X2_RIGHT:
                    tempVector.setX2(vector.getX2() + stepWidth);
                    break;
                default:
                    break;
            }

            return mMachineController.gotoAbsolutePosition(tempVector, feedrate);
        });
    }

    public Observable<ResponseStructure> stepToPositionAndLimit(Direction direction, float stepWidth, float limit) {
        return stepToPositionAndLimit(direction, stepWidth, limit, 1800);
    }

    public Observable<ResponseStructure> stepToPositionAndLimit(Direction direction, float stepWidth, float limit, int feedrate) {
        // Get latest position and move to new position.
        return mMachineController.getCurrentCoordinateObservable().flatMap(vector -> {
            // Assemble xyz temp vector, we assume that every move should locate single position(xyz coordinate),
            // not X1 plus X2 with two location.
            Vector tempVector = new Vector();
            if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                tempVector.setX(vector.getX());
            } else {
                tempVector.setX2(vector.getX2());
            }
            tempVector.setY(vector.getY());
            tempVector.setZ(vector.getZ());
            float stepWidth1 = stepWidth;
            // check step change.
            switch (direction) {
                case FORWARD:
//                    stepWidth1 = vector.getY() - stepWidth < limit ? vector.getY() - limit : stepWidth;
                    tempVector.setY(vector.getY() - stepWidth1);
                    break;
                case BACKWARD:
//                    stepWidth1 = vector.getY() + stepWidth > limit ? limit - vector.getY() : stepWidth;
                    tempVector.setY(vector.getY() + stepWidth1);
                    break;
                case LEFT:
//                    stepWidth1 =  vector.getX() - stepWidth < limit ? vector.getX() - limit : stepWidth;
                    tempVector.setX(vector.getX() - stepWidth1);
                    break;
                case RIGHT:
//                    stepWidth1 = vector.getX() + stepWidth > limit ? limit - vector.getX() : stepWidth;
                    tempVector.setX(vector.getX() + stepWidth1);
                    break;
                case UP:
//                    stepWidth1 = vector.getZ() + stepWidth > limit ? limit - vector.getZ() : stepWidth;
                    tempVector.setZ(vector.getZ() + stepWidth1);
                    break;
                case DOWN:
                    stepWidth1 = vector.getZ() - stepWidth < limit ? vector.getZ() - limit : stepWidth;
                    tempVector.setZ(vector.getZ() - stepWidth1);
                    break;
                case B_CLOCKWISE:
//                    stepWidth1 = vector.getB() + stepWidth > limit ? limit - vector.getB() : stepWidth;
                    tempVector.setB(vector.getB() + stepWidth1);
                    break;
                case B_COUNTERCLOCKWISE:
//                    stepWidth1 =  vector.getB() - stepWidth < limit ? vector.getB() - limit : stepWidth;
                    tempVector.setB(vector.getB() - stepWidth1);
                    break;
                case X2_LEFT:
//                    stepWidth1 =  vector.getX2() - stepWidth < limit ? vector.getX2() - limit : stepWidth;
                    tempVector.setX2(vector.getX2() - stepWidth1);
                    break;
                case X2_RIGHT:
//                    stepWidth1 = vector.getX2() + stepWidth > limit ? limit - vector.getX2() : stepWidth;
                    tempVector.setX2(vector.getX2() + stepWidth1);
                    break;
                default:
                    break;
            }

            return mMachineController.gotoAbsolutePosition(tempVector, feedrate);
        });
    }


    public enum Direction {
        NONE, ALL, FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN, B_CLOCKWISE, B_COUNTERCLOCKWISE, X2_LEFT, X2_RIGHT;

        public static boolean isRotary(Direction direction) {
            return direction == B_CLOCKWISE || direction == B_COUNTERCLOCKWISE;
        }
    }

    private static class XYZMoveControllerHolder {
        private static final MoveController INSTANCE = new MoveController();
    }
}
