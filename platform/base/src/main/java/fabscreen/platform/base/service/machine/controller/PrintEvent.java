package fabscreen.platform.base.service.machine.controller;

public class PrintEvent {
    //    START_FAIL,
//    PAUSE_FAIL,
//    RESUME_FAIL,
//    POWER_LOSS_RESUME_FAIL,
//    STOP_SUCCESS,
//    STOP_FAIL,
//    FINISH_SUCCESS,
//    FINISH_FAIL
    PrintEventState printEventState;
    int ErrorCode;

    public PrintEvent(PrintEventState printEventState, int errorCode) {
        this.printEventState = printEventState;
        ErrorCode = errorCode;
    }

    public PrintEventState getPrintEventState() {
        return printEventState;
    }

    public int getErrorCode() {
        return ErrorCode;
    }

    @Override
    public String toString() {
        return "PrintEvent{" +
                "printEventState=" + printEventState +
                ", ErrorCode=" + ErrorCode +
                '}';
    }
}