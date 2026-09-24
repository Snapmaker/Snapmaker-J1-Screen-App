package fabscreen.features.filemanager;

import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_CNC;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_GCODE;
import static fabscreen.features.filemanager.entity.FileType.FILE_TYPE_NC;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.features.filemanager.entity.BrowseShowFile;
import fabscreen.features.filemanager.entity.FileType;

public class NewBrowseFileListAdapter extends RecyclerView.Adapter<NewBrowseFileListAdapter.BrowseJ1FileListViewHolder> {
    private ArrayList<BrowseShowFile> mFileItems = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM-dd-yyyy  hh:mm", Locale.getDefault());
    private HashSet<FileType> mFileTypeSet;
    private boolean isMultipleSelection = false;
    private boolean mIsJ1;

    public NewBrowseFileListAdapter(boolean isJ1) {
        mIsJ1 = isJ1;
        mFileTypeSet = new HashSet<>(3);
        mFileTypeSet.add(FILE_TYPE_GCODE);
        mFileTypeSet.add(FILE_TYPE_NC);
        mFileTypeSet.add(FILE_TYPE_CNC);
    }

    public void setFileItems(ArrayList<BrowseShowFile> items) {
        mFileItems = items;
    }

    @NonNull
    @Override
    public BrowseJ1FileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_j1_browse_file, parent, false);
        return new BrowseJ1FileListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrowseJ1FileListViewHolder holder, int position) {
        // Handle data binding with ViewHolder.
        BrowseShowFile fileItem = mFileItems.get(position);

        holder.mTvFilename.setText(fileItem.getIFile().getName());
        String lastModified = mSimpleDateFormat.format(new Date(fileItem.getIFile().lastModified()));
        if (fileItem.getFileType() == FileType.FILE_TYPE_DIRECTORY) {
            holder.mTvDesc.setText(String.format(Locale.ENGLISH, "%s", lastModified));
        } else {
            // show file length in description.
            long fileLength = fileItem.getIFile().length();
            String fileLengthUnit = "bytes";
            if (fileLength > 1024) {
                fileLength /= 1024;
                fileLengthUnit = "KB";
                if (fileLength > 1024) {
                    fileLength /= 1024;
                    fileLengthUnit = "MB";
                }
            }
            holder.mTvDesc.setText(String.format(Locale.ENGLISH, "%s %s  %s", fileLength, fileLengthUnit, lastModified));
        }
        if (mFileTypeSet.contains(fileItem.getFileType()) && fileItem.isIsHavePic() && fileItem.getShowPic() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(fileItem.getShowPic())
                    .into(holder.mIvFileThumbnail);
        } else {
            Glide.with(holder.itemView.getContext()).
                    load(fileItem.getDefaultDisplay())
                    .into(holder.mIvFileThumbnail);
        }
        fileItem.setIsSetView(true);
        if (isMultipleSelection) {
            holder.mCbFileSelected.setVisibility(View.VISIBLE);
            holder.mCbFileSelected.setChecked(fileItem.isSelect());
        } else {
            holder.mCbFileSelected.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (isMultipleSelection) {
                fileItem.setSelect(!fileItem.isSelect());
                holder.mCbFileSelected.setChecked(fileItem.isSelect());
                mOnItemClickListener.onItemSelect(position, fileItem.isSelect());
                notifyItemChanged(position);
            } else {
                mOnItemClickListener.onItemClick(position);
            }
        });
    }

    public boolean isMultipleSelection() {
        return isMultipleSelection;
    }

    public void setMultipleSelection(boolean multipleSelection) {
        isMultipleSelection = multipleSelection;
        if (mFileItems != null && !mFileItems.isEmpty()) {
            for (int i = 0; i < mFileItems.size(); i++) {
                mFileItems.get(i).setSelect(false);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemSelect(int position, boolean state);
    }

    static class BrowseJ1FileListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.iv_browse_j1_item_thumbnail)
        ImageView mIvFileThumbnail;
        @BindView(R2.id.tv_browse_j1_item_file_name)
        TextView mTvFilename;
        @BindView(R2.id.cb_browse_j1_item_file_select)
        CheckBox mCbFileSelected;
        @BindView(R2.id.tv_browse_j1_item_file_desc)
        TextView mTvDesc;

        public BrowseJ1FileListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
