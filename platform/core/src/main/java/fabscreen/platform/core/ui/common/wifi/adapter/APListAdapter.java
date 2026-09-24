package fabscreen.platform.core.ui.common.wifi.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.ui.common.wifi.viewholder.header.A400HeadDividerViewHolder;
import fabscreen.platform.core.ui.common.wifi.viewholder.ap.APViewHolder;
import fabscreen.platform.core.ui.common.wifi.viewholder.header.J1HeadDividerViewHolder;

public abstract class APListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<AccessPoint> mAps;
    protected OnItemClickListener mListener;

    public static final int DIVIDER = 0;
    public static final int EMPTY = 1;
    public static final int AP = 2;
    private boolean mShowScanning;
    private boolean mShowEmpty;
    private boolean mSearchOff;

    public APListAdapter(List<AccessPoint> aps) {
        mAps = aps;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DIVIDER) {
            return getHeaderDividerViewHolder(parent);
        } else if (viewType == EMPTY) {
            return getEmptyAPViewHolder(parent);
        } else {
            return getAPViewHolder(parent);
        }
    }

    protected abstract RecyclerView.ViewHolder getEmptyAPViewHolder(ViewGroup parent);

    protected abstract RecyclerView.ViewHolder getHeaderDividerViewHolder(ViewGroup parent);

    protected abstract APViewHolder getAPViewHolder(ViewGroup parent);

    @Override
    public int getItemViewType(int position) {
        AccessPoint.ConnectState firstApState;
        if (mAps == null || mAps.isEmpty()) {
            if (mShowEmpty) {
                return position == 0 ? DIVIDER : EMPTY;
            } else {
                return DIVIDER;
            }
        } else {
            firstApState = mAps.get(0).getConnectState();
        }

        if (firstApState == AccessPoint.ConnectState.IDLE) {
            return position == 0 ? DIVIDER : AP;
        } else {
            // An ap is connecting/connected
            return position == 1 ? DIVIDER : AP;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof APViewHolder) {
            APViewHolder apViewHolder = (APViewHolder) holder;
            int apPosition = position == 0 ? 0 : position - 1;
            AccessPoint ap = mAps.get(apPosition);
            apViewHolder.bind(ap);
        } else if (holder instanceof J1HeadDividerViewHolder) {
            ((J1HeadDividerViewHolder) holder).bind(mShowScanning, mSearchOff);
        } else if (holder instanceof A400HeadDividerViewHolder) {
            ((A400HeadDividerViewHolder) holder).bind(mShowScanning, mSearchOff);
        }
    }

    @Override
    public int getItemCount() {
        // APs and a "Choose network" divider (and a empty view if scan result empty).
        if (mShowEmpty) {
            return 2;
        } else {
            return mAps.size() + 1;
        }
    }

    public void setShowScanning(boolean show) {
        mShowScanning = show;
        if (getItemCount() < 1) return;
        notifyItemRangeChanged(0, getItemCount() == 1 ? 1 : getItemCount());
    }

    public void setShowEmpty(boolean show) {
        // A400 doesn't have an empty view at this moment.
        if (this instanceof A400APListAdapter) return;

        mShowEmpty = show;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setSearchOff(boolean off) {
        mSearchOff = off;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(AccessPoint ap);
    }

    public static class EmptyAPViewHolder extends RecyclerView.ViewHolder {

        public EmptyAPViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
