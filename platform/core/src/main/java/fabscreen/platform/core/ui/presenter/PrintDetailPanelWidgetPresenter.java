package fabscreen.platform.core.ui.presenter;


import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

public class PrintDetailPanelWidgetPresenter {
    // 3dp
    @BindView(R2.id.tv_widget_nozzle_temp_value)
    TextView mTvNozzleTemp;

    @BindView(R2.id.tv_widget_heated_bed_temp_value)
    TextView mTvHeatedBedTemp;

    @BindView(R2.id.tv_widget_3dp_work_speed_value)
    TextView mTv3DPWorkSpeed;

    @BindView(R2.id.tv_widget_3dp_estimated_time_title)
    TextView mTv3DPEstimatedTimeTitle;
    @BindView(R2.id.tv_widget_3dp_estimated_time_value)
    TextView mTv3DPEstimatedTime;

    // laser
    @BindView(R2.id.tv_widget_laser_power_value)
    TextView mTvLaserPower;

    @BindView(R2.id.tv_widget_laser_work_speed_value)
    TextView mTvLaserWorkSpeed;

    @BindView(R2.id.tv_widget_laser_estimated_time_title)
    TextView mTvLaserEstimatedTimeTitle;
    @BindView(R2.id.tv_widget_laser_estimated_time_value)
    TextView mTvLaserEstimatedTime;

    // cnc
    @BindView(R2.id.tv_widget_spindle_speed_value)
    TextView mTvSpindleSpeed;

    @BindView(R2.id.tv_widget_cnc_work_speed_value)
    TextView mTvCNCWorkSpeed;

    @BindView(R2.id.tv_widget_cnc_estimated_time_title)
    TextView mTvCNCEstimatedTimeTitle;
    @BindView(R2.id.tv_widget_cnc_estimated_time_value)
    TextView mTvCNCEstimatedTime;

    public void bind(View view) {
        ButterKnife.bind(this, view);
    }

    public void setFeedRate(double feedRate) {
        String text = String.format(Locale.getDefault(), "%.0f mm/min", feedRate);
        mTv3DPWorkSpeed.setText(text);
        mTvLaserWorkSpeed.setText(text);
        mTvCNCWorkSpeed.setText(text);
    }

    public void setFeedRatePerSecond(double feedRate) {
        String text = String.format(Locale.getDefault(), "%.0f mm/s", feedRate);
        mTv3DPWorkSpeed.setText(text);
        mTvLaserWorkSpeed.setText(text);
        mTvCNCWorkSpeed.setText(text);
    }

    public void setWorkSpeedPercentage(double speed) {
        String text = String.format(Locale.getDefault(), "%.0f%%", speed);
        mTv3DPWorkSpeed.setText(text);
        mTvLaserWorkSpeed.setText(text);
        mTvCNCWorkSpeed.setText(text);
    }

    public void setEstimatedTime(double time) {
        String text = BaseApplication.formatTime(time);
        mTv3DPEstimatedTime.setText(text);
        mTvLaserEstimatedTime.setText(text);
        mTvCNCEstimatedTime.setText(text);
    }

    public void useElapsedTime() {
        mTv3DPEstimatedTimeTitle.setText(R.string.print_elapsed_time);
        mTvLaserEstimatedTimeTitle.setText(R.string.print_elapsed_time);
        mTvCNCEstimatedTimeTitle.setText(R.string.print_elapsed_time);
    }

    public void setNozzleTemp(double target) {
        mTvNozzleTemp.setText(String.format(Locale.getDefault(), "%.0f°C", target));
    }

    public void setNozzleTemp(double current, double target) {
        mTvNozzleTemp.setText(String.format(Locale.getDefault(), "%.0f/%.0f°C", current, target));
    }

    public void setHeatedBedTemp(double target) {
        mTvHeatedBedTemp.setText(String.format(Locale.getDefault(), "%.0f°C", target));
    }

    public void setHeatedBedTemp(double current, double target) {
        mTvHeatedBedTemp.setText(String.format(Locale.getDefault(), "%.0f/%.0f°C", current, target));
    }

    public void setLaserPower(double power) {
        mTvLaserPower.setText(String.format(Locale.getDefault(), "%.1f%%", power));
    }

    public void setSpindleSpeed(double speed) {
        mTvSpindleSpeed.setText(String.format(Locale.getDefault(), "%.0f RPM", speed));
    }
}
