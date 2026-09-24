package fabscreen.platform.core.ui.common.selection;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalibrationModeSelectionAdapter extends RecyclerView.Adapter<CalibrationModeSelectionAdapter.ViewHolder> {
    private final List<CalibrationModeSelectionItem> mSectionItems;
    private CalibrationModeSelectionAdapter.OnSectionSelectedListener mSelectedListener;
    private int mSelection = 0;

    public CalibrationModeSelectionAdapter(@NonNull List<CalibrationModeSelectionItem> sectionItems) {
        mSectionItems = sectionItems;
    }

    @NonNull
    @Override
    public CalibrationModeSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_left_section,parent,false);
        CalibrationModeSelectionView section = new CalibrationModeSelectionView(parent.getContext());
        return new CalibrationModeSelectionAdapter.ViewHolder(section);
    }

    @Override
    public void onBindViewHolder(@NonNull CalibrationModeSelectionAdapter.ViewHolder holder, int position) {
        CalibrationModeSelectionItem sectionItem = mSectionItems.get(position);
        CalibrationModeSelectionView sectionView = (CalibrationModeSelectionView) holder.itemView;
        sectionView.setTitle(sectionItem.titleID);
        if (sectionItem.contentID != 0)
            sectionView.setContent(sectionItem.contentID);
        sectionView.setSelected(position == mSelection);
    }

    @Override
    public int getItemCount() {
        return mSectionItems.size();
    }


    public void setOnSectionSelectedListener(OnSectionSelectedListener listener) {
        mSelectedListener = listener;
    }

    public void setSelection(int selection) {
        int lastSelection = mSelection;
        mSelection = selection;
        notifyItemChanged(lastSelection);
        notifyItemChanged(mSelection);
    }

    public interface OnSectionSelectedListener {
        void onSelected(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                setSelection(clickedPosition);
                if (mSelectedListener != null) {
                    mSelectedListener.onSelected(clickedPosition);
                }
            });
        }
    }
}
