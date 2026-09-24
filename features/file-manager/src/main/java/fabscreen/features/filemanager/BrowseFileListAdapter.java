package fabscreen.features.filemanager;

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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrowseFileListAdapter extends RecyclerView.Adapter<BrowseFileListAdapter.BrowseJ1FileListViewHolder> {

    private ArrayList<BrowseViewModel.BrowseJ1FileItem> mFileItems = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private boolean mIsJ1;

    public BrowseFileListAdapter(boolean isJ1) {
        mIsJ1 = isJ1;
    }

    public BrowseFileListAdapter(ArrayList<BrowseViewModel.BrowseJ1FileItem> items) {
        mFileItems = items;
    }

    public void setFileItems(ArrayList<BrowseViewModel.BrowseJ1FileItem> items) {
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
        BrowseViewModel.BrowseJ1FileItem fileItem = mFileItems.get(position);
//        holder.mIvFileThumbnail.setImageBitmap(fileItem.getFileThumbnail());
        // Default icon for different file
        switch (fileItem.fileType) {
            case FILE_TYPE_GCODE:
            case FILE_TYPE_NC:
//                holder.mIvFileThumbnail.setImageResource(mIsJ1 ? R.drawable.pic_file_normal_160x160 : R.drawable.pic_a400_file_normal_160x160);
                Glide.with(holder.itemView.getContext()).load(mIsJ1 ? R.drawable.pic_file_normal_160x160 : fileItem.thumbnailPath).into(holder.mIvFileThumbnail);
                //TODO remove this code after photography
//                holder.mIvFileThumbnail.setImageResource(mIsJ1 ? R.drawable.pic_file_normal_160x160 : R.drawable.pic_flie_boat_img);
                break;
            case FILE_TYPE_UPDATE:
                holder.mIvFileThumbnail.setImageResource(mIsJ1 ? R.drawable.pic_file_bin_160x160 : R.drawable.pic_a400_file_bin);
                break;
            case FILE_TYPE_LOG:
                holder.mIvFileThumbnail.setImageResource(R.drawable.pic_file_log_160x160);
                break;
            case FILE_TYPE_DIRECTORY:
                holder.mIvFileThumbnail.setImageResource(mIsJ1 ? R.drawable.pic_folder_normal_160x160 : R.drawable.pic_a400_folder_normal_160x160);
                break;
            case FILE_TYPE_UNKNOWN:
            default:
                holder.mIvFileThumbnail.setImageResource(mIsJ1 ? R.drawable.pic_file_error_160x160 : R.drawable.pic_a400_file_error_160x160);
                break;
        }
        holder.mTvFilename.setText(fileItem.getFilename());

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy  hh:mm", Locale.getDefault());
        String lastModified = sdf.format(new Date(fileItem.getFileLastModified()));

        if (fileItem.fileType == BrowseViewModel.FileType.FILE_TYPE_DIRECTORY) {
            holder.mTvDesc.setText(String.format(Locale.ENGLISH, "%s", lastModified));
        } else {
            // show file length in description.
            long fileLength = fileItem.getFileLength();
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

        holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(position));
        holder.mCbFileSelected.setVisibility(CheckBox.GONE);
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
