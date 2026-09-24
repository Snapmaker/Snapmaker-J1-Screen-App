package fabscreen.platform.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fabscreen.platform.base.R;
import fabscreen.platform.base.helper.PackageHelper;
import fabscreen.platform.base.lib.ShellCommander;
import fabscreen.platform.base.lib.update.FabPackageManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstallProcessReceiver extends BroadcastReceiver {
    private static final String TAG = "InstallProcessReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getStringExtra("URL");
        String operation = intent.getStringExtra("OPERATION");
        String packageName = intent.getStringExtra("PACKAGE_NAME");
        Logger.d("Received broadcast, op: %1$s, pkg: %2$s, url: %3$s", operation, packageName, url);
        if (operation == null) return;

        switch (operation) {
            case "update": {
                if (url != null) {
                    downloadAndInstall(context, url, packageName);
                } else {
                    installUpdatingPackage(context);
                }
                break;
            }
            case "factory_reset": {
                factoryReset(packageName);
                break;
            }
            case "local_file": {
                startUpdatingApp(context, packageName, "updating");
                install(url, packageName);
                break;
            }
        }
    }

    private void startUpdatingApp(Context context, String pkgName, String operation) {
        if (context != null) {
            Intent updateIntent = context.getPackageManager().getLaunchIntentForPackage("com.snapmaker.updating");
            if (updateIntent != null) {
                updateIntent.putExtra("package_name", pkgName);
                updateIntent.putExtra("operation", operation);
                if (context.getPackageName().equals("com.snapmaker.j1")) {
                    updateIntent.putExtra("string_updating_title", context.getString(R.string.j1_updating_title));
                    updateIntent.putExtra("string_updating_desc", context.getString(R.string.j1_updating_msg));
                }
                updateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(updateIntent);
            }
        }
    }

    private void installUpdatingPackage(Context context) {
        final String updatingPkgName = PackageHelper.getUpdatingAppPackageName();

        // If update app is installed, then skip installation
        PackageInfo packageInfo = PackageHelper.getUpdatingAppPackageInfo(context);
        if (packageInfo != null && packageInfo.versionName.compareTo("2.1") >= 0) {
            Log.d(TAG, updatingPkgName + " " + packageInfo.versionName + " is already installed");
            return;
        }

        if (context.getPackageName().equals("com.snapmaker.fabscreena400")) {
            Logger.d("Uninstalling updating...");
            FabPackageManager.uninstall(context, updatingPkgName, () -> {
                Logger.d("Old updating uninstalled, installing new updating...");
                FabPackageManager.install(context, context.getResources().openRawResource(R.raw.fabscreen_updating_2_1));
            });
        } else {
            // 5 inch screen can't do uninstall because of UID not match.
            Logger.d("Installing updating...");
            FabPackageManager.install(context, context.getResources().openRawResource(R.raw.fabscreen_updating_2_1));
        }
    }

    private void downloadAndInstall(Context context, String url, String pkgName) {
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                // Download failed
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                InputStream is = null;
                FileOutputStream fos = null;

                try {
                    is = response.body().byteStream();

                    File file = new File(context.getFilesDir(), "download.apk");
                    fos = new FileOutputStream(file);

                    byte[] buf = new byte[1024];
                    int n;
                    while ((n = is.read(buf)) != -1) {
                        fos.write(buf, 0, n);
                    }

                    fos.flush();
                    fos.close();

                    // Launch updating app when updating
                    if (PackageHelper.isUpdatingAppInstalled(context)) {
                        startUpdatingApp(context, pkgName, "updating");
                    }

                    // Install
                    install(file.getAbsolutePath(), null);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        });
    }

    private void factoryReset(String packageName) {
        // usage: pm uninstall -k [packageName]
        String[] args = {"pm", "uninstall", "-k", packageName};
        ShellCommander.run(args);
    }

    private void install(String apkPath, String packageName) {
        // a350 compat
        if (packageName == null) {
            packageName = "com.snapmaker.fabscreen";
        }

        File file = new File(apkPath);
        if (apkPath.length() == 0 || file.length() <= 0 || !file.exists() || !file.isFile()) {
            Log.e(TAG, "file read fail");
            return;
        }

        String[] args = {"pm", "install", "-r", "-i", packageName, "--user", "0", apkPath};
        ShellCommander.run(args);

        Logger.d("Install process finish.");
    }
}
