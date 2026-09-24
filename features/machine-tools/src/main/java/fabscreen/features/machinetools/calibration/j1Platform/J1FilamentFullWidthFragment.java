package fabscreen.features.machinetools.calibration.j1Platform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.j1.J1FilamentControlFragment;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.HelpDialog;

public class J1FilamentFullWidthFragment extends BaseFragment {

    private J1FilamentFullWidthViewModel mViewModel;

    /**
     * @param mode 0: both enabled; 1: left only; 2: right only
     */
    public static Fragment newInstance(int mode) {
        Fragment fragment = new J1FilamentFullWidthFragment();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R2.id.tv_top_bar_help)
    TextView mTvHelp;
    @BindView(R2.id.btn_top_bar_help)
    Button mBtnHelp;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_load_filament_full_width;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(J1FilamentFullWidthViewModel.class);

        initToolbar();
        initContent();
    }

    private void initContent() {
        int mode = requireArguments().getInt("mode");
        getChildFragmentManager().beginTransaction().replace(R.id.fcv_load_filament, J1FilamentControlFragment.newInstance(mode)).commit();
    }

    private void initToolbar() {
        mTvHelp.setVisibility(View.VISIBLE);
        mBtnHelp.setVisibility(View.VISIBLE);
    }

    @OnClick({R2.id.tv_top_bar_help, R2.id.btn_top_bar_help})
    void onHelpClicked() {
        HelpDialog.create(requireContext(), mViewModel.getHelpList()).show();
    }

    @Override
    protected void back() {
        getParentFragmentManager().popBackStack();
    }
}
