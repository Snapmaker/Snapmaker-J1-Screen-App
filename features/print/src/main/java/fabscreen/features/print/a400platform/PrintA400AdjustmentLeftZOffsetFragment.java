package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.FDMZOffsetStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.ZOffsetInfo;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class PrintA400AdjustmentLeftZOffsetFragment extends BaseFragment {

    @BindView(R2.id.tv_offset_value)
    TextView mTvValue;
    @BindView(R2.id.sbg_z_offset_steps)
    SegmentedButtonGroup mSbSteps;

    private IMachine mJ1Machine;

    private BehaviorSubject<Float> mZOffsetSubject = BehaviorSubject.createDefault(0f);
    private float mOffsetSize = 0.05f;

    public static Fragment newInstance() {
        return new PrintA400AdjustmentLeftZOffsetFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJ1Machine = ServiceContainer.getInstance().getService(IMachine.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        // get Z Offset
        // 0xa0 0x16 input uint8 key, output uint8 result
        if (getParentFragment() instanceof PrintA400AdjustmentContainerFragment) {
            ((PrintA400AdjustmentContainerFragment) getParentFragment()).setModelTitle(getString(R.string.a400_print_print_setting_left_z_offset));
        }

        mSbSteps.setPosition(1, false);
        mSbSteps.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    mOffsetSize = 0.02f;
                    break;
                case 1:
                    mOffsetSize = 0.05f;
                    break;
                case 2:
                    mOffsetSize = 0.01f;
                    break;
            }
        });
        mJ1Machine.getFDMController().getZOffset(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        FDMZOffsetStructure fdmzOffsetStructure = new FDMZOffsetStructure();
                        fdmzOffsetStructure.readBuffer(new Buffer().write(responseStructure.dataProp.toByteArray()));
                        List<ZOffsetInfo> zOffsetInfoList = fdmzOffsetStructure.getZOffsetInfoList();
                        if (zOffsetInfoList == null || zOffsetInfoList.size() == 0) return;
                        ZOffsetInfo zOffsetInfo = zOffsetInfoList.get(0);
                        mZOffsetSubject.onNext(zOffsetInfo.getZOffset());
                    }
                }, LogHelper::log);

        // set Z Offset
        // 0xa0 0x15 input uint8 key, uint8 extruderIndex, float zoffset value, output uint8 result

        mZOffsetSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(zOffset -> {
                    mTvValue.setText(String.valueOf(zOffset));
                });

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_adjustment_z_offset;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R2.id.btn_z_offset_down)
    void onClickZOffsetDown() {
        playNormalClickSound();
        mJ1Machine.getFDMController().setZOffset(0, 0, mZOffsetSubject.getValue() - mOffsetSize)
                .filter(ResponseStructure::isSuccess)
                .flatMap(responseStructure -> mJ1Machine.getFDMController().getZOffset(0))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        FDMZOffsetStructure fdmzOffsetStructure = new FDMZOffsetStructure();
                        fdmzOffsetStructure.readBuffer(new Buffer().write(responseStructure.dataProp.toByteArray()));
                        List<ZOffsetInfo> zOffsetInfoList = fdmzOffsetStructure.getZOffsetInfoList();
                        if (zOffsetInfoList == null || zOffsetInfoList.size() == 0) return;
                        ZOffsetInfo zOffsetInfo = zOffsetInfoList.get(0);
                        mZOffsetSubject.onNext(zOffsetInfo.getZOffset());
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_z_offset_up)
    void onClickZOffsetUp() {
        playNormalClickSound();
        mJ1Machine.getFDMController().setZOffset(0, 0, mZOffsetSubject.getValue() + mOffsetSize)
                .filter(ResponseStructure::isSuccess)
                .flatMap(responseStructure -> mJ1Machine.getFDMController().getZOffset(0))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        FDMZOffsetStructure fdmzOffsetStructure = new FDMZOffsetStructure();
                        fdmzOffsetStructure.readBuffer(new Buffer().write(responseStructure.dataProp.toByteArray()));
                        List<ZOffsetInfo> zOffsetInfoList = fdmzOffsetStructure.getZOffsetInfoList();
                        if (zOffsetInfoList == null || zOffsetInfoList.size() == 0) return;
                        ZOffsetInfo zOffsetInfo = zOffsetInfoList.get(0);
                        mZOffsetSubject.onNext(zOffsetInfo.getZOffset());
                    }
                }, LogHelper::log);
    }
}
