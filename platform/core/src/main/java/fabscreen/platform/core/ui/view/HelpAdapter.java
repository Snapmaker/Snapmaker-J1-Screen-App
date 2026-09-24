package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import fabscreen.platform.core.R;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.DataViewHolder> {

    private List<HelpBean> list;
    private Context context;

    public HelpAdapter(List<HelpBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_help, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        Glide.with(context).load(list.get(position).getPicResource()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.mIvPic);
        holder.mTvContent.setText(list.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIvPic;
        private TextView mTvContent;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvPic = itemView.findViewById(R.id.iv_help_dialog_pic);
            mTvContent = itemView.findViewById(R.id.iv_help_dialog_content);

        }
    }

}
