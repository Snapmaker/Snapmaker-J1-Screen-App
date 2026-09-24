package fabscreen.platform.core.ui.common.wifi.viewholder.ap;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;

public abstract class APViewHolder extends RecyclerView.ViewHolder {
    @BindView(R2.id.iv_signal)
    ImageView mIvWifiSignal;
    @BindView(R2.id.tv_ssid)
    TextView mTvSsid;
    @BindView(R2.id.progress)
    CircularProgressIndicator mProgress;
    @BindView(R2.id.iv_ap_encrypt)
    ImageView mIvApEncrypt;

    public APViewHolder(@NonNull View itemView, APListAdapter.OnItemClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (listener == null) return;
        itemView.setOnClickListener(v -> {
            AccessPoint ap = (AccessPoint) v.getTag();
            if (ap.getConnectState() == AccessPoint.ConnectState.IDLE) {
                listener.onItemClick(ap);
            }
        });
    }

    public void bind(AccessPoint ap) {
        refreshAPView(ap);
        itemView.setTag(ap);
    }

    protected abstract void refreshAPView(AccessPoint ap);
}
