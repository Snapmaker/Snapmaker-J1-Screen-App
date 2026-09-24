package fabscreen.features.welcome.a400;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import fabscreen.features.welcome.R;
import fabscreen.platform.core.ui.view.VideoPlayerIJK;

public class A400LanguageAdapter extends RecyclerView.Adapter<A400LanguageAdapter.ViewHolder> {

    private List<LanguageItem> mList;
    private int mPosition = 0;
    private OnItemClickListener mOnItemClickListener;

    public A400LanguageAdapter(List<LanguageItem> list) {
        mList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_a400_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mBtnLanguage.setSelected(mPosition == position);
        if (!TextUtils.isEmpty(mList.get(position).getName())) {
            holder.mBtnLanguage.setText(mList.get(position).getName());
        }
        holder.mBtnLanguage.setVisibility(TextUtils.isEmpty(mList.get(position).getName()) ? View.INVISIBLE : View.VISIBLE);
        holder.mBtnLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    if (TextUtils.isEmpty(mList.get(position).getName())) {
                        return;
                    } else {
                        mOnItemClickListener.onItemClick(position);
                    }
                }
            }
        });
    }

    public void selectPosition(int position) {
        mPosition = position;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Button mBtnLanguage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mBtnLanguage = itemView.findViewById(R.id.btn_language);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(int position);
    }

}
