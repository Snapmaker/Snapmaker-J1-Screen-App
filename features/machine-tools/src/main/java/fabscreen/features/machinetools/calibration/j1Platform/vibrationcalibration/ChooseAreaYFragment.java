package fabscreen.features.machinetools.calibration.j1Platform.vibrationcalibration;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ChooseAreaYFragment extends BaseFragment {

    private VibrationCalibrationViewModel mViewModel;

    public static Fragment newInstance() {
        return new ChooseAreaYFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_vibration_choose_area;
    }

    @BindView(R2.id.tv_choose_best_title)
    TextView mTvTitle;
    @BindView(R2.id.rg_areas)
    RadioGroup mRgAreas;
    @BindView(R2.id.btn_next)
    Button mBtnNext;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(VibrationCalibrationViewModel.class);
        initView();
    }

    private void initView() {
        mTvTitle.setText(R.string.j1_calibration_vibration_choose_y_best_area_title);
        mRgAreas.setOnCheckedChangeListener((group, checkedId) -> {
            if (!mBtnNext.isEnabled()) {
                mBtnNext.setEnabled(true);
            }
        });
    }

    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        int checkedIndex = mRgAreas.indexOfChild(mRgAreas.findViewById(mRgAreas.getCheckedRadioButtonId()));
        Logger.d("User select area index %d.", checkedIndex);
        mBtnNext.setEnabled(false);
        mViewModel.enableAndSetCompensationFreq(2, checkedIndex)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handleResult, e -> {
                    handleResult(false);
                    LogHelper.log(e);
                });
    }

    private void handleResult(Boolean success) {
        if (!success) {
            Logger.e("Set compensation frequency fail!");
            mBtnNext.setEnabled(true);
            return;
        }

        if (requireActivity() instanceof VibrationCalibrationActivity) {
            ((VibrationCalibrationActivity) requireActivity()).goToChooseXArea();
        }
    }
}
