package fabscreen.platform.base.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import io.reactivex.disposables.CompositeDisposable;

public class MenuAdapter extends BaseAdapter {
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Context mContext;
    private List<String> mItems;
    private int mSelectedItem = -1;
    private OnItemClickListener mListener;
    private boolean mIsJ1;

    public MenuAdapter(Context context, List<String> items) {
        mContext = context;
        mItems = items;
    }

    void dispose() {
        disposables.dispose();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public String getItem(int i) {
        return mItems.get(i);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_menu, parent, false);
        }

        String name = getItem(position);

        TextView tvItemName = convertView.findViewById(R.id.tv_menu_item_name);
        Button btnItem = convertView.findViewById(R.id.btn_menu_item);
        ImageView imageView = convertView.findViewById(R.id.iv_menu_item_selected);
        View view = convertView.findViewById(R.id.v_menu_item_line);
        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J){
            tvItemName.setTextSize(16);
        }
        tvItemName.setText(name);

        imageView.setVisibility(mSelectedItem == position ? View.VISIBLE : View.INVISIBLE);
        btnItem.setSelected(mSelectedItem == position);
        tvItemName.setSelected(mSelectedItem == position);
        view.setVisibility(position == mItems.size() - 1 ? View.GONE : View.VISIBLE);

        btnItem.setOnClickListener((v) -> {
            if (mListener != null) {
                mSelectedItem = position;
                mListener.onItemClick(v, position);
            }
        });

        return convertView;
    }

    public void setSelectPosition(int position) {
        mSelectedItem = position;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
