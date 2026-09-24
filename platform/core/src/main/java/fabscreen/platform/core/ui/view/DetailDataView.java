package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import fabscreen.platform.core.R;

public class DetailDataView {
    TextView mTvDetailDataName;
    TextView mTvDetailDataValue;
    Context mContext;
    String mDetailDataName;
    String mDetailDataValue;

    public DetailDataView(Context context, String detailDataName, String detailDataValue) {
        mContext = context;
        mDetailDataName = detailDataName;
        mDetailDataValue = detailDataValue;
    }

    public View initialize() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_datail_data, null);
        mTvDetailDataName = view.findViewById(R.id.tv_detail_data_name);
        mTvDetailDataValue = view.findViewById(R.id.tv_detail_data_value);
        mTvDetailDataName.setText(mDetailDataName);
        mTvDetailDataValue.setText(mDetailDataValue);
        return view;
    }
}
