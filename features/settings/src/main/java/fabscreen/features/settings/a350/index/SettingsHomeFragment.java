package fabscreen.features.settings.a350.index;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileOutputStream;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;

public class SettingsHomeFragment extends BaseFragment {
    private static final int SECTION_GENERAL = 100;
    private static final int ITEM_WIFI = 101;
    private static final int ITEM_LANGUAGE = 102;

    private static final int SECTION_ABOUT = 200;
    private static final int ITEM_ABOUT_MACHINE = 201;
    private static final int ITEM_SECURITY = 202;
    private static final int ITEM_USER_PREFERENCE = 203;
    private static final int ITEM_FIRMWARE_UPDATE = 204;

    private static final int SECTION_ADVANCED = 300;
    private static final int ITEM_ADVANCED_3DP = 301;
    private static final int ITEM_ADVANCED_3DP_GUIDE = 302;
    private static final int ITEM_ADVANCED_LASER = 303;
    private static final int ITEM_ADVANCED_LASER_GUIDE = 304;
    private static final int ITEM_ADVANCED_CNC_GUIDE = 305;
    private static final int ITEM_EXPORT_LOG = 306;
    private static final int ITEM_ADVANCE_ROTARY_LASER_GUIDE = 307;
    private static final int ITEM_ADVANCE_ROTARY_CNC_GUIDE = 308;
    private static final int ITEM_ADVANCE_AIR_PURIFIER = 309;
    private static final int ITEM_ADVANCED_LASER_10W = 310;
    private static final int ITEM_ADVANCED_LASER_10w_GUIDE = 311;
    @BindView(R2.id.lv_settings_item_list)
    ListView mListView;
    private IPartition mFileManager;
    private ItemListAdapter mAdapter;
    private List<SettingsItem> mItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileManager = ServiceContainer.getInstance().getService(IFileManagerService.class).getFabUsbDevice();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null && mItems != null) {
            for (SettingsItem item : mItems) {
                if (item.getItemCode() == ITEM_FIRMWARE_UPDATE) {
                    item.setLabelStatus(ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getUpdateNotification());
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFileManager = null;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_home;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.all_settings);

        mItems = new ArrayList<SettingsItem>() {{
            add(new SettingsItem(SECTION_GENERAL, R.string.all_general, 0));
            add(new SettingsItem(ITEM_WIFI, R.string.all_wifi, 0));
            // Hide language entry before copywriting has confirmed.
            add(new SettingsItem(ITEM_LANGUAGE, R.string.all_language, 0));

            add(new SettingsItem(SECTION_ABOUT, R.string.all_about, 0));
            add(new SettingsItem(ITEM_ABOUT_MACHINE, R.string.settings_about_machine, 0));
            add(new SettingsItem(ITEM_SECURITY, R.string.settings_terms_and_conditions, 0));
            add(new SettingsItem(ITEM_USER_PREFERENCE, R.string.settings_user_preference, 0));
            add(new SettingsItem(ITEM_FIRMWARE_UPDATE, R.string.settings_firmware_update, R.drawable.ic_new_38_20));
        }};

        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        boolean isRotaryAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
        switch (headType) {
            case Module.ModuleType.HEAD_3DP:
                mItems.add(new SettingsItem(SECTION_ADVANCED, R.string.settings_advanced_settings, 0));
                mItems.add(new SettingsItem(ITEM_ADVANCED_3DP_GUIDE, R.string.all_guides, 0));
                mItems.add(new SettingsItem(ITEM_ADVANCED_3DP, R.string.all_3d_printing, 0));
                break;
            case Module.ModuleType.HEAD_LASER:
                mItems.add(new SettingsItem(SECTION_ADVANCED, R.string.settings_advanced_settings, 0));
                if (isRotaryAvailable) {
                    mItems.add(new SettingsItem(ITEM_ADVANCE_ROTARY_LASER_GUIDE, R.string.all_guides, 0));
                } else {
                    mItems.add(new SettingsItem(ITEM_ADVANCED_LASER_GUIDE, R.string.all_guides, 0));
                }
                mItems.add(new SettingsItem(ITEM_ADVANCED_LASER, R.string.all_laser, 0));
                break;
            case Module.ModuleType.HEAD_LASER_10W:
                mItems.add(new SettingsItem(SECTION_ADVANCED, R.string.settings_advanced_settings, 0));
                // Only show Rotary Laser Guide when Rotary was plugged and available.
                if (isRotaryAvailable) {
                    mItems.add(new SettingsItem(ITEM_ADVANCE_ROTARY_LASER_GUIDE, R.string.all_guides, 0));
                } else {
                    mItems.add(new SettingsItem(ITEM_ADVANCED_LASER_10w_GUIDE, R.string.all_guides, 0));
                    mItems.add(new SettingsItem(ITEM_ADVANCED_LASER_10W, R.string.all_10w_laser, 0));
                }
                break;
            case Module.ModuleType.HEAD_CNC:
                mItems.add(new SettingsItem(SECTION_ADVANCED, R.string.settings_advanced_settings, 0));
                if (isRotaryAvailable) {
                    mItems.add(new SettingsItem(ITEM_ADVANCE_ROTARY_CNC_GUIDE, R.string.all_guides, 0));
                } else {
                    mItems.add(new SettingsItem(ITEM_ADVANCED_CNC_GUIDE, R.string.all_guides, 0));
                }
                break;

            default:
                break;
        }

        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            mItems.add(new SettingsItem(ITEM_ADVANCE_AIR_PURIFIER, R.string.all_air_purifier, 0));
        }

        // other advance settings
        mItems.add(new SettingsItem(ITEM_EXPORT_LOG, R.string.settings_export_log, 0));

        mAdapter = new ItemListAdapter(getContext());
        mAdapter.setItems(mItems);

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private boolean export(String fileName) {
        String diskPath = getContext().getCacheDir().getAbsolutePath();
        String folder = diskPath + File.separatorChar + "log";

        int fileNo = 0;
        File file;

        while (true) {
            String name = String.format("%s_%s.log", fileName, fileNo);
            file = new File(folder, name);

            if (!file.exists()) {
//                mFileManager.deviceHang();
                return true;
            }
            if (file.length() == 0) {
                fileNo++;
                continue;
            }

            // TODO: use IFile interface instead
            IFile rootFile = mFileManager.getRootFile();
            try {
                IFile logFile = mFileManager.search(rootFile.getAbsolutePath() + "//" + name);
                if (logFile != null) {
                    logFile.removeFile();
                }
                logFile = mFileManager.createFile(mFileManager.getRootFile(), name);
                try (OutputStream OutputStream = new FabFileOutputStream(logFile.getOutputStream().getOutputStream()).getOutputStream()) {

                    try (InputStream inputStream = new FileInputStream(file)) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buf)) > 0) {
                            OutputStream.write(buf, 0, len);
                        }
                    }
                }
            } catch (Throwable e) {
                LogHelper.log(e);
//                mFileManager.deviceHang();
                return false;
            }

            fileNo++;
        }
    }

    // Fixme: Did settings item should manage item label visibility?
    private static class SettingsItem {
        private int itemCode;
        private int nameRes;
        private int labelRes;
        private boolean labelStatus; // 0 not visible, 1 visible

        SettingsItem(int itemCode, @StringRes int nameRes, @DrawableRes int labelRes) {
            this.itemCode = itemCode;
            this.nameRes = nameRes;
            this.labelRes = labelRes;
            labelStatus = false;
        }

        public int getItemCode() {
            return itemCode;
        }

        public int getNameRes() {
            return nameRes;
        }

        public int getLabelRes() {
            return labelRes;
        }

        public boolean getLabelStatus() {
            return labelStatus;
        }

        public void setLabelStatus(boolean status) {
            labelStatus = status;
        }
    }

    private class ItemListAdapter extends BaseAdapter {
        private Context context;
        private List<SettingsItem> items;

        ItemListAdapter(Context context) {
            this.context = context;
        }

        void setItems(List<SettingsItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public SettingsItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (getItem(position).getItemCode() % 100 == 0) ? 0 : 1;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == 1;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            SettingsItem item = getItem(position);
            int viewType = getItemViewType(position);

            if (convertView == null) {
                if (viewType == 0) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_a400_settings_section, parent, false);
                    convertView.setEnabled(false);
                } else {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_settings_item, parent, false);
                }
            }

            switch (viewType) {
                case 0: {
                    TextView tvSectionTitle = convertView.findViewById(R.id.tv_settings_section_title);
                    tvSectionTitle.setText(item.getNameRes());
                    break;
                }
                case 1: {
                    Button button = convertView.findViewById(R.id.btn_settings_item_name);
                    TextView tvTitle = convertView.findViewById(R.id.tv_settings_item_title);
                    TextView tvDesc = convertView.findViewById(R.id.tv_settings_item_desc);

                    // TODO
                    tvTitle.setText(item.getNameRes());
                    tvDesc.setText(null);

                    // icon
                    ImageView ivIcon = convertView.findViewById(R.id.iv_settings_item_icon);
                    ImageView ivLabel = convertView.findViewById(R.id.iv_settings_item_label);

                    ivLabel.setVisibility(item.getLabelStatus() ? ImageView.VISIBLE : ImageView.GONE);
                    ivLabel.setImageResource(item.getLabelRes());

                    switch (item.getItemCode()) {
                        case ITEM_WIFI: {
                            ivIcon.setImageResource(R.drawable.ic_settings_wifi_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoWiFiList();
                                }
                            });
                            break;
                        }
                        case ITEM_LANGUAGE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_item_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoSettingsLanguage();
                                }
                            });
                            break;
                        }
                        case ITEM_ABOUT_MACHINE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_info_20x20);
                            button.setOnClickListener(v -> {
                                ServiceContainer.getInstance().getService(IRouter.class).routeToAboutPage().start(getContext());
                            });
                            break;
                        }
                        case ITEM_SECURITY: {
                            ivIcon.setImageResource(R.drawable.ic_settings_terms_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoSettingsSecurity();
                                }
                            });
                            break;
                        }
                        case ITEM_USER_PREFERENCE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_item_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoSettingsPreference();
                                }
                            });
                            break;
                        }
                        case ITEM_FIRMWARE_UPDATE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_item_20x20);
                            button.setOnClickListener(v -> {
                                // TODO: Temporary fix here. We need to update SettingsHomeFragment View after back from firmware page,
                                //  but fragment lifecycle is followed with the Activity.
                                ServiceContainer.getInstance().getService(IRouter.class).routeToSettingsFirmwarePage().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCED_3DP: {
                            ivIcon.setImageResource(R.drawable.ic_settings_3dp_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoAdvanced3DP();
                                }
                            });
                            break;
                        }
                        case ITEM_ADVANCED_3DP_GUIDE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_guide_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Guide to 3DP.");
                                ServiceContainer.getInstance().getService(IRouter.class).routeToGuide3DP().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCED_LASER: {
                            ivIcon.setImageResource(R.drawable.ic_settings_laser_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoAdvancedLaser();
                                }
                            });
                            break;
                        }
                        case ITEM_ADVANCED_LASER_10W: {
                            ivIcon.setImageResource(R.drawable.ic_settings_laser_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.gotoAdvancedLaser10w();
                                }
                            });
                            break;
                        }
                        case ITEM_ADVANCED_LASER_GUIDE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_guide_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Guide to Laser.");
                                ServiceContainer.getInstance().getService(IRouter.class).routeToGuideLaser().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCED_LASER_10w_GUIDE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_guide_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Guide to 10w Laser.");
                                ServiceContainer.getInstance().getService(IRouter.class).routeToGuide10WLaser().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCE_ROTARY_LASER_GUIDE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_guide_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Guide to Rotary Laser.");
                                ServiceContainer.getInstance().getService(IRouter.class).routeToGuideRotaryLaser().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCED_CNC_GUIDE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_guide_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Guide to CNC.");
                                ServiceContainer.getInstance().getService(IRouter.class).routeToGuideCNC().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCE_ROTARY_CNC_GUIDE: {
                            ivIcon.setImageResource(R.drawable.ic_settings_guide_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Guide to Rotary CNC.");
                                ServiceContainer.getInstance().getService(IRouter.class).routeToGuideRotaryCNC().start(getContext());
                            });
                            break;
                        }
                        case ITEM_ADVANCE_AIR_PURIFIER: {
                            ivIcon.setImageResource(R.drawable.ic_settings_air_purifier_20x20);
                            button.setOnClickListener(v -> {
                                SettingsActivity activity = (SettingsActivity) getContext();
                                if (activity != null) {
                                    activity.startAdvanceAirPurifierPage();
                                }
                            });
                            break;
                        }
                        case ITEM_EXPORT_LOG: {
                            ivIcon.setImageResource(R.drawable.ic_settings_item_20x20);
                            button.setOnClickListener(v -> {
                                Logger.i("Export log to U-disk.");
                                button.setEnabled(false);
                                // TODO
//                                mFileManager.mount()
//                                        .observeOn(Schedulers.io())
//                                        .map(success -> {
//                                            boolean isSCExport = false;
//                                            boolean isFWExport = false;
//                                            if (success) {
//                                                isSCExport = export("SC");
//                                                isFWExport = export("FW");
//                                            }
//                                            return isSCExport && isFWExport;
//                                        })
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .map(success -> {
//                                            mFileManager.unmount();
//                                            return success;
//                                        })
//                                        .as(bindToLifecycle())
//                                        .subscribe(success -> {
//                                            if (success) {
//                                                FabConfirm.create(getContext())
//                                                        .setDescription(R.string.settings_export_log_done)
//                                                        .setConfirm(R.string.all_ok, (dialog, which) -> {
//                                                            dialog.dismiss();
//                                                        })
//                                                        .show();
//                                            } else {
//                                                FabConfirm.create(getContext())
//                                                        .setDescription(R.string.settings_export_log_failed)
//                                                        .setConfirm(R.string.all_ok, (dialog, which) -> {
//                                                            dialog.dismiss();
//                                                        })
//                                                        .show();
//                                            }
//                                            button.setEnabled(true);
//                                        }, e -> {
//                                            LogHelper.log(e);
//                                            FabConfirm.create(getContext())
//                                                    .setDescription(R.string.settings_export_log_failed)
//                                                    .setConfirm(R.string.all_ok, (dialog, which) -> {
//                                                        dialog.dismiss();
//                                                    })
//                                                    .show();
//                                            button.setEnabled(true);
//                                        });
                            });
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                }
                default:
                    break;
            }

            return convertView;
        }
    }
}
