package fabscreen.features.home;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class LauncherFragment extends BaseFragment {
    private static final int ENTRY_FILES = 1000;
    private static final int ENTRY_CONTROL = 2000;
    private static final int ENTRY_CALIBRATION = 3000;
    private static final int ENTRY_SETTINGS = 4000;
    private static final int ENTRY_FACTORY = 5000;
    private static final int ENTRY_EXP = 6000;
    private static final int ENTRY_ENCLOSURE = 7000;
    private static final int ENTRY_CNC_TOOL_BOX = 8000;
    private static final int ENTRY_AIR_PURIFIER = 9000;
    @BindView(R2.id.rl_calibration_panel)
    GridView mGridView;
    private ModuleEntryAdapter mAdapter;
    private AlertDialog mToolboxDialog;

    public static LauncherFragment newInstance() {
        return new LauncherFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        final int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        final boolean isRotaryAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
        final List<ModuleEntry> entries = new ArrayList<>();

        entries.add(new ModuleEntry(ENTRY_FILES, R.string.browse_files, RoutePath.FILE_BROWSER, R.drawable.ic_module_files_80x80));
        entries.add(new ModuleEntry(ENTRY_CONTROL, R.string.all_control, RoutePath.TOOLS_CONTROL_J1, R.drawable.ic_module_control_80x80));
        if (headType == Module.ModuleType.HEAD_CNC && isRotaryAvailable) {
            entries.add(new ModuleEntry(ENTRY_CNC_TOOL_BOX, R.string.cnc_tool_box, null, R.drawable.ic_module_toolbox_80x80));
        } else if (headType != Module.ModuleType.HEAD_LASER_10W) {
            entries.add(new ModuleEntry(ENTRY_CALIBRATION, R.string.all_calibration, RoutePath.TOOLS_CALIBRATION_S20_LASER, R.drawable.ic_module_calibration_80x80));
        }
        entries.add(new ModuleEntry(ENTRY_SETTINGS, R.string.all_settings, RoutePath.SETTINGS_INDEX, R.drawable.ic_module_settings_80x80));

        boolean debugFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getDebugFlag();
        if (debugFlag) {
            entries.add(new ModuleEntry(ENTRY_FACTORY, R.string.all_factory, RoutePath.SETTINGS_FACTORY, R.drawable.ic_module_settings_80x80));
            entries.add(new ModuleEntry(ENTRY_EXP, R.string.all_experiment, null, R.drawable.ic_module_settings_80x80));
        }

        // check enclosure status
        boolean isEnclosureReady = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable;
        if (isEnclosureReady) {
            entries.add(new ModuleEntry(ENTRY_ENCLOSURE, R.string.all_enclosure, RoutePath.ADDONS_ENCLOSURE, R.drawable.ic_module_enclosure_80x80));
        }

        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            entries.add(new ModuleEntry(ENTRY_AIR_PURIFIER, R.string.all_air_purifier, RoutePath.ADDONS_AIR_PURIFIER, R.drawable.ic_module_air_purifier_80x80));
        }

        mAdapter.setEntries(entries);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_launcher;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mAdapter = new ModuleEntryAdapter(getContext());
        mGridView.setAdapter(mAdapter);
    }

    public void showToolboxDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        if (mToolboxDialog != null) {
            if (mToolboxDialog.isShowing()) return;
        }
        mToolboxDialog = builder.create();
        if (mToolboxDialog.getWindow() != null) {
            mToolboxDialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            mToolboxDialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_cnc_toolbox, null);
        // init button icon
        Button btnOriginAssist = view.findViewById(R.id.btn_cnc_origin_assistant_entry);
        Button btnBitAssistant = view.findViewById(R.id.btn_cnc_bit_assistant_entry);

        btnOriginAssist.setOnClickListener(v -> {
            ServiceContainer.getInstance().getService(IRouter.class).routeToCNCOriginAssistantPage().start(getContext());
            if (mToolboxDialog != null) {
                mToolboxDialog.dismiss();
            }
        });

        btnBitAssistant.setOnClickListener(v -> {
            ServiceContainer.getInstance().getService(IRouter.class).routeToCNCBitAssistantPage().start(getContext());
            if (mToolboxDialog != null) {
                mToolboxDialog.dismiss();
            }
        });
        mToolboxDialog.setView(view);
        mToolboxDialog.setCanceledOnTouchOutside(true);
        mToolboxDialog.show();
    }

    /**
     * Module Entry represents a entry information of a module: its name, class path and icon.
     */
    static class ModuleEntry {
        private int entryCode;
        private int nameRes;
        private String entryClassPath;
        private int resourceId;

        ModuleEntry(int entryCode, @StringRes int nameRes, @RoutePath.Path String entryClassPath, int resourceId) {
            this.entryCode = entryCode;
            this.nameRes = nameRes;
            this.entryClassPath = entryClassPath;
            this.resourceId = resourceId;
        }

        int getEntryCode() {
            return entryCode;
        }

        int getNameRes() {
            return nameRes;
        }

        int getResourceId() {
            return resourceId;
        }

        @RoutePath.Path
        String getEntryClassPath() {
            return entryClassPath;
        }
    }

    class ModuleEntryAdapter extends BaseAdapter {
        private Context mContext;
        private List<ModuleEntry> mEntries = new ArrayList<>();

        ModuleEntryAdapter(Context context) {
            mContext = context;
        }

        void setEntries(List<ModuleEntry> entries) {
            this.mEntries = entries;
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public ModuleEntry getItem(int i) {
            return mEntries.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_module_entry, parent, false);
            }

            final ModuleEntry entry = getItem(position);

            // icon
            final Button icon = convertView.findViewById(R.id.icon);
            icon.setBackgroundResource(entry.getResourceId());
            icon.setOnClickListener(v -> {
                switch (entry.getEntryCode()) {
                    case ENTRY_CALIBRATION: {
                        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
                        if (headType == Module.ModuleType.HEAD_3DP) {
                            ServiceContainer.getInstance().getService(IRouter.class)
                                    .routeTo3DPCalibrationPage(true)
                                    .start(mContext);
                        } else {
                            ServiceContainer.getInstance().getService(IRouter.class)
                                    .routeToLaserCalibrationPage()
                                    .start(mContext);
                        }
                        break;
                    }
                    case ENTRY_CNC_TOOL_BOX: {
                        showToolboxDialog();
                        break;
                    }

                    case ENTRY_EXP: {
                        ServiceContainer.getInstance().getService(IRouter.class)
                                .routeToExperimentPage()
                                .start(mContext);
                        break;
                    }
                    default:
                        ServiceContainer.getInstance().getService(IRouter.class)
                                .routeWithClassPath(entry.getEntryClassPath())
                                .start(mContext);
                        break;
                }
            });

            // module name
            final TextView title = convertView.findViewById(R.id.title);
            if (entry.getNameRes() != 0) {
                title.setText(entry.getNameRes());
            }

            return convertView;
        }
    }
}
