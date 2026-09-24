package fabscreen.platform.base.lib;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class ShellCommander {
    public static void run(String[] args) {
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> runOnIO(args));
    }

    private static void runOnIO(String[] args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();

        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String result;
            while ((result = successResult.readLine()) != null) {
                successMsg.append(result);
                successMsg.append("\n");
            }
            while ((result = errorResult.readLine()) != null) {
                errorMsg.append(result);
                errorMsg.append("\n");
            }
            Logger.d("ShellTerminal: success: %s", successMsg);
            Logger.d("ShellTerminal: error: %s", errorMsg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
    }
}
