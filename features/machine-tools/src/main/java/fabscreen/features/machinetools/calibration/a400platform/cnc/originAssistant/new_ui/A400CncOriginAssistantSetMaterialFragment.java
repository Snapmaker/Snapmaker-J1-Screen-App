package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.new_ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.A400CNCOriginAssistantViewModel;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.helper.EditTextHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400CncOriginAssistantSetMaterialFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.et_cnc_origin_assistant_set_material_diameter)
    EditText mEtWorkpieceDiameter;
    @BindView(R2.id.et_cnc_origin_assistant_set_material_length)
    EditText mEtWorkpieceLength;
    @BindView(R2.id.tv_cnc_origin_assistant_set_material_diameter_tip)
    TextView mTvDiameterTip;
    @BindView(R2.id.tv_cnc_origin_assistant_set_material_length_tip)
    TextView mTvLengthTip;
    @BindView(R2.id.btn_cnc_origin_assistant_set_material_next)
    Button mBtnNext;
    private A400CNCOriginAssistantViewModel mViewModel;

    public static A400CncOriginAssistantSetMaterialFragment newInstance() {
        return new A400CncOriginAssistantSetMaterialFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();

        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        setTitle(R.string.calibration_cnc_origin_assistant);
        mTvTopBarContent.setText("设置材料（1/9）");
        mGuideProgressBar.setMax(9);
        mGuideProgressBar.setProgress(1);
        // We need to disabled next button first before user start inputting.
        mBtnNext.setEnabled(false);

        initEditText();

        // Skip the first value.
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
                        default:
                            break;
                    }
                });

        // Skip the first value.
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
                        default:
                            break;
                    }
                });

        mViewModel.getMaterialInputReady()
                .debounce(200, Constants.TIME_UNIT)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isReady -> mBtnNext.setEnabled(isReady));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_origin_assistant_set_materia_landl;
    }

    @Override
    protected A400CNCOriginAssistantViewModel getViewModel() {
        return getViewModelProvider().get(A400CNCOriginAssistantViewModel.class);
    }


    private void hideKeyboard() {
        if (getView() == null) return;
        if (getContext() == null) return;

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
        }
    }

    private void initEditText() {
        // Limit the length of input.
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
    }

    @OnClick(R2.id.btn_cnc_origin_assistant_set_material_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((A400OriginAssistantActivity) getActivity()).gotoCNCOriginAssistantInstallMaterialFragment();
        }
    }
}
