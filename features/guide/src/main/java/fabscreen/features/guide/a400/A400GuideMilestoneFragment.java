package fabscreen.features.guide.a400;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseFragment;

public class A400GuideMilestoneFragment extends BaseFragment {
    @BindView(R2.id.tv_title_main)
    TextView mTvTitle;
    @BindView(R2.id.tv_main_desc)
    TextView mTvMainDesc;
    @BindView(R2.id.rv_procedure_list)
    RecyclerView mRvProcedureList;
    @BindView(R2.id.btn_start_or_continue)
    Button mBtnStartOrContinue;

    private int mNextProcedure = 1;
    private List<GuideProcedure> mProcedureList = new ArrayList<>();
    private ProcedureListAdapter mAdapter;
    private A400GuideMilestoneViewModel mViewModel;


    public static Fragment newInstance() {
        return new A400GuideMilestoneFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    @Override
    protected A400GuideMilestoneViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(A400GuideMilestoneViewModel.class);
    }

    /**
     * 3dp
     * - single-dual
     * - single-single
     * laser
     * - 10w
     * - 1.6w
     * - rotary
     * cnc
     */
//    private int getMilestoneType() {
//        return getArguments() != null ? getArguments().getInt("headType") : Module.ModuleType.HEAD_UNPLUGGED;
//    }
    private void initView() {
        mTvTitle.setText(R.string.guide_a400_setup_completed);
        mAdapter = new ProcedureListAdapter(mProcedureList);
        mRvProcedureList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRvProcedureList.setAdapter(mAdapter);

        mProcedureList.clear();
        switch (mViewModel.getHeadType()) {
            case HEAD_3DP_DOUBLE_EXTRUDER:
                mTvMainDesc.setText(R.string.guide_a400_double_extruder_msg);
//                mProcedureList.add(new GuideProcedure("安装挡块", false));
                mProcedureList.add(new GuideProcedure(getString(R.string.a400_guide_step_1), false));
                mProcedureList.add(new GuideProcedure(getString(R.string.a400_guide_step_2), false));
                mProcedureList.add(new GuideProcedure(getString(R.string.a400_guide_step_3), false));
                break;
            case HEAD_3DP:
                mProcedureList.add(new GuideProcedure("热床调平", false));
                mProcedureList.add(new GuideProcedure("装载物料", false));
                break;
            case HEAD_LASER_10W:
                mTvMainDesc.setText(R.string.guide_a400_10w_laser_msg);
                if (mViewModel.isRotaryAvailable()) {
                    mProcedureList.add(new GuideProcedure("四轴轴心高度校准", false));
                } else {
                    mProcedureList.add(new GuideProcedure(getString(R.string.guide_a400_thickness_m_calibration), false));
                    mProcedureList.add(new GuideProcedure(getString(R.string.guide_a400_camera_calibration), false));
                }
                break;
            case HEAD_LASER:
                if (mViewModel.isRotaryAvailable()) {
                    mProcedureList.add(new GuideProcedure("四轴轴心高度校准", false));
                } else {
                    mProcedureList.add(new GuideProcedure("辅助激光焦距校准", false));
                    mProcedureList.add(new GuideProcedure("摄像头捕捉校准", false));
                }
                break;
            case HEAD_CNC:
            case HEAD_CNC_200W:
                mTvMainDesc.setText(R.string.guide_a400_cnc_msg);
                mProcedureList.add(new GuideProcedure(getString(R.string.guide_a400_safety_goggles), false));
                mProcedureList.add(new GuideProcedure(getString(R.string.guide_a400_fix_material), false));
                mProcedureList.add(new GuideProcedure(getString(R.string.guide_a400_attach_bit), false));
                mProcedureList.add(new GuideProcedure(getString(R.string.guide_a400_tools_screen), false));
                break;
            default:
                mRouter.backHome().start(requireContext());
                break;
        }
        mAdapter.notifyItemRangeInserted(0, mProcedureList.size());
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_guide_milstone;
    }

