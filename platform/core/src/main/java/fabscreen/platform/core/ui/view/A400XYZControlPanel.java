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
import co.ceryle.segmentedbutton.SegmentedButton;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

import static fabscreen.platform.core.ui.data.MoveController.Direction;

@SuppressLint("NonConstantResourceId")
public class A400XYZControlPanel extends ConstraintLayout {

    @BindView(R2.id.sbg_control_steps)
    SegmentedButtonGroup mSbgControlSteps;
    @BindView(R2.id.sv_control_panel_xy)
    SteeringView mSvControlXY;
    @BindView(R2.id.btn_calibration_move_up)
    Button mBtnMoveUp;
    @BindView(R2.id.btn_calibration_move_down)
    Button mBtnMoveDown;
    @BindView(R2.id.sb_1)
    SegmentedButton mSb1;
    @BindView(R2.id.sb_2)
    SegmentedButton mSb2;
    @BindView(R2.id.sb_3)
    SegmentedButton mSb3;
    @BindView(R2.id.sb_4)
    SegmentedButton mSb4;


    private OnDirectionClickListener mDirectionListener;
    private float[] mWidths = {0.1f, 1f, 10f, 100f};
    private float mStepWidth = mWidths[1];

    public A400XYZControlPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_a400_control_panel_xyz_axes_mini, this);
        ButterKnife.bind(this, view);
        mSbgControlSteps.setPosition(1, false);
        mSbgControlSteps.setOnPositionChangedListener(position -> {
            if (mWidths.length < 4) return;
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

    public A400XYZControlPanel setStepWidths(float width0, float width1, float width2, float width3) {
        mWidths[0] = width0;
        mWidths[1] = width1;
        mWidths[2] = width2;
        mWidths[3] = width3;
        return this;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mSbgControlSteps.setEnabled(enabled);
        mSvControlXY.setEnabled(enabled);
        mBtnMoveUp.setEnabled(enabled);
        mBtnMoveDown.setEnabled(enabled);
    }

    @OnClick({R2.id.btn_calibration_move_up, R2.id.btn_calibration_move_down})
    void onPanelItemClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_calibration_move_up) {
            mDirectionListener.onDirectionClicked(Direction.UP, mStepWidth);
        } else if (id == R.id.btn_calibration_move_down) {
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
