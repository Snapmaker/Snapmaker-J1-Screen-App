package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.PrintReadyViewModel;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.WarmTipDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LaserSetZFragment extends BaseFragment {

    private PrintReadyViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400LaserSetZFragment();
    }

    @BindView(R2.id.rl_calibration_check_mode)
    RelativeLayout mRlCheckMode;
    @BindView(R2.id.tv_calibration_check_mode)
    TextView mTvModeName;
    @BindView(R2.id.tv_touch_desc)
    TextView mTvTouchDesc;
    @BindView(R2.id.btn_start_work)
    Button mBtnStartWork;
    @BindView(R2.id.top_bar_back)
    Button mBack;
    @BindView(R2.id.top_bar_title)
    TextView mTvTopBarTitle;
    @BindView(R2.id.top_bar_content)
    TextView mTvTopBarContent;
    @BindView(R2.id.top_bar_ico)
    ImageView mIvTopBarIco;
    @BindView(R2.id.view_guide_progress_bar)
    LinearProgressIndicator mProgress;

    private WarmTipDialog fabLoading;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabLoading = WarmTipDialog.create(requireContext())
                .setDialogWidthSize(WarmTipDialog.WarmTipDialogSize.SIZE_M)
                .setPic(R.drawable.ic_move_home)
                .setTitle(requireContext().getString(R.string.move_show))
                .setContent(R.string.move_show_content);
        mViewModel = getViewModel();
        mViewModel.checkHome()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.updateMode();
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.setOriginIndicatorState(false);
    }

    private void initView() {
        mTvTopBarTitle.setText(R.string.print_laser_job_preparation);
        mTvTopBarContent.setText(getString(R.string.print_adjust_laser_height, "(2/3)"));
        mIvTopBarIco.setVisibility(View.GONE);
        mProgress.setMax(3);
        mProgress.setProgress(2);
        int prepareMode = mViewModel.getPrepareMode();
        Fragment fragment = A400LaserAutoThickMeasureFragment.newInstance();
        if (mViewModel.getIsRotaryAvailable()) {
            switch (prepareMode) {
                case 0:
                    mViewModel.setOriginIndicatorState(false);
                    mTvModeName.setText(R.string.a400_laser_print_four_axis_input_diameter_subtitle);
                    fragment = ThicknessCheckFragment.newInstance();
                    break;
                case 1:
                    mViewModel.setOriginIndicatorState(false);
                    mTvModeName.setText(R.string.a400_laser_print_four_axis_touchmaterial_subtitle);
                    Bundle WorkBundle = new Bundle();
                    WorkBundle.putInt("pic", 0);
                    WorkBundle.putString("title", getString(R.string.a400_print_laser_touch_material_surface_title));
                    WorkBundle.putString("desc", getString(R.string.a400_print_laser_touch_material_surface_content));
                    fragment = WorkPrepareWithJogFragment.newInstance(WorkBundle);
                    break;
                case 2:
                    // open laser
                    mViewModel.setOriginIndicatorState(true);
                    mTvModeName.setText(R.string.a400_laser_print_four_axis_manual_focus_title);
                    Bundle bundle = new Bundle();
                    bundle.putInt("pic", 0);
                    bundle.putString("title", getString(R.string.a400_print_laser_Manually_Adjust_Laser_Height_title));
                    bundle.putString("desc", getString(R.string.a400_print_laser_Manually_Adjust_Laser_Height_content));
                    fragment = WorkPrepareWithJogFragment.newInstance(bundle);
                    break;
                default:
                    break;
            }
        } else {
            switch (prepareMode) {
                case 0:
                    mViewModel.setOriginIndicatorState(false);
                    mTvModeName.setText(R.string.automatic_thickness_measurement_title);
                    fragment = A400LaserAutoThickMeasureFragment.newInstance();
                    break;
                case 1:
                    mViewModel.setOriginIndicatorState(false);
                    mTvModeName.setText(R.string.enter_material_thickness_title);
                    fragment = ThicknessCheckFragment.newInstance();
//                    mTvTouchDesc.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    mViewModel.setOriginIndicatorState(false);
                    mTvModeName.setText(R.string.bonding_material_surface_title);
                    Bundle WorkBundle = new Bundle();
                    WorkBundle.putInt("pic", 0);
                    WorkBundle.putString("title", getString(R.string.print_laser_calibration_plate_assisted_subtitle));
                    WorkBundle.putString("desc", getString(R.string.print_laser_calibration_plate_assisted_message));
                    fragment = WorkPrepareWithJogFragment.newInstance(WorkBundle);
                    break;
                case 3:
                    // open laser
                    mViewModel.setOriginIndicatorState(true);
                    mTvModeName.setText(R.string.manual_focus_title);
                    Bundle bundle = new Bundle();
                    bundle.putInt("pic", 0);
                    bundle.putString("title", getString(R.string.print_laser_manual_focus_subtitle));
                    bundle.putString("desc", getString(R.string.print_laser_manual_focus_content));
                    fragment = WorkPrepareWithJogFragment.newInstance(bundle);
                    break;
                default:
                    break;
            }
        }
        getChildFragmentManager().beginTransaction().replace(R.id.fcv_prepare_mode, fragment).commit();
        mViewModel.getMovingObservable().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(aBoolean -> {
            if (aBoolean) {
                fabLoading.show();
            } else {
                fabLoading.dismiss();
            }
            mRlCheckMode.setEnabled(!aBoolean);
            mBtnStartWork.setEnabled(!aBoolean);
            mBack.setEnabled(!aBoolean);
        });

    }

    @Override
    protected PrintReadyViewModel getViewModel() {
        return getViewModelProvider().get(PrintReadyViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_laser_last_prepare_step;
    }


    @OnClick(R2.id.rl_calibration_check_mode)
    void onClickedMode() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.PRINT_LASER_SET_Z_SELECT)
                .start(getContext());
    }

    @OnClick(R2.id.btn_start_work)
    void onStartWorkClicked() {
        playNormalClickSound();
        mViewModel.moveToZ()
                .observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        ((PrintA400Activity) requireActivity()).goToSetOrigin();
                    }
                }, LogHelper::log);
    }
}
