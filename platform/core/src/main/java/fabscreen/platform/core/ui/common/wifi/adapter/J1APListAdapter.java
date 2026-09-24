package fabscreen.platform.core.ui.common.wifi.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.common.wifi.viewholder.ap.APViewHolder;
import fabscreen.platform.core.ui.common.wifi.viewholder.ap.J1APViewHolder;
import fabscreen.platform.core.ui.common.wifi.viewholder.header.J1HeadDividerViewHolder;

public class J1APListAdapter extends APListAdapter {
    public J1APListAdapter(List<AccessPoint> aps) {
        super(aps);
    }

    @Override
    protected RecyclerView.ViewHolder getEmptyAPViewHolder(ViewGroup parent) {
        return new EmptyAPViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_j1_ap_empty, parent, false));
    }

    @Override
    protected J1HeadDividerViewHolder getHeaderDividerViewHolder(ViewGroup parent) {
        return new J1HeadDividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_j1_wifi_divider, parent, false));
    }

    @Override
    protected APViewHolder getAPViewHolder(ViewGroup parent) {
        return new J1APViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_j1_wifi_ap, parent, false), mListener);
    }
}
