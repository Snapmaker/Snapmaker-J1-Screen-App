package fabscreen.platform.core.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

import static fabscreen.platform.core.ui.data.MoveController.Direction;

@SuppressLint("NonConstantResourceId")
public class XYZControlPanel extends ConstraintLayout {

    @BindView(R2.id.sbg_control_steps)
    SegmentedButtonGroup mSbgControlSteps;
    @BindView(R2.id.sv_control_panel_xy)
    SteeringView mSvControlXY;
    @BindView(R2.id.btn_control_panel_z_plus)
    Button mBtnZPlus;
    @BindView(R2.id.btn_control_panel_z_minus)
    Button mBtnZMinus;
    private OnDirectionClickListener mDirectionListener;
    private float[] mWidths = {0.1f, 1f, 10f};
    private float mStepWidth = mWidths[1];

    public XYZControlPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_control_panel_xyz_axes_mini, this);
        ButterKnife.bind(this, view);
        mSbgControlSteps.setPosition(1, false);
        mSbgControlSteps.setOnPositionChangedListener(position -> {
            if (mWidths.length < 3) return;
            mStepWidth = mWidths[position];
        });
        mSvControlXY.setOnDirectionClickedListener(state -> {
            switch (state) {
                case SteeringView.DIRECTION_UP:
                    mDirectionListener.onDirectionClicked(Direction.FORWARD, mStepWidth);
                    break;
                case SteeringView.DIRECTION_DOWN:
                    mDirectionListener.onDirectionClicked(Direction.BACKWARD, mStepWidth);
                    break;
                case SteeringView.DIRECTION_LEFT:
                    mDirectionListener.onDirectionClicked(Direction.LEFT, mStepWidth);
                    break;
                case SteeringView.DIRECTION_RIGHT:
                    mDirectionListener.onDirectionClicked(Direction.RIGHT, mStepWidth);
                    break;
            }
        });
    }

    public XYZControlPanel setStepWidths(float width0, float width1, float width2) {
        mWidths[0] = width0;
        mWidths[1] = width1;
        mWidths[2] = width2;
        return this;
    }

    public void setXYEnabled(boolean enabled) {
        mSvControlXY.setEnabled(enabled);
    }

    public void setZEnabled(boolean enabled) {
        mSbgControlSteps.setEnabled(enabled);
        mBtnZPlus.setEnabled(enabled);
        mBtnZMinus.setEnabled(enabled);
    }

    @OnClick({R2.id.btn_control_panel_z_plus, R2.id.btn_control_panel_z_minus})
    void onPanelItemClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_control_panel_z_plus) {
            mDirectionListener.onDirectionClicked(Direction.UP, mStepWidth);
        } else if (id == R.id.btn_control_panel_z_minus) {
            mDirectionListener.onDirectionClicked(Direction.DOWN, mStepWidth);
        }
    }

    public void setOnDirectionClickListener(OnDirectionClickListener listener) {
        mDirectionListener = listener;
    }

    public interface OnDirectionClickListener {
        void onDirectionClicked(Direction direction, float stepWidth);
    }
}
