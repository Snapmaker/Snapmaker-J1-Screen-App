package fabscreen.platform.core.ui.common.wifi.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.common.wifi.viewholder.ap.A400APViewHolder;
import fabscreen.platform.core.ui.common.wifi.viewholder.header.A400HeadDividerViewHolder;
import fabscreen.platform.core.ui.common.wifi.viewholder.ap.APViewHolder;

public class A400APListAdapter extends APListAdapter {
    public A400APListAdapter(List<AccessPoint> aps) {
        super(aps);
    }

    @Override
    protected RecyclerView.ViewHolder getEmptyAPViewHolder(ViewGroup parent) {
        return new EmptyAPViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_j1_ap_empty, parent, false));
    }

    @Override
    protected A400HeadDividerViewHolder getHeaderDividerViewHolder(ViewGroup parent) {
        return new A400HeadDividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_a400_wifi_divider, parent, false));
    }

    @Override
    protected APViewHolder getAPViewHolder(ViewGroup parent) {
        return new A400APViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_a400_wifi_ap, parent, false), mListener);
    }
}
