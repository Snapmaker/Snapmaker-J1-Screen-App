package fabscreen.platform.base.view.debugtool;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.base.R;

public class BTAdapter extends RecyclerView.Adapter<BTAdapter.ViewHolder> {
    private final List<String> mNameList;
    private OnItemClickListener mListener;

    public BTAdapter(List<String> list) {
        mNameList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView itemView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bt_interface, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(mNameList.get(position));
    }

    @Override
    public int getItemCount() {
        return mNameList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(TextView itemView, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull TextView itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> mListener.onItemClick(itemView, getAdapterPosition()));
        }
    }
}
