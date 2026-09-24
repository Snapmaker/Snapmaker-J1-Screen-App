package fabscreen.platform.core.ui.common.wifi.viewholder.ap;

import android.view.View;

import androidx.annotation.NonNull;

import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.R;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;

public class J1APViewHolder extends APViewHolder {
    public J1APViewHolder(@NonNull View itemView, APListAdapter.OnItemClickListener listener) {
        super(itemView, listener);
    }

    @Override
    protected void refreshAPView(AccessPoint ap) {
        AccessPoint.ConnectState connectState = ap.getConnectState();
        mProgress.setVisibility(connectState == AccessPoint.ConnectState.CONNECTING || connectState == AccessPoint.ConnectState.CONFIRMED ? View.VISIBLE : View.INVISIBLE);
        mIvWifiSignal.setImageResource(connectState == AccessPoint.ConnectState.CONNECTED ? R.drawable.ic_ap_active : R.drawable.ic_ap_normal);
        mIvWifiSignal.setVisibility((connectState == AccessPoint.ConnectState.IDLE || connectState == AccessPoint.ConnectState.CONNECTED) ? View.VISIBLE : View.INVISIBLE);
        mIvApEncrypt.setImageResource(ap.isEncrypted() ? R.drawable.ic_ap_lock : R.drawable.ic_ap_unlock);
        mTvSsid.setText(ap.getSSID());
    }
}
