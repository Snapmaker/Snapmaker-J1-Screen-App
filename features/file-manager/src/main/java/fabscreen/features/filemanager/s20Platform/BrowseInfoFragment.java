package fabscreen.features.filemanager.s20Platform;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class BrowseInfoFragment extends BaseFragment {

    @BindView(R2.id.tv_print_file_info)
    TextView mTvInfo;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    public void initView() {
        String info = "工作头: Dual-Nozzle Head\n" +
                "\n" +
                "左挤出机: 材料 Snapmaker PLA W\n" +
                "      喷嘴温度 230 ℃ | 150 ℃ \n" +
                "      喷嘴直径 0.4mm\n" +
                "右挤出机: 材料 Snapmaker PLA B\n" +
                "      喷嘴温度 230 ℃ | 150 ℃ \n" +
                "      喷嘴直径 0.25mm\n" +
                "\n" +
                "Bed: 50 ℃\n" +
                "\n" +
                "Layer: 2302\n" +
                "Estimated Time: 24d 12h";
        mTvInfo.setText(info);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_s30_browse_print_file_info;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }


}
