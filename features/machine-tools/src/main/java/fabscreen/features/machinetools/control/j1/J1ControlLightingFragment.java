package fabscreen.features.machinetools.control.j1;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.J1ControlLightingViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1ControlLightingFragment extends BaseFragment {
    @BindView(R2.id.tv_lighting_value_current)
    TextView mTvLightingCurrentValue;
    @BindView(R2.id.cas_j1_control_lighting)
    CustomArcSeekBar mCasEnclosureLighting;

    IMachine mMachine;

    private J1ControlLightingViewModel mViewModel;
    private static final int ENCLOSURE_LIGHTING_MAX_VALUE = 100;
    private static final int ENCLOSURE_LIGHTING_MIN_VALUE = 0;

    public static Fragment newInstance() {
        return new J1ControlLightingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getFragmentScopeViewModel(J1ControlLightingViewModel.class);
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

    }

    private void initView() {

        Enclosure enclosure = mMachine.getMachineController().getEnclosure();
        if (enclosure == null) return;

        int ledValue = enclosure.getEnclosureStatusValue().getLedValue();
        int ledValuePercent = Math.round((ledValue * 10f / 255f ) * 100 / 10f);
        mCasEnclosureLighting.setProgress(ledValuePercent + ENCLOSURE_LIGHTING_MIN_VALUE);
        mTvLightingCurrentValue.setText(ledValuePercent + "");

        mCasEnclosureLighting.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvLightingCurrentValue.setText((progress + ENCLOSURE_LIGHTING_MIN_VALUE) + "");
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                int v = Math.round((ENCLOSURE_LIGHTING_MIN_VALUE + customArcSeekBar.getProgress()) * 10f / 100f * 255 / 10f);
                mViewModel.setEnclosureLedValue(v)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(responseStructure -> {

                        }, LogHelper::log);
            }
        });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control_lighting;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMachine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mViewModel.unsubscribeEnclosure();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMachine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mViewModel.subscribeEnclosure();
        }
    }

}
