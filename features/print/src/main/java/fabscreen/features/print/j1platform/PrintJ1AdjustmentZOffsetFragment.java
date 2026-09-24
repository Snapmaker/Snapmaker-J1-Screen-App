package fabscreen.features.print.j1platform;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.FDMZOffsetStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.ZOffsetInfo;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import okio.Buffer;

public class PrintJ1AdjustmentZOffsetFragment extends BaseFragment {

    @BindView(R2.id.tv_offset_value)
    TextView mTvZOffsetValue;

    @BindView(R2.id.ll_z_offset_range)
    LinearLayout mLlMovingRange;
    @BindView(R2.id.tv_range_0_01)
    TextView mTvRangePoint01;
    @BindView(R2.id.tv_range_0_05)
    TextView mTvRangePoint05;

    int mPrimaryColor;
    int mUncheckColor;
    int mDisabledColor;

    private float mStepWidthPos = 0.05f;

    private IMachine mJ1Machine;
    private final BehaviorSubject<Float> mZOffsetSubject = BehaviorSubject.createDefault(0f);
    private final PublishSubject<Boolean> mClickEventSubj = PublishSubject.create();

    public static Fragment newInstance() {
        return new PrintJ1AdjustmentZOffsetFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJ1Machine = ServiceContainer.getInstance().getService(IMachine.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getColor();
        initView();
        saveConfig();
    }

    private void saveConfig() {
        mClickEventSubj.hide()
                .throttleLast(1, TimeUnit.SECONDS)
                .as(bindToLifecycle())
                .subscribe(clicked -> mJ1Machine.getMachineController().saveConfigToFlash(), LogHelper::log);
    }
    private void getColor() {
        mPrimaryColor = getValueOfColorAttr(R.attr.theme_color_primary);
        mUncheckColor = getValueOfColorAttr(R.attr.theme_color_tab_uncheck);
        mDisabledColor = getValueOfColorAttr(R.attr.theme_color_disabled);
    }

    private int getValueOfColorAttr(@AttrRes int attrId) {
        TypedValue typedValue = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(attrId, typedValue, true)) {
            return typedValue.data;
        } else {
            return Color.TRANSPARENT;
        }
    }

    private void initView() {
        // get Z Offset
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
                    String zOffsetStr = String.valueOf(zOffset);
                    if (zOffset > 0) {
                        zOffsetStr = "+" + zOffsetStr;
                    }
                    mTvZOffsetValue.setText(zOffsetStr);
                });

        onClickMovingRangePoint05();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_print_adjustment_z_offset;
    }

    @OnClick(R2.id.tv_range_0_01)
    void onClickMovingRangePoint01() {
        mStepWidthPos = 0.01f;
        mTvRangePoint01.setTextColor(mPrimaryColor);
        mTvRangePoint05.setTextColor(mUncheckColor);
        mLlMovingRange.setBackgroundResource(R.drawable.pic_tab_bg_horizontal_left_440x104);
    }

    @OnClick(R2.id.tv_range_0_05)
    void onClickMovingRangePoint05() {
        mStepWidthPos = 0.05f;
        mTvRangePoint05.setTextColor(mPrimaryColor);
        mTvRangePoint01.setTextColor(mUncheckColor);
        mLlMovingRange.setBackgroundResource(R.drawable.pic_tab_horizontal_right_440x104);
    }

    @OnClick(R2.id.iv_z_offset_bed_up)
    void onClickZOffsetBedUp() {
        // value minus, bed up
        mJ1Machine.getFDMController().setZOffset(0, 0, mZOffsetSubject.getValue() - mStepWidthPos)
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
        mClickEventSubj.onNext(true);
    }

    @OnClick(R2.id.iv_z_offset_bed_down)
    void onClickZOffsetBedDown() {
        // value plus, bed down
        mJ1Machine.getFDMController().setZOffset(0, 0, mZOffsetSubject.getValue() + mStepWidthPos)
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
        mClickEventSubj.onNext(true);
    }
}
