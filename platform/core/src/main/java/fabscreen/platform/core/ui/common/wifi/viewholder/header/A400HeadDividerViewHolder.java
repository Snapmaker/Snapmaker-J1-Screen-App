package fabscreen.platform.core.ui.common.wifi.viewholder.header;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R2;

public class A400HeadDividerViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.cpi_progress)
    CircularProgressIndicator mCpiProgress;

    public A400HeadDividerViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(boolean showScanning, boolean searchOff) {
        mCpiProgress.setVisibility(showScanning ? View.VISIBLE : View.INVISIBLE);
        itemView.setVisibility(searchOff ? View.INVISIBLE : View.VISIBLE);
        if (getAdapterPosition() == 1) {
            itemView.getLayoutParams().height = (int) DimensUtils.dp2px(82);
        } else {
            itemView.getLayoutParams().height = (int) DimensUtils.dp2px(114);
        }
    }
}
