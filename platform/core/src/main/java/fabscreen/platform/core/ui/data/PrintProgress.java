package fabscreen.platform.core.ui.data;

import android.content.Context;

import fabscreen.platform.core.R;

public class PrintProgress {
    public int remaining;
    public int percentage;

    public PrintProgress() {
    }

    public PrintProgress(int remaining, int percentage) {
        this.remaining = remaining;
        this.percentage = percentage;
    }

    public String formatTime(Context context) {
        int hour = (int) (remaining) / 3600;
        int minute = ((int) (remaining) % 3600) / 60;
        int second = ((int) (remaining) % 60);
        if (hour < 1) {
            return context.getString(R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return context.getString(R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    @Override
    public String toString() {
        return "PrintProgress{" +
                "remaining=" + remaining +
                ", percentage=" + percentage +
                '}';
    }
}