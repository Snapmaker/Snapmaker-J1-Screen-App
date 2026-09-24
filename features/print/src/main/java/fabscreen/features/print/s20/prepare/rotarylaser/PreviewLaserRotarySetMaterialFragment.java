package fabscreen.features.print.s20.prepare.rotarylaser;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.helper.EditTextHelper;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PreviewLaserRotarySetMaterialFragment extends BaseFragment {
    @BindView(R2.id.et_laser_calibration_4axis_set_workpiece_diameter)
    EditText mEtWorkpieceDiameter;
    @BindView(R2.id.et_laser_calibration_4axis_set_workpiece_length)
    EditText mEtWorkpieceLength;
    @BindView(R2.id.tv_laser_calibration_4axis_set_workpiece_diameter_tip)
    TextView mTvDiameterTip;
    @BindView(R2.id.tv_laser_calibration_4axis_set_workpiece_length_tip)
    TextView mTvLengthTip;
    @BindView(R2.id.btn_laser_calibration_4axis_set_workpiece_next)
    Button mBtnNext;
    private LaserCalibrationViewModel mViewModel;
    private CoordinateSystemPresenter mCoordinateSystemPresenter;

    public static PreviewLaserRotarySetMaterialFragment newInstance() {
        return new PreviewLaserRotarySetMaterialFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.cnc_origin_assistant_material_settings);

        initView();

        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(1);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_4axis_set_workpiece;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    @Override
    protected void back() {
        hideKeyboard();
        super.back();
    }

    private void hideKeyboard() {
        if (getView() == null) return;
        if (getContext() == null) return;

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
        }
    }

    private void initView() {
        mBtnNext.setEnabled(false);

        mEtWorkpieceLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        mEtWorkpieceDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        if (getViewModel().getWorkpieceDiameter() > 0) {
            mEtWorkpieceDiameter.setText(String.valueOf(getViewModel().getWorkpieceDiameter()));
        }

        if (getViewModel().getWorkpieceLength() > 0) {
            mEtWorkpieceLength.setText(String.valueOf(getViewModel().getWorkpieceLength()));
        }
        mEtWorkpieceDiameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = EditTextHelper.fixNumberInputSinglePoint(s).toString();
                mViewModel.setWorkpieceDiameterInput(input);
            }
        });

        mEtWorkpieceLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = EditTextHelper.fixNumberInputSinglePoint(s).toString();
                mViewModel.setWorkpieceLengthInput(input);
            }
        });

        mViewModel.getWorkpieceDiameterTipObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tip -> {
                    switch (tip) {
                        case TIP_OK:
                            mTvDiameterTip.setVisibility(TextView.GONE);
                            mTvDiameterTip.setText("");
                            break;
                        case TIP_NOT_POSITIVE_NUMBER:
                            mTvDiameterTip.setVisibility(TextView.VISIBLE);
                            mTvDiameterTip.setText(R.string.cnc_origin_assistant_input_tip_not_positive);
                            break;
                        case TIP_EMPTY:
                            mTvDiameterTip.setVisibility(TextView.VISIBLE);
                            mTvDiameterTip.setText(R.string.cnc_origin_assistant_input_tip_empty);
                            break;
                    }
                });

        mViewModel.getWorkpieceLengthTipObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tip -> {
                    switch (tip) {
                        case TIP_OK:
                            mTvLengthTip.setVisibility(TextView.GONE);
                            mTvLengthTip.setText("");
                            break;
                        case TIP_NOT_POSITIVE_NUMBER:
                            mTvLengthTip.setVisibility(TextView.VISIBLE);
                            mTvLengthTip.setText(R.string.cnc_origin_assistant_input_tip_not_positive);
                            break;
                        case TIP_EMPTY:
                            mTvLengthTip.setVisibility(TextView.VISIBLE);
                            mTvLengthTip.setText(R.string.cnc_origin_assistant_input_tip_empty);
                            break;
                    }
                });

        mViewModel.getMaterialInputReady()
                .debounce(200, Constants.TIME_UNIT)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isReady -> {
                    mBtnNext.setEnabled(isReady);
                });
    }

    @OnClick(R2.id.btn_laser_calibration_4axis_set_workpiece_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((PreviewActivity) getActivity()).gotoLaserRotaryInstallMaterial();
        }
    }
}
