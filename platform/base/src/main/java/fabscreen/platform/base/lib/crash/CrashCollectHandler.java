package fabscreen.platform.base.lib.crash;

import android.content.Context;

import fabscreen.platform.lib.LogHelper;

//import fabscreen.libraries.core.ui.base.BaseApplication;

//TO BE REFACTOR
@Deprecated
public class CrashCollectHandler implements Thread.UncaughtExceptionHandler {
    public static final String FABSCREEN_CRASH = "FABSCREEN_CRASH";
    private static CrashCollectHandler mInstance;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private boolean isCrashed;

    public static CrashCollectHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashCollectHandler();
        }
        return mInstance;
    }


    public void init(Context context) {
        mContext = context;
        isCrashed = false;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!isCrashed) {
            synchronized (this) {
                if (!isCrashed) {
                    isCrashed = true;
                    LogHelper.log(e);
                    mDefaultHandler.uncaughtException(t, e);
//                    ArrayList<Intent> intents = BaseApplication.getInstance().getFabScreenActivityManagement().getIntents();
//                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.snapmaker.updating");
//                    // If the intent is not empty, send the intent to restart
//                    if (intent != null) {
//                        intent.putExtra(PACKAGE_NAME,BaseApplication.getInstance().getPackageName());
//                        intent.putParcelableArrayListExtra(FABSCREEN_CRASH, intents);
//                        mContext.startActivity(intent);
//                    }
//                    // Shut down all activit, report an error message, shut down the current process, and terminate the JVM
//                    BaseApplication.getInstance().getFabScreenActivityManagement().removeAllActivity();
//                    FirebaseCrashlytics.getInstance().recordException(e);
//                    if (intent != null) {
//                        intent.putParcelableArrayListExtra(FABSCREEN_CRASH, intents);
//                        mContext.startActivity(intent);
//                    }
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(0);
                }
            }

        }
    }
}
