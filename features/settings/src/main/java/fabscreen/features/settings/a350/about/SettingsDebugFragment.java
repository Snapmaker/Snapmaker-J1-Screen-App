package fabscreen.features.settings.a350.about;

import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.BindView;
import butterknife.OnItemSelected;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockResponsePacketBuilder;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

@Route(path = RoutePath.SETTINGS_ABOUT_)
public class SettingsDebugFragment extends BaseFragment {
    @BindView(R2.id.sp_about_rotating_changed_head_status)
    Spinner mSpRotating;
    @BindView(R2.id.sp_about_toolhead_changed)
    Spinner mSpHeadStatus;
    @BindView(R2.id.sp_about_machine_changed)
    Spinner mSpMachineStatus;

    @BindView(R2.id.tv_about_toolhead_changed_head_status)
    TextView mTvHeadStatus;
    @BindView(R2.id.tv_about_machine_changed)
    TextView mTvModelStatus;
    @BindView(R2.id.tv_about_status_data)
    TextView mTvStatusData;

//    @BindView(R2.id.btn_about_toolhead_changed_3dp)
//    Button mBtn3DP;
//    @BindView(R2.id.btn_about_toolhead_changed_laser)
//    Button mBtnLaser;
//    @BindView(R2.id.btn_about_toolhead_changed_cnc)
//    Button mBtnCNC;
//    @BindView(R2.id.btn_about_model_changed_a400)
//    Button mBtnA400;
//    @BindView(R2.id.btn_about_model_changed_j1)
//    Button mBtnJ1;


    private BehaviorSubject<Integer> mHeadStatusSubject = BehaviorSubject.createDefault(0);
    private BehaviorSubject<Integer> mModelStatusSubject = BehaviorSubject.createDefault(0);

    public static SettingsDebugFragment newInstance() {
        return new SettingsDebugFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    mSpMachineStatus.setSelection(machineInfo.seriesId);
                });
        mSpHeadStatus.setSelection(MachineStatusManager.getMachineInfoHolder().getValue().headStatus);

        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    mSpRotating.setSelection(machineInfo.isRotaryAvailable ? 1 : 0);
                });
    }

    @OnItemSelected(R2.id.sp_about_rotating_changed_head_status)
    public void spinnerRotatingSelected(Spinner spinner, int position) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setRotaryAvailable(position);
    }

    @OnItemSelected(R2.id.sp_about_toolhead_changed)
    public void spinnerHeadStatusSelected(Spinner spinner, int position) {
        MockResponsePacketBuilder.getInstance().setHeadType(position);
    }

    @OnItemSelected(R2.id.sp_about_machine_changed)
    public void spinnerMachineSelected(Spinner spinner, int position) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setMachineType(position);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_about;
    }

}
