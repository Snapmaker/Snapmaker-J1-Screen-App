package fabscreen.features.welcome.j1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.features.welcome.R;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;

public class WelcomeLanguageAdapter extends RecyclerView.Adapter<WelcomeLanguageAdapter.ViewHolder> {
    private Context mContext;
    private List<LanguageItem> mItems;
    private OnSectionSelectedListener mSelectedListener;
    private int mSelectPosition = 0;

    public WelcomeLanguageAdapter(List<LanguageItem> items, Context context) {
        mItems = items;
        mContext = context;
    }

    public void setOnSectionSelectedListener(OnSectionSelectedListener listener) {
        mSelectedListener = listener;
    }

    public interface OnSectionSelectedListener {
        void onSelected(int position);
    }

    public void setSelectPosition(int position) {
        mSelectPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_welcome_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.mLanguageTv.setText(mItems.get(position).getLanguageName());
        holder.mIvSelectIcon.setVisibility(position == mSelectPosition ? View.VISIBLE : View.GONE);
        holder.mLanguageTv.setTextColor(position == mSelectPosition ?
                ContextCompat.getColor(mContext, R.color.palette_orange_pizazz)
                : ContextCompat.getColor(mContext, R.color.palette_grey_french));
        holder.mTvLine.setVisibility(position == mItems.size() - 1 ? View.GONE : View.VISIBLE);
        holder.mRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedListener != null) {
                    mSelectedListener.onSelected(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIvSelectIcon;
        private TextView mLanguageTv;
        private RelativeLayout mRl;
        private TextView mTvLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvSelectIcon = itemView.findViewById(R.id.iv_welcome_language_selected);
            mLanguageTv = itemView.findViewById(R.id.tv_welcome_language_name);
            mRl = itemView.findViewById(R.id.rl_welcome_language);
            mTvLine = itemView.findViewById(R.id.tv_welcome_language_line);
        }
    }
}
