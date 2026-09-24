package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ReplaceModuleConfirmationFragment extends ReplaceModuleProgressFragment {

    @BindView(R2.id.ll_removed)
    LinearLayout mLlRemoved;
    @BindView(R2.id.ll_added)
    LinearLayout mLlAdded;


    private ReplaceModuleViewModel mViewModel;

    public static Fragment newInstance() {
        return new ReplaceModuleConfirmationFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceModuleViewModel.class);
        initView();
    }

    private void initView() {
        setMainTitle("Replace Module");
        setSubTitle("Confirm Replaced Module(3/3)");
        setProgress(3, 3);
        setIfShowClose(true);

        List<String> removedModules = mViewModel.getRemovedModuleList();
        List<String> addedModules = mViewModel.getAddedModuleList();

        Logger.d("removed: %1$s, added: %2$s", removedModules, addedModules);

        // refresh view
        for (String moduleName : removedModules) {
            TextView textView = new TextView(requireContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            textView.setTextColor(Color.WHITE);
            textView.setText(moduleName);
            textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_a400_module_indicator, null), null, null, null);
            textView.setCompoundDrawablePadding((int) DimensUtils.dp2px(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = 15;
            mLlRemoved.addView(textView, lp);
        }

        for (String moduleName : addedModules) {
            TextView textView = new TextView(requireContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            textView.setTextColor(Color.WHITE);
            textView.setText(moduleName);
            textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_a400_module_indicator, null), null, null, null);
            textView.setCompoundDrawablePadding((int) DimensUtils.dp2px(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = 15;
            mLlAdded.addView(textView, lp);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_replace_module_confirmation;
    }

    @OnClick({R2.id.btn_fail, R2.id.btn_done})
    @Override
    public void onClick(View view) {
        super.onClick(view);
        playNormalClickSound();
        int id = view.getId();
        if (id == R.id.btn_fail) {
            // do on fail
            DecisionDialog.create(requireContext())
                    .setDialogStatus(1, true, false, true, true)
                    .setType(DecisionDialog.ERROR_TYPE)
                    .setPic(R.drawable.pic_a400_dialog_failed_72x72)
                    .setTitle("Recognition Failed")
                    .setContent("Check if all cables are connected correctly. If yes, tap Retry, and the mainboard will restart. If the problem persists, please contact our Support at support@snapmaker.com.")
                    .setFirstTv(R.string.all_retry, R.color.select_dialog_white_txt, (dialog, which) -> {
                        dialog.dismiss();
                        retry();
                    })
                    .show();
        } else if (id == R.id.btn_done) {
            goToReplaceComplete();
        }
    }

    private void retry() {
        if (requireActivity() instanceof A400ReplaceModuleActivity) {
            mViewModel.restartMachine()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(this::handleRestartResult);
        }
    }

    private void handleRestartResult(Integer result) {
        switch (result) {
            case 0:
                ((A400ReplaceModuleActivity) requireActivity()).goToReplaceModuleRestart();
                break;
            case 1:
            default:
                new SuperToastHelper.Builder()
                        .setMessage("Fail to restart!")
                        .build()
                        .showToast(requireContext());
        }
    }

    private void goToReplaceComplete() {
        if (requireActivity() instanceof A400ReplaceModuleActivity) {
            ((A400ReplaceModuleActivity) requireActivity()).goToComplete();
        }
    }
}
