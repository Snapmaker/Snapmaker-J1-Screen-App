package fabscreen.platform.core.ui.common.wifi.viewholder.header;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.core.R2;

public class J1HeadDividerViewHolder extends RecyclerView.ViewHolder {
    @BindView(R2.id.ll_networks_label)
    LinearLayout mLlNetworksLabel;
    @BindView(R2.id.progress_scan)
    CircularProgressIndicator mProgressScan;

    public J1HeadDividerViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(boolean showScanning, boolean searchOff) {
        Logger.d("scan: %1$s, search-off: %2$s", showScanning, searchOff);
        mProgressScan.setVisibility(showScanning ? View.VISIBLE : View.INVISIBLE);
        mLlNetworksLabel.setVisibility(searchOff ? View.INVISIBLE : View.VISIBLE);
    }
}
