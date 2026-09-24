package fabscreen.platform.base.service;

import android.app.Dialog;

public class ErrorMessage {
    private int errorNo;
    private boolean errorState;
    private Dialog dialog;

    public int getErrorNo() {
        return errorNo;
    }

    public void setErrorNo(int errorNo) {
        this.errorNo = errorNo;
    }

    public boolean isErrorState() {
        return errorState;
    }

    public void setErrorState(boolean errorState) {
        this.errorState = errorState;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }
}
