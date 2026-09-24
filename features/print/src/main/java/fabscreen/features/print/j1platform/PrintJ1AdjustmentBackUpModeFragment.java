package fabscreen.features.print.j1platform;

import static fabscreen.platform.base.lib.print.IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class PrintJ1AdjustmentBackUpModeFragment extends BaseFragment {
    @BindView(R2.id.tv_back_up_mode_status)
    TextView mTvBackUpStatus;
    @BindView(R2.id.lin_j1_print_asdjustment_switch)
    LinearLayout mLinAdjustmentSwitch;
    @BindView(R2.id.iv_j1_print_asdjustment_switch_pic)
    ImageView mIvPower;
    @BindView(R2.id.iv_j1_print_asdjustment_light_pic)
    ImageView mIvLight;

    private boolean mIsOpen = false;

    private final BehaviorSubject<Boolean> mBackUpModeSubject = BehaviorSubject.createDefault(false);

    public static Fragment newInstance() {
        return new PrintJ1AdjustmentBackUpModeFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController()
                .getPrintModeStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(printMode -> {
                    Logger.d("Print Mode " + printMode);
                    mBackUpModeSubject.onNext(((int) printMode) == PRINT_MODE_DUAL_EXTRUDER_BACK_UP);
                });
    }

    private void initView() {
        mBackUpModeSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isBackUp -> {
                    mIsOpen = isBackUp;
                    touchSwitch();
                });
    }

    public void touchSwitch() {
        mLinAdjustmentSwitch.setBackgroundResource(mIsOpen ? R.drawable.pic_tab_horizontal_right_440x104 : R.drawable.pic_tab_bg_horizontal_left_440x104);
        mIvPower.setImageResource(mIsOpen ? R.drawable.icon_off_normal_64x64 : R.drawable.icon_off_checked_64x64);
        mIvLight.setImageResource(mIsOpen ? R.drawable.icon_backup_checked_64x64 : R.drawable.icon_backup_normal_64x64);
        mTvBackUpStatus.setText(mIsOpen ? R.string.all_on : R.string.all_off);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_print_adjustment_back_up;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController().unsubscribePrintModeStatus().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController().subscribePrintModeStatus().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    @OnClick({R2.id.iv_j1_print_asdjustment_switch_pic, R2.id.iv_j1_print_asdjustment_light_pic})
    void onClickSwitch(View view) {
        playNormalClickSound();
        if (view.getId() == R.id.iv_j1_print_asdjustment_switch_pic) {
            if (!mIsOpen) {
                return;
            }
            changSwitchType();
        } else if (view.getId() == R.id.iv_j1_print_asdjustment_light_pic) {
            if (mIsOpen) {
                return;
            }
            changSwitchType();
        }

    }

    public void changSwitchType() {
        int mode = mBackUpModeSubject.getValue() ? 0 : 1;
        ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController().requestChangePrintMode(mode)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resultStructure -> {
                }, LogHelper::log);
    }

}
