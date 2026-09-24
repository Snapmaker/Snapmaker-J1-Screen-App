package fabscreen.features.settings.a350.factory;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class FactoryToolsFragment extends BaseFragment {
    @BindView(R2.id.lv_factory_tools)
    ListView mListView;

    @BindView(R2.id.tv_factory_version)
    TextView mTvVersion;
    @BindView(R2.id.tv_factory_tool_head)
    TextView mTvToolHead;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_factory);

        initViews();

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_factory;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initViews() {
        List<Integer> items = new ArrayList<>();
        items.add(ListViewAdapter.CODE_COLOR_FRAUD);
        items.add(ListViewAdapter.CODE_CPU_BENCHMARK);
        items.add(ListViewAdapter.CODE_WIFI_SIGNAL);
        items.add(ListViewAdapter.CODE_BRIGHTNESS_TEST);
        items.add(ListViewAdapter.CODE_TOUCH_PANEL_TEST);
        items.add(ListViewAdapter.CODE_BLUETOOTH_TEST);
        items.add(ListViewAdapter.CODE_U_DISK_TEST);
        items.add(ListViewAdapter.CODE_COLOR_DIFFERENCE_TEST);

        ListViewAdapter adapter = new ListViewAdapter(getContext());
        adapter.setItems(items);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(adapter);

        final int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        String toolHead;
        switch (headType) {
            case Toolhead.HeadFactoryId.HEAD_FACTORY_3DP:
                toolHead = "3DP";
                break;
            case Toolhead.HeadFactoryId.HEAD_FACTORY_CNC:
                toolHead = "CNC";
                break;
            case Toolhead.HeadFactoryId.HEAD_FACTORY_LASER:
                toolHead = "Laser";
                break;
            default:
                toolHead = "Unknown";
                break;
        }

        mTvVersion.setText(String.format(Locale.getDefault(), "Version: %s", BaseApplication.getInstance().getAppVersionName()));
        mTvToolHead.setText(String.format(Locale.getDefault(), "Factory mode: %s", toolHead));
    }

    public class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private static final int CODE_COLOR_FRAUD = 0;
        private static final int CODE_CPU_BENCHMARK = 1;
        private static final int CODE_WIFI_SIGNAL = 2;
        private static final int CODE_BRIGHTNESS_TEST = 3;
        private static final int CODE_TOUCH_PANEL_TEST = 4;
        private static final int CODE_BLUETOOTH_TEST = 5;
        private static final int CODE_U_DISK_TEST = 6;
        private static final int CODE_COLOR_DIFFERENCE_TEST = 7;

        private Context mContext;
        private List<Integer> mItems = new ArrayList<>();

        ListViewAdapter(Context context) {
            mContext = context;
        }

        public void setItems(List<Integer> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_factory_tools, parent, false);
            }

            int code = (int) getItem(position);

            TextView textView = convertView.findViewById(R.id.tv_factory_tools_name);

            switch (code) {
                case CODE_COLOR_FRAUD: {
                    textView.setText("坏点检测");
                    break;
                }
                case CODE_CPU_BENCHMARK: {
                    textView.setText("CPU 性能测试");
                    break;
                }
                case CODE_WIFI_SIGNAL: {
                    textView.setText("Wi-Fi 信号测试");
                    break;
                }
                case CODE_BRIGHTNESS_TEST: {
                    textView.setText("亮度测试");
                    break;
                }
                case CODE_TOUCH_PANEL_TEST: {
                    textView.setText("TP 测试");
                    break;
                }
                case CODE_BLUETOOTH_TEST: {
                    textView.setText("模块蓝牙测试");
                    break;
                }
                case CODE_U_DISK_TEST: {
                    textView.setText("U 盘读取测试");
                    break;
                }
                case CODE_COLOR_DIFFERENCE_TEST: {
                    textView.setText("图标色差测试");
                    break;
                }

                default:
                    break;
            }

            return convertView;
        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int code = (int) getItem(position);

            switch (code) {
                case CODE_COLOR_FRAUD: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoColorFraudFragment();
                    break;
                }
                case CODE_CPU_BENCHMARK: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoCpuBenchmarkFragment();
                    break;
                }
                case CODE_WIFI_SIGNAL: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoWifiSignalFragment();
                    break;
                }
                case CODE_BRIGHTNESS_TEST: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoBrightnessTestFragment();
                    break;
                }
                case CODE_TOUCH_PANEL_TEST: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoTouchPanelTestFragment();
                    break;
                }
                case CODE_BLUETOOTH_TEST: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoBluetoothFragment();
                    break;
                }
                case CODE_U_DISK_TEST: {
                    // Reuse file page for testing u-disk.
                    ServiceContainer.getInstance().getService(IRouter.class).routeToFilesPage(0).start(getContext());
                    break;
                }
                case CODE_COLOR_DIFFERENCE_TEST: {
                    if (getActivity() == null) return;
                    ((FactoryToolsActivity) getActivity()).gotoColorDifferenceFragment();
                    break;
                }
            }
        }
    }
}
