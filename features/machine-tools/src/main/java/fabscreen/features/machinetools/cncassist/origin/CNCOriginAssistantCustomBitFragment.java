package fabscreen.features.machinetools.cncassist.origin;

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
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.helper.EditTextHelper;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CNCOriginAssistantCustomBitFragment extends BaseFragment {
    @BindView(R2.id.et_cnc_origin_assistant_custom_bit_length)
    EditText mEtLength;
    @BindView(R2.id.et_cnc_origin_assistant_custom_bit_diameter)
    EditText mEtDiameter;
    @BindView(R2.id.tv_cnc_origin_assistant_custom_bit_diameter_tip)
    TextView mTvDiameterTip;
    @BindView(R2.id.tv_cnc_origin_assistant_custom_bit_length_tip)
    TextView mTvLengthTip;
    @BindView(R2.id.btn_cnc_origin_assistant_custom_bit_next)
    Button mBtnNext;
    private CNCOriginAssistantViewModel mViewModel;

    public static CNCOriginAssistantCustomBitFragment newInstance() {
        return new CNCOriginAssistantCustomBitFragment();
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

        setTitle(R.string.cnc_origin_assistant_custom_bit);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_origin_assistant_custom_bit;
    }

    @Override
    protected CNCOriginAssistantViewModel getViewModel() {
        return getViewModelProvider().get(CNCOriginAssistantViewModel.class);
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
        // We need to disabled next button first before user start inputting.
        mBtnNext.setEnabled(false);

        initEditText();

        // Skip the first value.
        mViewModel.getBitLengthTipObservable()
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

        // Skip the first value.
        mViewModel.getBitDiameterTipObservable()
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

        mViewModel.getCustomBitInputReady()
                .debounce(200, Constants.TIME_UNIT)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isReady -> mBtnNext.setEnabled(isReady));
    }

    private void initEditText() {
        // Limit the length of input.
        mEtLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        mEtDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

        if (getViewModel().getBitLength() > 0) {
            mEtLength.setText(String.valueOf(getViewModel().getBitLength()));
        }

        if (getViewModel().getBitDiameter() > 0) {
            mEtDiameter.setText(String.valueOf(getViewModel().getBitDiameter()));
        }

        mEtLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = EditTextHelper.fixNumberInputSinglePoint(s).toString();
                mViewModel.setBitLengthInput(input);
            }
        });

        mEtDiameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = EditTextHelper.fixNumberInputSinglePoint(s).toString();
                mViewModel.setBitDiameterInput(input);
            }
        });
    }

    @OnClick(R2.id.btn_cnc_origin_assistant_custom_bit_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CNCOriginAssistantActivity) getActivity()).gotoCNCOriginAssistantSetOriginIntroFragment();
        }
    }
}
