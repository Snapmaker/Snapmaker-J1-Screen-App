package fabscreen.features.settings.a400.moduleassistant.replacehotend;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.BaseFragment;

public class ReplaceHotendCompleteFragment extends BaseFragment {

    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_desc)
    TextView mTvDesc;
    @BindView(R2.id.sv_need_calibration)
    ScrollView mSvNeedCalibration;
    @BindView(R2.id.ll_need_calibration)
    LinearLayout mLlNeedCalibration;
    @BindView(R2.id.btn_calibrate)
    Button mBtnCalibrate;
    @BindView(R2.id.btn_skip)
    Button mBtnSkip;

    private ReplaceHotendViewModel mViewModel;

    public static Fragment newInstance() {
        return new ReplaceHotendCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceHotendViewModel.class);
        initView();
    }

    private void initView() {
        mSvNeedCalibration.setVisibility(View.VISIBLE);
        mBtnCalibrate.setText("Z Offset Calibration");
        mBtnSkip.setText("Skip Calibration");
        mTvTitle.setText("Hot End Replaced");
        mTvDesc.setText(R.string.a400_replace_hotend_complete_desc);

        for (String name : mViewModel.getReplaceHotendName()) {
            TextView textView = new TextView(requireContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            textView.setTextColor(Color.WHITE);
            textView.setText(name);
            textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_a400_module_indicator, null), null, null, null);
            textView.setCompoundDrawablePadding((int) DimensUtils.dp2px(12));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = 15;
            mLlNeedCalibration.addView(textView, lp);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_replace_module_complete;
    }

    @OnClick({R2.id.btn_calibrate, R2.id.btn_skip})
    @Override
    public void onClick(View view) {
        super.onClick(view);
        playSwitchSound();
        int id = view.getId();
        if (id == R.id.btn_calibrate) {
            mRouter.routeToCalibrationPage().start(requireContext());
            requireActivity().finish();
        } else if (id == R.id.btn_skip) {
            requireActivity().finish();
        }
    }
}
