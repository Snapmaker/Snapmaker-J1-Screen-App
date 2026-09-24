package fabscreen.features.welcome.s20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fabscreen.features.welcome.R;
import fabscreen.platform.base.lib.network.AccessPoint;

public class WelcomeWifiListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private List<AccessPoint> mAccessPointList;
    private OnItemClickListener mOnItemClickListener;

    public WelcomeWifiListAdapter() {
        mAccessPointList = new ArrayList<>();
    }

    public void setAccessPoints(List<AccessPoint> accessPoints) {
        mAccessPointList = accessPoints;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public int getCount() {
        return mAccessPointList.size();
    }

    @Override
    public AccessPoint getItem(int position) {
        return mAccessPointList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_welcome_access_point, parent, false);
        }

        TextView ssid = convertView.findViewById(R.id.tv_access_point_ssid);
        ImageView ivEncrypted = convertView.findViewById(R.id.iv_settings_ap_item_has_authentication);

        AccessPoint accessPoint = getItem(position);

        ssid.setText(accessPoint.getSSID());
        ivEncrypted.setVisibility(accessPoint.isEncrypted() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AccessPoint accessPoint = getItem(position);

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onClick(accessPoint);
        }
    }

    public interface OnItemClickListener {
        void onClick(AccessPoint accessPoint);
    }
}