    @OnClick(R2.id.btn_start_or_continue)
    void onStartClick() {
        playNormalClickSound();
        A400GuideMilestoneActivity activity = (A400GuideMilestoneActivity) requireActivity();
        switch (mViewModel.getHeadType()) {
            case HEAD_3DP_DOUBLE_EXTRUDER:
                singleDualSetup(activity);
                break;
            case HEAD_3DP:
                singleSingleSetup(activity);
                break;
            case HEAD_LASER_10W:
                if (mViewModel.isRotaryAvailable()) {
                    fourAxisLaserSetup(activity);
                } else {
                    tenWLaserSetup(activity);
                }
                break;
            case HEAD_LASER:
                if (mViewModel.isRotaryAvailable()) {
                    fourAxisLaserSetup(activity);
                } else {
                    originalLaserSetup(activity);
                }
                break;
            case HEAD_CNC:
            case HEAD_CNC_200W:
                cncSetup(activity);
                break;
        }
    }

    public void onMilestoneAchieved(int requestCode) {
        int procedureIndex = requestCode - 1;
        if (mViewModel.getHeadType() == HEAD_CNC || mViewModel.getHeadType() == HEAD_CNC_200W) {
            // If cnc, activated all.
            for (GuideProcedure procedure : mProcedureList) {
                procedure.activated = true;
            }
            procedureIndex = mProcedureList.size() - 1;
            mAdapter.notifyItemRangeChanged(0, mProcedureList.size());
        } else {
            mProcedureList.get(procedureIndex).activated = true;
            mAdapter.notifyItemChanged(procedureIndex);
        }

        switch (mViewModel.getHeadType()) {

            case HEAD_CNC:
            case HEAD_CNC_200W:
                if (procedureIndex == mProcedureList.size() - 1) {
                    mTvMainDesc.setVisibility(View.GONE);
                    mTvTitle.setText(R.string.guide_a400_setup_guidance_completed);
                    mBtnStartOrContinue.setText(R.string.all_safety_goggles);
                }

                break;
            case HEAD_LASER_10W:
                if (procedureIndex == 0) {
                    mTvMainDesc.setVisibility(View.VISIBLE);
                    mTvTitle.setText(R.string.guide_a400_laser_10w_step_1_suc_title);
                    mTvMainDesc.setText(R.string.guide_a400_laser_10w_step_1_suc_msg);
                    mBtnStartOrContinue.setText(R.string.all_continue);
                } else if (procedureIndex == mProcedureList.size() - 1) {
                    mTvMainDesc.setVisibility(View.VISIBLE);
                    mTvTitle.setText(R.string.guide_a400_laser_10w_success_title);
                    mTvMainDesc.setText(R.string.guide_a400_laser_10w_success_msg);
                    mBtnStartOrContinue.setText(R.string.guide_a400_start_creating);
                }

                break;
            case HEAD_3DP_DOUBLE_EXTRUDER:
                if (procedureIndex == 0) {
                    //step one success
                    mTvMainDesc.setVisibility(View.VISIBLE);
                    mTvTitle.setText(R.string.guide_a400_double_extruder_step_1_suc_title);
                    mTvMainDesc.setText(R.string.guide_a400_double_extruder_step_1_suc_msg);
                    mBtnStartOrContinue.setText(R.string.all_continue);
                } else if (procedureIndex == 1) {
                    //step two success
                    mTvMainDesc.setVisibility(View.VISIBLE);
                    mTvTitle.setText(R.string.guide_a400_double_extruder_step_2_suc_title);
                    mTvMainDesc.setText(R.string.guide_a400_double_extruder_step_2_suc_msg);
                    mBtnStartOrContinue.setText(R.string.all_continue);
                } else if (procedureIndex == mProcedureList.size() - 1) {
                    mTvMainDesc.setVisibility(View.VISIBLE);
                    mTvTitle.setText(R.string.guide_a400_laser_10w_success_title);
                    mTvMainDesc.setText(R.string.guide_a400_double_extruder_suc_msg);
                    mBtnStartOrContinue.setText(R.string.guide_a400_start_creating);
                }
                break;
        }

        mNextProcedure++;
    }

    private void cncSetup(A400GuideMilestoneActivity activity) {
        switch (mNextProcedure) {
            case 1:
                activity.goToCNCSetupForResult(mNextProcedure);
                break;

            default:
                activity.goHomePage();
                break;
        }
    }

