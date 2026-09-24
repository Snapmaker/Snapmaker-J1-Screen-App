package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.core.R;

public class ModelPopupAdapter extends RecyclerView.Adapter<ModelPopupAdapter.popupHolder> {

    private List<String> list;
    private Context context;
    private onRvClickListener listener;

    public ModelPopupAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setOnItemOnclickListener(onRvClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public popupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_model_popup, parent, false);
        return new popupHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull popupHolder holder, int position) {
        holder.modelTv.setText(list.get(position));
        holder.mTvLine.setVisibility(position == list.size() - 1 ? View.GONE : View.VISIBLE);
        holder.mBtnBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class popupHolder extends RecyclerView.ViewHolder {

        TextView modelTv;
        Button mBtnBg;
        TextView mTvLine;

        public popupHolder(@NonNull View itemView) {
            super(itemView);
            modelTv = itemView.findViewById(R.id.tv_model_popup_txt);
            mBtnBg = itemView.findViewById(R.id.btn_model_popup_bg);
            mTvLine = itemView.findViewById(R.id.tv_model_popup_line);
        }
    }

    public interface onRvClickListener {
        void onItemClick(int position);
    }

}
