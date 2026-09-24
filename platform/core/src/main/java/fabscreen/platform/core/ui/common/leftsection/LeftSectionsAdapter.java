package fabscreen.platform.core.ui.common.leftsection;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fabscreen.platform.core.ui.view.leftsection.ILeftSectionView;

public abstract class LeftSectionsAdapter extends RecyclerView.Adapter<LeftSectionsAdapter.ViewHolder> {
    private OnSectionSelectedListener mSelectedListener;
    private final List<SectionItem> mSectionItems;
    private int mSelection = 0;

    public LeftSectionsAdapter(@NonNull List<SectionItem> sectionItems) {
        mSectionItems = sectionItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(getView(parent));
    }

    protected abstract View getView(ViewGroup parent);

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SectionItem sectionItem = mSectionItems.get(position);
        ILeftSectionView sectionView = (ILeftSectionView) holder.itemView;
        sectionView.setTitle(sectionItem.title);
        sectionView.setShowBadge(sectionItem.showBadge);
        sectionView.setIcon(sectionItem.iconRes);
        sectionView.setSelected(position == mSelection);
        sectionView.setLineCount(sectionItem.lineCount);
    }

    @Override
    public int getItemCount() {
        return mSectionItems.size();
    }


    public void setOnSectionSelectedListener(OnSectionSelectedListener listener) {
        mSelectedListener = listener;
    }

    public interface OnSectionSelectedListener {
        void onSelected(int position, boolean isUserClick);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                setSelection(clickedPosition, true);
            });
        }
    }

    public void setSelection(int selection) {
        setSelection(selection, false);
    }

    private void setSelection(int selection, boolean isUserClick) {
        int lastSelection = mSelection;
        mSelection = selection;
        notifyItemChanged(lastSelection);
        notifyItemChanged(mSelection);
        if (mSelectedListener != null) {
            mSelectedListener.onSelected(selection, isUserClick);
        }
    }

    public int getSelection() {
        return mSelection;
    }
}
