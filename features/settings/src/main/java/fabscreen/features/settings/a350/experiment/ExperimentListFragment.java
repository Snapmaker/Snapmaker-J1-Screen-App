package fabscreen.features.settings.a350.experiment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.core.ui.view.FabInputDialog;
import fabscreen.platform.core.ui.view.FabProgressDialog;

public class ExperimentListFragment extends BaseFragment {
    private static final String EXP_VIEW = "Custom View";
    private static final String EXP_CLEAR = "Clear Local Files";
    private static final String EXP_API = "API";
    private static final String EXP_LASER_CALIBRATION = "Laser Camera Detection";
    private static final String EXP_BLUETOOTH_DEMO = "Bluetooth Demo";
    private static final String EXP_PREFERENCE = "Preference";
    private static final String EXP_COPY_TEST = "Workspace Copy Test";
    private static final String EXP_CRASHLYTIC_TEST = "Crashlytic Test";
    private static final String EXP_CUSTOM_DIALOG = "Custom Dialog";
    private static final String EXP_LASER_10W = "Laser 10W";
    private static final String EXP_SOCKET = "Socket Test";
    private static final String EXP_LASER_SELF_INSPECTION = "Laser self-inspection ";
    private static final String EXP_SACP_DEBUG = "SACP Debug";

    @BindView(R2.id.lv_experiment_list)
    ListView mLvExperimentList;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment_list;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setTitle("Experimental Features");

        List<String> entries = new ArrayList<>();

        // fragile dev features
        entries.add(EXP_VIEW);
        entries.add(EXP_CLEAR);
        entries.add(EXP_API);
        entries.add(EXP_LASER_CALIBRATION);
        entries.add(EXP_BLUETOOTH_DEMO);
        entries.add(EXP_PREFERENCE);
        entries.add(EXP_COPY_TEST);
        entries.add(EXP_CRASHLYTIC_TEST);
        entries.add(EXP_CUSTOM_DIALOG);
        entries.add(EXP_LASER_10W);
        entries.add(EXP_SOCKET);
        entries.add(EXP_LASER_SELF_INSPECTION);
        entries.add(EXP_SACP_DEBUG);

        Adapter adapter = new Adapter(getContext(), entries);
        mLvExperimentList.setAdapter(adapter);
    }

    class Adapter extends BaseAdapter {
        private Context mContext;
        private List<String> mEntries;

        Adapter(Context context, List<String> entries) {
            mContext = context;
            mEntries = entries;
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public String getItem(int position) {
            return mEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_settings_item, parent, false);
            }

            final String entry = getItem(position);

            final Button button = convertView.findViewById(R.id.btn_settings_item_name);

            button.setText(entry);
            button.setOnClickListener(v -> {
                ExperimentActivity activity = (ExperimentActivity) mContext;

                switch (entry) {
                    case EXP_VIEW:
                        activity.gotoCustomView();
                        break;
                    case EXP_CLEAR:
                        final boolean isClear = FileHelper.removeFile(ServiceContainer.getInstance().getService(IAppService.class).getFilesDir());
                        FabAlert.alert(mContext, isClear ? "Clear local files success." : "Local files is already empty.");
                        break;
                    case EXP_API:
                        FabInputDialog.create(getContext())
                                .setTitle("Please input your update host")
                                .setEditText(ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getApiHost())
                                .setButton("Set", (dialog, which) -> {
                                    final String url = FabInputDialog.getsInstance().getEditTextContent();
                                    if (url.isEmpty()) {
                                        FabAlert.alert(getContext(), "The host address is empty, please try again.");
                                    } else {
                                        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setApiHost(url);
                                    }
                                }).show();
                        break;
                    case EXP_LASER_CALIBRATION:
                        activity.gotoLaserCalibrationDetect();
                        break;
                    case EXP_BLUETOOTH_DEMO:
                        activity.gotoBluetoothDemo();
                        break;
                    case EXP_PREFERENCE:
                        activity.gotoPreferenceFragment();
                        break;
                    case EXP_COPY_TEST:
                        activity.gotoCopyTestFragment();
                        break;
                    case EXP_CRASHLYTIC_TEST:
                        activity.gotoCrashlyticsTestFragment();
                        break;
                    case EXP_SOCKET:
                        activity.gotoSocketTestFragment();
                    case EXP_CUSTOM_DIALOG:
                        FabProgressDialog progressDialog = new FabProgressDialog(requireContext());
                        progressDialog.setMessage(R.string.preview_auto_measure_loading_msg);
                        progressDialog.setCancelOnTouchOutside(true);
                        progressDialog.show();
                        break;
                    case EXP_LASER_10W:
                        ServiceContainer.getInstance().getService(IRouter.class).routeToPreviewPage(true, "").start(requireContext());
                        break;
                    case EXP_LASER_SELF_INSPECTION:
                        activity.gotoSelfInspectionFragment();
                        break;

                    case EXP_SACP_DEBUG:
                        activity.gotoSACPDebugFragment();
                        break;
                    default:
                        break;
                }
            });

            // description
            TextView textView = convertView.findViewById(R.id.tv_settings_item_desc);
            textView.setText(R.string.all_hello);

            return convertView;
        }
    }
}
