package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.helper.EditTextHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CustomBitDialog {

    private static CustomBitDialog sInstance;
    private AlertDialog mDialog;
    private Button mBtnBack;
    private TextView mTvTitle;
    private TextView mTvContent;
    private LinearProgressIndicator progress;
    private EditText mEtLength;
    private EditText mEtDiameter;
    private TextView mTvDiameterTip;
    private TextView mTvLengthTip;
    private Button mBtnNext;

    private static A400CNCOriginAssistantViewModel mViewModel;
    private static CompositeDisposable disposables = new CompositeDisposable();
    public OnItemClickListener mListener;

    public CustomBitDialog setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static CustomBitDialog create(Context context, A400CNCOriginAssistantViewModel viewModel) {
        mViewModel = viewModel;

        if (sInstance != null) {
            sInstance.mDialog.cancel();
            sInstance.mDialog.dismiss();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, fabscreen.platform.core.R.style.AppTheme_Dialog_Fullscreen);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();

        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_custom_bit, null);
        dialog.setView(view);

        sInstance = new CustomBitDialog();
        sInstance.mDialog = dialog;

        sInstance.mBtnBack = view.findViewById(R.id.top_bar_back);
        sInstance.mTvTitle = view.findViewById(R.id.top_bar_title);
        sInstance.mTvContent = view.findViewById(R.id.top_bar_content);
        sInstance.progress = view.findViewById(R.id.view_guide_progress_bar);
        sInstance.mEtLength = view.findViewById(R.id.et_cnc_origin_assistant_set_material_length);
        sInstance.mEtDiameter = view.findViewById(R.id.et_cnc_origin_assistant_set_material_diameter);
        sInstance.mTvDiameterTip = view.findViewById(R.id.tv_cnc_origin_assistant_set_material_diameter_tip);
        sInstance.mTvLengthTip = view.findViewById(R.id.tv_cnc_origin_assistant_set_material_length_tip);
        sInstance.mBtnNext = view.findViewById(R.id.btn_cnc_origin_assistant_set_material_next);

        sInstance.mBtnBack.setBackgroundResource(R.drawable.ic_back_136x136);
        sInstance.mTvTitle.setText(R.string.cnc_origin_assistant_custom_bit);
        sInstance.mTvContent.setVisibility(View.GONE);
        sInstance.progress.setVisibility(View.GONE);

        sInstance.mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sInstance.mDialog.dismiss();
            }
        });
        sInstance.mBtnNext.setEnabled(false);

        sInstance.mEtLength.addTextChangedListener(new TextWatcher() {
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

        sInstance.mEtDiameter.addTextChangedListener(new TextWatcher() {
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

        // Skip the first value.
        Disposable sub = mViewModel.getBitLengthTipObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tip -> {
                    switch (tip) {
                        case TIP_OK:
                            sInstance.mTvLengthTip.setVisibility(TextView.GONE);
                            sInstance.mTvLengthTip.setText("");
                            sInstance.mEtLength.setBackgroundResource(R.drawable.bg_snap_black_round);
                            break;
                        case TIP_NOT_POSITIVE_NUMBER:
                            sInstance.mTvLengthTip.setVisibility(TextView.VISIBLE);
                            sInstance.mTvLengthTip.setText(R.string.cnc_origin_assistant_input_tip_not_positive);
                            sInstance.mEtLength.setBackgroundResource(R.drawable.bg_set_material_error);
                            break;
                        case TIP_EMPTY:
                            sInstance.mTvLengthTip.setVisibility(TextView.VISIBLE);
                            sInstance.mTvLengthTip.setText(R.string.cnc_origin_assistant_input_tip_empty);
                            sInstance.mEtLength.setBackgroundResource(R.drawable.bg_set_material_error);
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
                            sInstance.mTvDiameterTip.setVisibility(TextView.GONE);
                            sInstance.mTvDiameterTip.setText("");
                            sInstance.mEtDiameter.setBackgroundResource(R.drawable.bg_snap_black_round);
                            break;
                        case TIP_NOT_POSITIVE_NUMBER:
                            sInstance.mTvDiameterTip.setVisibility(TextView.VISIBLE);
                            sInstance.mTvDiameterTip.setText(fabscreen.features.machinetools.R.string.cnc_origin_assistant_input_tip_not_positive);
                            sInstance.mEtDiameter.setBackgroundResource(R.drawable.bg_set_material_error);
                            break;
                        case TIP_EMPTY:
                            sInstance.mTvDiameterTip.setVisibility(TextView.VISIBLE);
                            sInstance.mTvDiameterTip.setText(fabscreen.features.machinetools.R.string.cnc_origin_assistant_input_tip_empty);
                            sInstance.mEtDiameter.setBackgroundResource(R.drawable.bg_set_material_error);
                            break;
                        default:
                            break;
                    }
                });
        disposables.add(sub);

        sub = mViewModel.getCustomBitInputReady()
                .debounce(200, Constants.TIME_UNIT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isReady -> sInstance.mBtnNext.setEnabled(isReady));
        disposables.add(sub);

        return sInstance;
    }

    public CustomBitDialog onBackClick(AlertDialog.OnClickListener listener) {
        mBtnBack.setOnClickListener(v -> listener.onClick(mDialog, 1));
        return this;
    }

    public CustomBitDialog onBackNext(AlertDialog.OnClickListener listener) {
        mBtnNext.setOnClickListener(v -> listener.onClick(mDialog, 0));
        return this;
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }
}
