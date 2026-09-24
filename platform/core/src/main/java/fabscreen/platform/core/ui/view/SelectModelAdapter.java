package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.media.JetPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.core.R;

public class SelectModelAdapter extends RecyclerView.Adapter<SelectModelAdapter.ViewHolder> {

    private List<SelectModelBean> mModelBeanList;
    private OnItemClickListener mListener;
    private int mSelectPosition = 0;
    private Context mContext;

    public SelectModelAdapter(List<SelectModelBean> modelBeanList, Context context) {
        mModelBeanList = modelBeanList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_select_model, parent, false);
        return new SelectModelAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTvTitle.setText(mModelBeanList.get(position).getTitle());
        holder.mTvTitle.setTextColor(ContextCompat.getColor(mContext, mSelectPosition == position ? R.color.palette_blue_ribbon : R.color.palette_white_pure));
        holder.mTvContent.setText(mModelBeanList.get(position).getContent());
        holder.mIvCheck.setVisibility(mSelectPosition == position ? View.VISIBLE : View.INVISIBLE);
        holder.mCL.setBackgroundColor(ContextCompat.getColor(mContext, position == mSelectPosition ? R.color.palette_grey_nero : R.color.palette_black_snapmaker));
        holder.line.setVisibility(mSelectPosition == position ? View.VISIBLE : View.INVISIBLE);

        holder.mCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });
    }

    public void selectPosition(int position) {
        mSelectPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mModelBeanList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIvCheck;
        private TextView mTvTitle;
        private TextView mTvContent;
        private ConstraintLayout mCL;
        private TextView line;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvCheck = itemView.findViewById(R.id.iv_adapter_select_model_check);
            mTvTitle = itemView.findViewById(R.id.tv_adapter_select_model_title);
            mTvContent = itemView.findViewById(R.id.tv_adapter_select_model_content);
            mIvCheck = itemView.findViewById(R.id.iv_adapter_select_model_check);
            mCL = itemView.findViewById(R.id.cl_adapter_select_model);
            line = itemView.findViewById(R.id.tv_adapter_select_model_line);

        }
    }

}
