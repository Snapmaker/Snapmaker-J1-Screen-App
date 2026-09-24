package fabscreen.platform.base.lib.api;

public class ResultException extends Exception {
    private int mErrorCode;
    private String mMsg;
    private String mJsonData;

    public ResultException(int code, String msg, String jsonData) {
        super(msg);
        mMsg = msg;
        mErrorCode = code;
        mJsonData = jsonData;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public String getJsonData() {
        return mJsonData;
    }
}
