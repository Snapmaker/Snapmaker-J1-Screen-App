package fabscreen.platform.base.view.debugtool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.service.machine.connection.mock.DebugModule;

public class MockModuleAdapter extends RecyclerView.Adapter<MockModuleAdapter.ViewHolder> {
    private List<DebugModule> debugModuleList;
    private onDeleteModuleListener onDeleteModuleListener;

    public MockModuleAdapter(List<DebugModule> debugModuleList) {
        this.debugModuleList = debugModuleList;
    }


    public void setDebugModuleList(List<DebugModule> debugModuleList) {
        this.debugModuleList = debugModuleList;
    }

    public void setOnDeleteModuleListener(MockModuleAdapter.onDeleteModuleListener onDeleteModuleListener) {
        this.onDeleteModuleListener = onDeleteModuleListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_module_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DebugModule debugModule = debugModuleList.get(position);
        holder.mTvModuleIndex.setText(String.format("%d", debugModule.index));
        holder.mTvModuleName.setText(debugModule.getModuleName());
        holder.mBtDeleteModule.setOnClickListener(v -> {
            if (onDeleteModuleListener != null) {
                onDeleteModuleListener.onClickDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return debugModuleList.size();
    }

    public interface onDeleteModuleListener {
        void onClickDelete(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.module_name)
        TextView mTvModuleName;
        @BindView(R2.id.module_index)
        TextView mTvModuleIndex;
        @BindView(R2.id.delete_module)
        Button mBtDeleteModule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
