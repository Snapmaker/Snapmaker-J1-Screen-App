package fabscreen.features.machinetools.calibration.j1Platform;

import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.core.ui.common.leftsection.J1LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1CalibrationFragment extends SectionAndDetailContainerFragment {
    private long mLastClickTime;
    private int mClickCount;
    private boolean isGuide = false;
    @BindView(R2.id.top_bar_back)
    Button button;

    public static Fragment newInstance() {
        return new J1CalibrationFragment();
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        if (getArguments() != null) {
            isGuide = getArguments().getBoolean("is_guide", false);
        }
        List<SectionItem> items = new ArrayList<>();
        items.add(new SectionItem(requireContext(), R.string.calibration_heated_bed_leveing_title, J1CalibrationModeFragment.newInstance(J1CalibrationMode.J1CalibrationModeIndex.HEATED_BED_LEVELING, isGuide)));
        items.add(new SectionItem(requireContext(), R.string.calibration_Z_offset_calibration_title, J1CalibrationModeFragment.newInstance(J1CalibrationMode.J1CalibrationModeIndex.Z_OFFSET_CALIBRATION, isGuide)));
        items.add(new SectionItem(requireContext(), R.string.calibration_XY_offset_calibration_title, J1CalibrationModeFragment.newInstance(J1CalibrationMode.J1CalibrationModeIndex.XY_OFFSET_CALIBRATION, isGuide)));
        items.add(new SectionItem(requireContext(), R.string.calibration_calibration_check_title, J1CalibrationModeFragment.newInstance(J1CalibrationMode.J1CalibrationModeIndex.CALIBRATION_CHECK, isGuide), 3));
        items.add(new SectionItem(requireContext(), R.string.j1_calibration_vibration_calibration, J1CalibrationModeFragment.newInstance(J1CalibrationMode.J1CalibrationModeIndex.CALIBRATION_VIBRATION, isGuide), 3));
        return items;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_section_and_detail_container;
    }

    @Override
    protected LeftSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new J1LeftSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return "Calibration";
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed().setZoneTargetTemperature(0, 0)
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getFDMController().stopExtruderHeat())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                }, LogHelper::log);
        if (isGuide) {
            button.setVisibility(View.INVISIBLE);
            IPreferences.Helper helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
            if (!helper.getGuideLevelingBed()) {
                setSelection(0);
            } else if (!helper.getGuideLevelingZ()) {
                setSelection(1);
            } else if (!helper.getGuideLevelingXY()) {
                setSelection(2);
            } else if (!helper.getGuideCheckPrint()) {
                setSelection(3);
            } else if (!helper.getGuideVibrationCompensation()) {
                setSelection(4);
            }  else {
                ((J1CalibrationActivity) requireActivity()).gotoGuideSuccess();
            }
        }
    }

    @OnClick(R2.id.btn_invisible_door)
    void onDoorClick() {
        long curTime = SystemClock.elapsedRealtime();
        if (curTime - mLastClickTime < 500) {
            mClickCount++;
        } else {
            mClickCount = 1;
        }
        mLastClickTime = curTime;
        if (mClickCount >= 5 && isGuide) {
            // backdoor open!
            Logger.i("User open backdoor to Settings!");
            mRouter.routeToSettingsPage().start(requireContext());
        }
    }
}
