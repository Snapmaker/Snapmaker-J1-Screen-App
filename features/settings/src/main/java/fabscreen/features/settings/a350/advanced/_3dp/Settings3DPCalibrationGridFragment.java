package fabscreen.features.settings.a350.advanced._3dp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class Settings3DPCalibrationGridFragment extends BaseFragment {
    @BindView(R2.id.tv_set_calibration_grid_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_set_calibration_grid_value)
    TextView mTvValue;
    @BindView(R2.id.sbg_calibration_grid)
    SegmentedButtonGroup mSbgCalibration;
    @BindView(R2.id.iv_calibration_grid)
    ImageView mIvCalibrationGrid;
    private int mGrid = 3;
    private BehaviorSubject<Integer> mGridValueSubject = BehaviorSubject.createDefault(3);

    public static Settings3DPCalibrationGridFragment getInstance() {
        return new Settings3DPCalibrationGridFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.settings_3dp_calibration_grid);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_3dp_calibration_grid;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        int pos = 0;
        // get initial position
        mGrid = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationGrid();
        mGridValueSubject.onNext(mGrid);

        switch (mGrid) {
            case 3:
                pos = 0;
                break;
            case 4:
                pos = 1;
                break;
            case 5:
                pos = 2;
                break;
        }
        mSbgCalibration.setPosition(pos, false);

        // init step buttons
        mSbgCalibration.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    mGrid = 3;
                    break;
                case 1:
                    mGrid = 4;
                    break;
                case 2:
                    mGrid = 5;
                    break;
            }
            mGridValueSubject.onNext(mGrid);
        });

        mGridValueSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> {
                    mTvValue.setText(String.format(Locale.getDefault(), "%d × %d", value, value));
                    switch (value) {
                        case 3:
                            mIvCalibrationGrid.setImageResource(R.drawable.pic_3dp_calibration_9point_280x280);
                            break;
                        case 4:
                            mIvCalibrationGrid.setImageResource(R.drawable.pic_3dp_calibration_16point_280x280);
                            break;
                        case 5:
                            mIvCalibrationGrid.setImageResource(R.drawable.pic_3dp_calibration_25point_280x280);
                            break;
                    }
                });
    }

    @OnClick(R2.id.btn_3dp_calibration_grid_save)
    void onClickSave() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().set3DPCalibrationGrid(mGridValueSubject.getValue());
        back();
    }
}
