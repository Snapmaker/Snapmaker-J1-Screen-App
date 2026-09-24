package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.PrintReadyViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.A400XYZControlPanel;
import fabscreen.platform.core.ui.view.GuideProgressBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SetXYOriginFragment extends BaseFragment {
    @BindView(R2.id.top_bar_content)
    TextView mTvTopBarContent;
    @BindView(R2.id.fragment_a400_set_xy_origin_title)
    TextView mTvTitle;
    @BindView(R2.id.fragment_a400_set_xy_origin_content)
    TextView mTvContent;
    @BindView(R2.id.view_guide_progress_bar)
    protected GuideProgressBar mGuideProgressBar;

    @BindView(R2.id.cp_a400_set_xy_origin_move_move)
    A400XYZControlPanel a400XYZControlPanel;

    private PrintReadyViewModel mViewModel;

    public static Fragment newInstance() {
        return new SetXYOriginFragment();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
//        setTitle("1-1 准备激光作业");
//        mTvTopBarContent.setText("设置工作原点 （2/3）");
//        mTvTitle.setText("设置 XY 原点");
//        mTvContent.setText("手动控制执行头移动到目标工作原点位置，激光作业将会以此位置为原点开始加工。你可以点击跑边框来验证当前位置对应的工作区域。");
//        mGuideProgressBar.setmStepNum(3);
//        mGuideProgressBar.setmStepIndex(2);
//        mGuideProgressBar.invalidate();
//        mGuideProgressBar.setVisibility(View.VISIBLE);
//        a400XYZControlPanel.setStepWidths(0.1f, 1f, 10f, 100f)
//                .setOnDirectionClickListener((direction, stepWidth) -> {
//                    mViewModel.moveXYZByStep(direction, stepWidth);
//                });
//        mViewModel.getMovingObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(aBoolean -> {
//                    a400XYZControlPanel.setEnabled(!aBoolean);
//                });
    }

    @Override
    protected PrintReadyViewModel getViewModel() {
        return getViewModelProvider().get(PrintReadyViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_set_xy_origin;
    }


    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        playNormalClickSound();
        mViewModel.setXYOrigin()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    // jump next
                    if (success) {
                        ((PrintA400Activity) requireActivity()).getSetZ();
                    } else {
                    }
                }, LogHelper::log);
    }
}
