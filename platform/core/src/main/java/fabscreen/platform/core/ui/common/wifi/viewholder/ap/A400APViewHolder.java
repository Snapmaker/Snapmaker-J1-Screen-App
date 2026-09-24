package fabscreen.platform.core.ui.common.wifi.viewholder.ap;

import static fabscreen.platform.base.lib.network.AccessPoint.ConnectState.*;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;

public class A400APViewHolder extends APViewHolder {
    @BindView(R2.id.iv_tick)
    ImageView mIvTick;


    public A400APViewHolder(@NonNull View itemView, APListAdapter.OnItemClickListener listener) {
        super(itemView, listener);
    }

    @Override
    protected void refreshAPView(AccessPoint ap) {
        AccessPoint.ConnectState connectState = ap.getConnectState();
        mTvSsid.setText(ap.getSSID());
        mTvSsid.setTextColor(getAdapterPosition() == 0 ? 0xffffffff : 0xffc9c9c9);
        mIvTick.setVisibility(connectState == CONNECTED ? View.VISIBLE : View.INVISIBLE);
        mProgress.setVisibility(connectState == CONNECTING || connectState == CONFIRMED ? View.VISIBLE : View.INVISIBLE);
        mIvApEncrypt.setImageResource(ap.isEncrypted() ? R.drawable.ic_a400_ap_lock : R.drawable.ic_a400_ap_unlock);
        mIvWifiSignal.setImageResource(getSignalResource(ap.getRssi()));
    }

    private int getSignalResource(float rssi) {
        if (rssi >= -75) {
            return R.drawable.ic_a400_signal_good;
        } else if (rssi >= -85) {
            return R.drawable.ic_a400_signal_fair;
        } else {
            return R.drawable.ic_a400_signal_poor;
        }
    }
}
