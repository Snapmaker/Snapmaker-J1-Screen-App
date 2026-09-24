package fabscreen.features.filemanager.a400platform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import fabscreen.features.filemanager.R;

public class DetailA400DataView {

    TextView mTvDetailDataName;
    TextView mTvDetailDataValue;
    Context mContext;
    String mDetailDataName;
    String mDetailDataValue;

    public DetailA400DataView(Context context, String detailDataName, String detailDataValue) {
        mContext = context;
        mDetailDataName = detailDataName;
        mDetailDataValue = detailDataValue;
    }

    public View initialize() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_a400_datail_data, null);
        mTvDetailDataName = view.findViewById(fabscreen.platform.core.R.id.tv_detail_data_name);
        mTvDetailDataValue = view.findViewById(fabscreen.platform.core.R.id.tv_detail_data_value);
        mTvDetailDataName.setText(mDetailDataName);
        mTvDetailDataValue.setText(mDetailDataValue);
        return view;
    }

}
