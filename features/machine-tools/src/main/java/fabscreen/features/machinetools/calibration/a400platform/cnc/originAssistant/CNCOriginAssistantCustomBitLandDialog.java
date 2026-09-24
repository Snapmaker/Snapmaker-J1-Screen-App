package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.helper.EditTextHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CNCOriginAssistantCustomBitLandDialog {
    private static A400CNCOriginAssistantViewModel mViewModel;
    private static CompositeDisposable disposables = new CompositeDisposable();
    @BindView(R2.id.et_cnc_origin_assistant_custom_bit_length)
    EditText mEtLength;
    @BindView(R2.id.et_cnc_origin_assistant_custom_bit_diameter)
    EditText mEtDiameter;
    @BindView(R2.id.tv_cnc_origin_assistant_custom_bit_diameter_tip)
    TextView mTvDiameterTip;
    @BindView(R2.id.tv_cnc_origin_assistant_custom_bit_length_tip)
    TextView mTvLengthTip;
    @BindView(R2.id.btn_cnc_origin_assistant_custom_bit_back)
    Button mBtnBack;
    @BindView(R2.id.btn_cnc_origin_assistant_custom_bit_next)
    Button mBtnNext;
    private AlertDialog mDialog;

    CNCOriginAssistantCustomBitLandDialog(AlertDialog dialog, View view) {
        mDialog = dialog;
        ButterKnife.bind(this, view);
        initView();
    }

    public static CNCOriginAssistantCustomBitLandDialog create(Context context, A400CNCOriginAssistantViewModel viewModel) {
        mViewModel = viewModel;
        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // create view
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.fragment_cnc_origin_assistant_custom_bit_land, null);
//        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);

        // instance
        return new CNCOriginAssistantCustomBitLandDialog(dialog, view);
    }

    private void initView() {
        // We need to disabled next button first before user start inputting.
        mBtnNext.setEnabled(false);

        initEditText();

        // Skip the first value.
        Disposable sub = mViewModel.getBitLengthTipObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
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
        disposables.add(sub);
        // Skip the first value.
        sub = mViewModel.getBitDiameterTipObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tip -> {
                    switch (tip) {
                        case TIP_OK:
                            mTvDiameterTip.setVisibility(TextView.GONE);
                            mTvDiameterTip.setText("");
                            break;
                        case TIP_NOT_POSITIVE_NUMBER:
                            mTvDiameterTip.setVisibility(TextView.VISIBLE);
                            mTvDiameterTip.setText(fabscreen.features.machinetools.R.string.cnc_origin_assistant_input_tip_not_positive);
                            break;
                        case TIP_EMPTY:
                            mTvDiameterTip.setVisibility(TextView.VISIBLE);
                            mTvDiameterTip.setText(fabscreen.features.machinetools.R.string.cnc_origin_assistant_input_tip_empty);
                            break;
                        default:
                            break;
                    }
                });
        disposables.add(sub);

        sub = mViewModel.getCustomBitInputReady()
                .debounce(200, Constants.TIME_UNIT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isReady -> mBtnNext.setEnabled(isReady));
        disposables.add(sub);
    }

    private void initEditText() {
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


    public CNCOriginAssistantCustomBitLandDialog onBackClick(AlertDialog.OnClickListener listener) {
        mBtnBack.setOnClickListener(v -> listener.onClick(mDialog, 1));
        return this;
    }

    public CNCOriginAssistantCustomBitLandDialog onBackNext(AlertDialog.OnClickListener listener) {
        mBtnNext.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public void show() {
        mDialog.show();

        // Dynamically change dialog width (trick)
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            mDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public void dismiss() {
        mDialog.dismiss();
        disposables.clear();
    }
}
