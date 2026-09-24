package fabscreen.platform.core.ui.common.a400jogpanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import java.util.List;

import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.data.MoveController.Direction;
import fabscreen.platform.core.ui.view.CustomSteeringView;
import fabscreen.platform.core.ui.view.SteeringView;

public class JogPanelViewPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int XYZ_TYPE = 101;
    public static final int B_TYPE = 102;
    private final List<Integer> mList;

    // Moving state of panel, NONE means not moving.
    private Direction mMovingDirection;

    private OnItemClickListener mListener;

    public JogPanelViewPagerAdapter(List<Integer> list) {
        mList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == XYZ_TYPE) {
            View xzyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vp_control_xyz, parent, false);
            return new XYZViewHolder(xzyView);
        } else {
            View bView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vp_control_b, parent, false);
            return new BViewHolder(bView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof XYZViewHolder) {
            XYZViewHolder xyzViewHolder = (XYZViewHolder) holder;
            if (mMovingDirection != null) {
                xyzViewHolder.refreshViewByMovingState(mMovingDirection);
            }

            xyzViewHolder.mCsvXy.setOnDirectionClickedListener(direction -> {
                if (mListener == null) return;
                switch (direction) {
                    case SteeringView.DIRECTION_UP:
                        mListener.onItemClick(Direction.FORWARD);
                        break;
                    case SteeringView.DIRECTION_DOWN:
                        mListener.onItemClick(Direction.BACKWARD);
                        break;
                    case SteeringView.DIRECTION_LEFT:
                        mListener.onItemClick(Direction.LEFT);
                        break;
                    case SteeringView.DIRECTION_RIGHT:
                        mListener.onItemClick(Direction.RIGHT);
                        break;
                }
            });

            xyzViewHolder.mBtnControlZPlus.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(Direction.UP);
                    Logger.d("upupup");
                }
            });
            xyzViewHolder.mBtnControlZMinus.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(Direction.DOWN);
                    Logger.d("downdowndown");
                }
            });
        } else if (holder instanceof BViewHolder) {
            BViewHolder bViewHolder = (BViewHolder) holder;
            if (mMovingDirection != null) {
                bViewHolder.refreshViewByMovingState(mMovingDirection);
            }
            bViewHolder.mBtnControlLeft.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(Direction.B_COUNTERCLOCKWISE);
                }
            });

            bViewHolder.mBtnControlRight.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(Direction.B_CLOCKWISE);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void onMachineMoving(Direction direction) {
        mMovingDirection = direction;
        // TODO: 2022/6/24 refactor to notifyItemRangeChanged(0, getItemCount()) later
        notifyDataSetChanged();
        // notifyItemRangeChanged(0, getItemCount());
    }

    public static class XYZViewHolder extends RecyclerView.ViewHolder {
        Button mBtnControlZPlus;
        Button mBtnControlZMinus;
        CustomSteeringView mCsvXy;

        public XYZViewHolder(@NonNull View itemView) {
            super(itemView);
            mBtnControlZPlus = itemView.findViewById(R.id.btn_control_panel_z_plus);
            mBtnControlZMinus = itemView.findViewById(R.id.btn_control_panel_z_minus);
            mCsvXy = itemView.findViewById(R.id.csv_control_panel_xy);
            mCsvXy.setSkin(CustomSteeringView.SteeringViewSkin.STEERING_VIEW_SKIN_A400);
        }

        public void refreshViewByMovingState(Direction direction) {
            if (direction == null) return;
            Logger.d("refresh moving state: direction=%s", direction);
            mCsvXy.setEnabled(false);
            mBtnControlZPlus.setEnabled(false);
            mBtnControlZMinus.setEnabled(false);
            switch (direction) {
                case FORWARD:
                    mCsvXy.setFrozenWithDirection(true, SteeringView.DIRECTION_UP);
                    break;
                case BACKWARD:
                    mCsvXy.setFrozenWithDirection(true, SteeringView.DIRECTION_DOWN);
                    break;
                case LEFT:
                    mCsvXy.setFrozenWithDirection(true, SteeringView.DIRECTION_LEFT);
                    break;
                case RIGHT:
                    mCsvXy.setFrozenWithDirection(true, SteeringView.DIRECTION_RIGHT);
                    break;
                case UP:
                    mCsvXy.setEnabled(false);
                    mBtnControlZPlus.setBackgroundResource(R.drawable.ic_a400_control_up_activated);
                    break;
                case DOWN:
                    mCsvXy.setEnabled(false);
                    mBtnControlZMinus.setBackgroundResource(R.drawable.ic_a400_control_down_activated);
                    break;
                case ALL:
                    mCsvXy.setEnabled(false);
                    mBtnControlZPlus.setEnabled(false);
                    mBtnControlZMinus.setEnabled(false);
                    break;
                case NONE:
                    mCsvXy.setFrozenWithDirection(false, SteeringView.DRECTION_IDLE);
                    mCsvXy.setEnabled(true);
                    mBtnControlZPlus.setEnabled(true);
                    mBtnControlZMinus.setEnabled(true);
                    mBtnControlZPlus.setBackgroundResource(R.drawable.control_btn_up_background);
                    mBtnControlZMinus.setBackgroundResource(R.drawable.control_btn_down_background);
                    break;
                default:
                    mCsvXy.setEnabled(false);
                    mBtnControlZPlus.setBackgroundResource(R.drawable.control_btn_up_background);
                    mBtnControlZMinus.setBackgroundResource(R.drawable.control_btn_down_background);
                    mBtnControlZPlus.setEnabled(false);
                    mBtnControlZMinus.setEnabled(false);
                    break;
            }
        }
    }

    public static class BViewHolder extends RecyclerView.ViewHolder {
        public Button mBtnControlLeft;
        public Button mBtnControlRight;

        public BViewHolder(@NonNull View itemView) {
            super(itemView);
            mBtnControlLeft = itemView.findViewById(R.id.btn_control_b_left);
            mBtnControlRight = itemView.findViewById(R.id.btn_control_b_right);
        }

        public void refreshViewByMovingState(Direction movingDirection) {
            if (movingDirection == null) return;
            switch (movingDirection) {
                case B_CLOCKWISE:
                    mBtnControlRight.setBackgroundResource(R.drawable.ic_control_b_right_active);
                    break;
                case B_COUNTERCLOCKWISE:
                    mBtnControlLeft.setBackgroundResource(R.drawable.ic_control_b_left_active);
                    break;
                case NONE:
                    mBtnControlLeft.setBackgroundResource(R.drawable.select_control_b_left_bg);
                    mBtnControlRight.setBackgroundResource(R.drawable.select_control_b_right_bg);
                    mBtnControlLeft.setEnabled(true);
                    mBtnControlRight.setEnabled(true);
                    break;
                default:
                    mBtnControlLeft.setBackgroundResource(R.drawable.select_control_b_left_bg);
                    mBtnControlRight.setBackgroundResource(R.drawable.select_control_b_right_bg);
                    mBtnControlLeft.setEnabled(false);
                    mBtnControlRight.setEnabled(false);
                    break;
            }
        }
    }


    public interface OnItemClickListener {
        void onItemClick(Direction direction);
    }
}
