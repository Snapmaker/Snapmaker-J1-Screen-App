package fabscreen.features.print.a400platform;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.PrintReadyViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.FabInputDialog;

public class ThicknessCheckFragment extends BaseFragment {

    @BindView(R2.id.btn_thickness)
    Button mBtnThickness;
    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_desc)
    TextView mTvDesc;
    @BindView(R2.id.tv_sub_title)
    TextView mTvSubTitle;

    private PrintReadyViewModel mViewModel;
    private FabInputDialog mFabInputDialog;

    public static Fragment newInstance() {
        return new ThicknessCheckFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mBtnThickness.setText(mViewModel.getIsRotaryAvailable() ? R.string.a400_laser_print_four_axis_material_diameter_title : R.string.print_laser_input_material_thickness_input_hint);
        mTvSubTitle.setText(mViewModel.getIsRotaryAvailable() ? R.string.a400_laser_print_four_axis_material_diameter_subtitle : R.string.print_laser_input_material_thickness_subtitle);
        mTvTitle.setText(mViewModel.getIsRotaryAvailable() ? R.string.a400_laser_print_four_axis_material_diameter_title : R.string.print_laser_input_material_thickness);
        mTvDesc.setText(mViewModel.getIsRotaryAvailable() ? R.string.a400_laser_print_four_axis_material_diameter_contnent : R.string.print_laser_input_material_thickness_message);

        mFabInputDialog = FabInputDialog.create(requireContext()).setTitle(R.string.print_laser_input_material_thickness_subtitle)
                .setButton(getString(R.string.all_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(mFabInputDialog.getEditTextContent())) {
                            mBtnThickness.setTextColor(ContextCompat.getColor(requireContext(), R.color.palette_white_pure));
                            mBtnThickness.setText(mFabInputDialog.getEditTextContent());
                            mViewModel.saveMaterialThickness(Float.parseFloat(mFabInputDialog.getEditTextContent()));
                        }
                    }
                });


    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_thickness_check;
    }

    @Override
    protected PrintReadyViewModel getViewModel() {
        return getViewModelProvider().get(PrintReadyViewModel.class);
    }

    @OnClick(R2.id.btn_thickness)
    void onThinkClick() {
        playSwitchSound();
        mFabInputDialog.show();
    }

}