    private void fourAxisLaserSetup(A400GuideMilestoneActivity activity) {
        switch (mNextProcedure) {
            case 1:
                Bundle pageData = new Bundle();
                pageData.putString("title", "1-1 四轴轴心高度校准");
                pageData.putInt("image", 0);
                pageData.putString("desc", "再接下来的步骤中，屏幕将会引导你使用自定义材料来校准四轴轴心");
                pageData.putString("router_destination", RoutePath.TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS);
                activity.goToSetupIntroForResult(mNextProcedure, pageData);
                break;

            default:
                activity.goHomePage();
                break;
        }

    }


    private void originalLaserSetup(A400GuideMilestoneActivity activity) {
        Bundle pageData = new Bundle();
        switch (mNextProcedure) {
            case 1:
                pageData.putString("title", "辅助激光校准");
                pageData.putInt("image", 0);
                pageData.putString("desc", "再接下来的步骤中，屏幕将会引导你进行辅助激光校准");
                pageData.putString("router_destination", RoutePath.TOOLS_CALIBRATION_A400_LASER_FOCUS_CALIBRATION);
                activity.goToSetupIntroForResult(mNextProcedure, pageData);
                break;
            case 2:
                pageData.putString("title", "1-1 摄像头捕捉校准");
                pageData.putInt("image", 0);
                pageData.putString("desc", "再接下来的步骤中，屏幕将会引导你进行摄像头捕捉校准");
                pageData.putString("router_destination", RoutePath.TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION);
                activity.goToSetupIntroForResult(mNextProcedure, pageData);
                break;
            default:
                activity.goHomePage();
        }
    }

    private void singleDualSetup(A400GuideMilestoneActivity activity) {
        switch (mNextProcedure) {
//            case 1:
//                activity.goToBlockSetupForResult(mNextProcedure);
//                break;
            case 1:
                activity.goToFilamentSetupForResult(mNextProcedure);
                break;
            case 2:

                activity.goToZCalibrationSetupForResult(mNextProcedure);
                break;
            case 3:
                activity.goToXYCalibrationSetupForResult(mNextProcedure);
                break;
            default:
                activity.goHomePage();
                break;
        }
    }

    private void singleSingleSetup(A400GuideMilestoneActivity activity) {
        switch (mNextProcedure) {
            case 1:
                activity.goToHeatedBedLevelingSetupForResult(mNextProcedure);
                break;

            case 2:
                activity.goToSingleSingleFilamentSetupForResult(mNextProcedure);
                break;
            default:
                activity.goHomePage();
        }
    }

    private void tenWLaserSetup(A400GuideMilestoneActivity activity) {
        Bundle pageData = new Bundle();
        switch (mNextProcedure) {
            case 1:
                pageData.putString("title", getString(R.string.guide_a400_thickness_m_calibration));
                pageData.putInt("image", 0);
                pageData.putString("desc", getString(R.string.guide_a400_thickness_measurement_calibration_msg));
                pageData.putString("router_destination", RoutePath.TOOLS_CALIBRATION_A400_LASER_THICKNESS_MEASURE_CALIBRATION);
                activity.goToSetupIntroForResult(mNextProcedure, pageData);
                break;
            case 2:
                pageData.putString("title", getString(R.string.guide_a400_camera_calibration));
                pageData.putInt("image", 0);
                pageData.putString("desc", getString(R.string.guide_a400_camera_calibration_msg));
                pageData.putString("router_destination", RoutePath.TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION);
                activity.goToSetupIntroForResult(mNextProcedure, pageData);
                break;
            default:
                activity.goHomePage();
        }
    }

    static class GuideProcedure {
        public String name;
        public boolean activated;

        public GuideProcedure(String title, boolean activated) {
            this.name = title;
            this.activated = activated;
        }
    }


    static class ProcedureListAdapter extends RecyclerView.Adapter<ProcedureListAdapter.ViewHolder> {
        private final List<GuideProcedure> mProcedureList;

        public ProcedureListAdapter(List<GuideProcedure> procedureList) {
            mProcedureList = procedureList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guide_procedure, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GuideProcedure procedure = mProcedureList.get(position);
            holder.mTvProcedureName.setText(procedure.name);
            holder.itemView.setActivated(procedure.activated);
        }

        @Override
        public int getItemCount() {
            return mProcedureList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R2.id.tv_procedure_name)
            TextView mTvProcedureName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
