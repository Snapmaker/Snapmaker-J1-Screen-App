package fabscreen.features.settings.a400.moduleassistant;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.common.A400ModuleAssistantViewModel;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.BaseFragment;

public class A400ModuleAssistantFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new A400ModuleAssistantFragment();
    }

    @BindView(R2.id.ll_module_list)
    LinearLayout mLlModuleList;

    private A400ModuleAssistantViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(A400ModuleAssistantViewModel.class);
        initView();
    }

    private void initView() {
        setTitle("Module Assistant");
        if (mLlModuleList.getChildCount() > 1) {
            mLlModuleList.removeViews(1, mLlModuleList.getChildCount() - 1);
        }
        List<String> moduleNameList = mViewModel.getModuleNameList();
        for (String name : moduleNameList) {
            TextView tvName = new TextView(requireContext());
            tvName.setText(name);
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            tvName.setTextColor(0xFFFFFFFF);
            tvName.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_a400_module_indicator, null), null, null, null);
            tvName.setCompoundDrawablePadding((int) DimensUtils.dp2px(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = 15;
            mLlModuleList.addView(tvName, lp);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_settings_module_assist;
    }

    @OnClick(R2.id.btn_replace_module)
    void onReplaceModuleClicked() {
        playNormalClickSound();
        mRouter.routeToReplaceModules().startForResult(this, 2);
    }

    @OnClick(R2.id.btn_replace_hotend)
    void onReplaceNozzleClicked() {
        playNormalClickSound();
        mRouter.routeToReplaceHotend().start(requireContext());
    }

    @OnClick(R2.id.sv_module_list)
    void guide() {
        playSwitchSound();
        mRouter.routeToGuideMilestone().start(requireContext());
    }
}
