package fabscreen.features.filemanager.j1Platform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.features.filemanager.R;

public class ProblemRvAdapter extends RecyclerView.Adapter<ProblemRvAdapter.DataViewHolder> {

    private List<Integer> list;
    private Context context;

    public ProblemRvAdapter(List<Integer> list, Context context) {
        this.list = list;
        this.context=context;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_rv_problem, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {

        switch (position){
            case 0:
                holder.iv.setImageResource(R.drawable.pic_normal_mode_160x160);
                holder.nameTv.setText(context.getResources().getString(R.string.file_detail_standard_mode_name));
                holder.contentTv.setText(context.getResources().getString(R.string.file_detail_standard_mode_name_content));
                break;
            case 1:
                holder.iv.setImageResource(R.drawable.pic_standby_mode_160x160);
                holder.nameTv.setText(context.getResources().getString(R.string.file_detail_backup_mode_name));
                holder.contentTv.setText(context.getResources().getString(R.string.file_detail_backup_mode_name_content));
                break;
            case 2:
                holder.iv.setImageResource(R.drawable.pic_copy_mode_160x160);
                holder.nameTv.setText(context.getResources().getString(R.string.file_detail_copy_mode_name));
                holder.contentTv.setText(context.getResources().getString(R.string.file_detail_copy_mode_name_content));
                break;
            case 3:
                holder.iv.setImageResource(R.drawable.pic_mirror_mode_160x160);
                holder.nameTv.setText(context.getResources().getString(R.string.file_detail_mirror_mode_name));
                holder.contentTv.setText(context.getResources().getString(R.string.file_detail_mirror_mode_name_content));
                break;
        }

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv;
        private TextView nameTv;
        private TextView contentTv;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_model);
            nameTv = itemView.findViewById(R.id.tv_model);
            contentTv = itemView.findViewById(R.id.tv_model2);
        }
    }

}
