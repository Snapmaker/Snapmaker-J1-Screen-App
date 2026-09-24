package fabscreen.features.machinetools.setup.singledual.blocksetup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.WarmTipDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BlockSetupFragment extends BaseFragment {

    private BlockSetupViewModel mViewModel;

    public static Fragment newInstance() {
        return new BlockSetupFragment();
    }

    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_sub_title)
    TextView mTvSubTitle;
    @BindView(R2.id.progress)
    LinearProgressIndicator mProgress;
    @BindView(R2.id.iv_demonstrate)
    ImageView mIvBlockSetup;
    @BindView(R2.id.tv_demonstrate_desc)
    TextView mTvBlockDesc;
    @BindView(R2.id.btn_start_or_next)
    Button mBtnStartOrNext;
    @BindView(R2.id.lin_a400_guide_self_inspection)
    LinearLayout mLinSelfInspection;

    private int mCurrentStep = 0;
    private WarmTipDialog mMoveDialog;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mTvTitle.setText("安装挡块");
        mProgress.setMax(3);
        refreshView();
        initMovingDialog();
        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::showMovingDialogOrNot, LogHelper::log);
    }

    private void initMovingDialog() {
        mMoveDialog = WarmTipDialog.create(getActivity())
                .setDialogWidthSize(WarmTipDialog.WarmTipDialogSize.SIZE_M)
                .setPic(R.drawable.ic_block_setup)
                .setTitle(R.string.move_show)
                .setContent(R.string.move_show_content);
    }

    private void refreshView() {
        mProgress.setProgress(mCurrentStep);
        switch (mCurrentStep) {
            case 0:
                mProgress.setVisibility(View.INVISIBLE);
                mTvSubTitle.setVisibility(View.INVISIBLE);
                mBtnStartOrNext.setVisibility(View.VISIBLE);
                mTvBlockDesc.setText("接下来，屏幕将会引导你安装挡块，以确保左、右喷嘴可以顺利切换。");
                mBtnStartOrNext.setText("开始");
                break;

            case 1:
                mProgress.setVisibility(View.VISIBLE);
                mTvSubTitle.setVisibility(View.VISIBLE);
                mTvSubTitle.setText("左挡块安装（1/3）");
                mTvBlockDesc.setText("将双挤出模组背面的拨杆拨至最右侧。\n" +
                        "将左挡块插入 X 轴转接板底部的左挡块槽中。\n" +
                        "将左挡块向右拉出一定距离，使其贴合拨杆，然后使用 M3 螺丝将其固定。");
                mBtnStartOrNext.setText("下一步");
                break;
            case 2:
                mProgress.setVisibility(View.VISIBLE);
                mTvSubTitle.setVisibility(View.VISIBLE);
                mTvSubTitle.setText("右挡块安装（2/3）");
                mTvBlockDesc.setText("将双挤出模组背面的拨杆拨至最左侧。\n" +
                        "将右挡块插入 X 轴转接板底部的右挡块槽中。\n" +
                        "将右挡块向左拉出一定距离，使其贴合拨杆，然后使用 M3 螺丝将其固定。");
                mBtnStartOrNext.setText("下一步");
                break;
            case 3:
                mProgress.setVisibility(View.VISIBLE);
                mTvSubTitle.setVisibility(View.VISIBLE);
                mTvSubTitle.setText("自检（3/3）");
                mTvBlockDesc.setVisibility(View.GONE);
                mIvBlockSetup.setVisibility(View.GONE);
                mLinSelfInspection.setVisibility(View.VISIBLE);
                mBtnStartOrNext.setVisibility(View.INVISIBLE);
                animateViews();
                break;
        }
    }

    @OnClick(R2.id.btn_start_or_next)
    void onStartOrNextClicked() {
        playNormalClickSound();
        switch (mCurrentStep) {
            case 0:
                mCurrentStep++;
                refreshView();
                moveToolhead(BlockSetupViewModel.LEFT);
                break;

            case 1:
                mCurrentStep++;
                refreshView();
                moveToolhead(BlockSetupViewModel.RIGHT);
                break;
            case 2:
                mCurrentStep++;
                refreshView();
                selfCheck();
                break;
        }
    }

    @OnClick({R2.id.btn_close})
    void onCloseClicked() {
        playNormalClickSound();
        back();
    }

    private void moveToolhead(@BlockSetupViewModel.LeftOrRight int position) {
        mViewModel.goToInstallPosition(position)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    // do sth
                }, LogHelper::log);
    }

    private void selfCheck() {
        // Do async self check and finish.
        mViewModel.checkBlockInstall()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    finishActivityWithResultOk();
                }, e -> {
                    // setup fail
                    LogHelper.log(e);
                    showFailDialog(e.getMessage());
                });
    }

    private void showFailDialog(String message) {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, true, false, true, false)
                .setType(DecisionDialog.WARMING_TYPE)
                .setTitle("失败")
                .setContent("挡块未正确安装，请重试或退出安装\n" + message)
                .setFirstTv("重试", R.color.select_dialog_blue_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mCurrentStep = 0;
                        refreshView();
                    }
                }).show();
    }

    /**
     * Move ImageView and desc to proper place.
     */
    private void animateViews() {

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_guide_setup;
    }

    @Override
    protected BlockSetupViewModel getViewModel() {
        return getViewModelProvider().get(BlockSetupViewModel.class);
    }

    private void showMovingDialogOrNot(Boolean isMoving) {
        if (isMoving) {
            mMoveDialog.show();
        } else {
            mMoveDialog.dismiss();
        }
    }
}
