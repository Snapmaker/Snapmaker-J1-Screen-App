package fabscreen.platform.base.lib.update;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FabPackageManager {
    private static Callback mCallback;

    public static void install(Context context, InputStream is) {
        try {
            Logger.d("FabInstaller: installing...");
            PackageInstaller installer = context.getPackageManager().getPackageInstaller();

            int sessionId = installer.createSession(new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
            PackageInstaller.Session session = installer.openSession(sessionId);
            OutputStream os = session.openWrite("fab_install_session", 0, -1);
            BufferedInputStream bis = new BufferedInputStream(is);
            final int readSize = 1 << 16;
            final byte[] b = new byte[readSize];
            int n;
            while ((n = bis.read(b, 0, readSize)) != -1) {
                os.write(b, 0, n);
            }
            bis.close();
            is.close();
            os.close();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.snapmaker.fabscreen.PACKAGE_INSTALLED"), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
            session.close();

            Logger.d("FabInstaller: installed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void uninstall(Context context, String packageName, @NonNull Callback callback) {
        mCallback = callback;
        Intent intent = new Intent(context.getApplicationContext(), InstallReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        try {
            installer.uninstall(packageName, pendingIntent.getIntentSender());
        } catch (Exception e) {
            e.printStackTrace();
            mCallback = null;
        }
    }

    public interface Callback {
        void onSuccess();
    }

    public static class InstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Logger.d("InstallReceiver: intent: %s", intent);
            int extra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
            Logger.d("InstallReceiver: extra: %d", extra);
            if (mCallback != null) {
                mCallback.onSuccess();
                mCallback = null;
            }
        }
    }
}
