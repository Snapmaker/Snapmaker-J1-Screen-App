package fabscreen.features.print.print;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class PrintChangeFilamentLandFragment extends BaseFragment {
    private static final int PRINT_FILAMENT_CHANGED_STATE_IDLE = 1;
    private static final int PRINT_FILAMENT_CHANGED_STATE_HEATING = 2;
    private static final int PRINT_FILAMENT_CHANGED_STATE_LOADING = 3;
    private static final int PRINT_FILAMENT_CHANGED_STATE_COMPLETE = 4;

    @BindView(R2.id.view_print_filament_changed_buttons)
    View mViewButtons;

    @BindView(R2.id.btn_print_filament_changed_hide)
    Button mBtnHide;
    @BindView(R2.id.btn_print_filament_changed_retry)
    Button mBtnRetry;
    @BindView(R2.id.btn_print_filament_changed_load)
    Button mBtnLoad;
    @BindView(R2.id.btn_print_filament_changed_complete)
    Button mBtnComplete;

    @BindView(R2.id.tv_print_filament_changed_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_print_filament_changed_desc)
    TextView mTvDesc;
    @BindView(R2.id.tv_print_filament_changed_loading)
    TextView mTvLoading;

    private BehaviorSubject<Integer> mFilamentChangedStateSubject = BehaviorSubject.createDefault(PRINT_FILAMENT_CHANGED_STATE_IDLE);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.print_change_filament);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print_filament_changed;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mFilamentChangedStateSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(state -> {
                    switch (state) {
                        case PRINT_FILAMENT_CHANGED_STATE_IDLE:
                            mViewButtons.setVisibility(View.VISIBLE);
                            mBtnHide.setVisibility(Button.VISIBLE);
                            mBtnLoad.setVisibility(Button.VISIBLE);
                            mBtnRetry.setVisibility(Button.GONE);
                            mBtnComplete.setVisibility(Button.GONE);

                            mTvTitle.setVisibility(TextView.VISIBLE);
                            mTvDesc.setVisibility(TextView.VISIBLE);
                            mTvLoading.setVisibility(TextView.GONE);

                            mTvTitle.setText("{挤出机}物料已用尽");
                            mTvDesc.setText("检测到{挤出机}未装载物料。\n" +
                                    "请插入新物料后，后点击装载按钮");
                            break;
                        case PRINT_FILAMENT_CHANGED_STATE_HEATING:
                            mViewButtons.setVisibility(View.GONE);
                            mBtnHide.setVisibility(Button.GONE);
                            mBtnLoad.setVisibility(Button.GONE);
                            mBtnRetry.setVisibility(Button.GONE);
                            mBtnComplete.setVisibility(Button.GONE);

                            mTvTitle.setVisibility(TextView.GONE);
                            mTvDesc.setVisibility(TextView.GONE);
                            mTvLoading.setVisibility(TextView.VISIBLE);
                            mTvLoading.setText("加热中，请稍后。。。");
                            break;
                        case PRINT_FILAMENT_CHANGED_STATE_LOADING:
                            mViewButtons.setVisibility(View.GONE);
                            mBtnHide.setVisibility(Button.GONE);
                            mBtnLoad.setVisibility(Button.GONE);
                            mBtnRetry.setVisibility(Button.GONE);
                            mBtnComplete.setVisibility(Button.GONE);

                            mTvTitle.setVisibility(TextView.GONE);
                            mTvDesc.setVisibility(TextView.GONE);
                            mTvLoading.setVisibility(TextView.VISIBLE);
                            mTvLoading.setText("装载物料中，请稍后。。。");
                            break;
                        case PRINT_FILAMENT_CHANGED_STATE_COMPLETE:
                            mViewButtons.setVisibility(View.VISIBLE);
                            mBtnHide.setVisibility(Button.GONE);
                            mBtnLoad.setVisibility(Button.GONE);
                            mBtnRetry.setVisibility(Button.VISIBLE);
                            mBtnComplete.setVisibility(Button.VISIBLE);

                            mTvTitle.setVisibility(TextView.VISIBLE);
                            mTvDesc.setVisibility(TextView.VISIBLE);
                            mTvLoading.setVisibility(TextView.GONE);

                            mTvTitle.setText("物料装载完成");
                            mTvDesc.setText("请检察喷嘴口有物料挤出；\n" +
                                    "若有，则装载成功，请点击继续打印按钮继续未完成的打印。\n" +
                                    "若无，请点击重试按钮再次装载。");
                            break;
                        default:
                            break;
                    }
                });
    }

    public void mockLoad() {
        mFilamentChangedStateSubject.onNext(PRINT_FILAMENT_CHANGED_STATE_COMPLETE);
    }

    public void mockHeat() {
        mFilamentChangedStateSubject.onNext(PRINT_FILAMENT_CHANGED_STATE_LOADING);
        AndroidSchedulers.mainThread().scheduleDirect(this::mockLoad, 2000, TimeUnit.MILLISECONDS);
    }

    @OnClick(R2.id.btn_print_filament_changed_hide)
    void onClickHide() {
        playNormalClickSound();
        back();
    }

    @OnClick(R2.id.btn_print_filament_changed_retry)
    void onClickRetry() {
        playNormalClickSound();
        mFilamentChangedStateSubject.onNext(PRINT_FILAMENT_CHANGED_STATE_IDLE);
    }

    @OnClick(R2.id.btn_print_filament_changed_load)
    void onClickLoad() {
        playNormalClickSound();
        mFilamentChangedStateSubject.onNext(PRINT_FILAMENT_CHANGED_STATE_HEATING);
        AndroidSchedulers.mainThread().scheduleDirect(this::mockHeat, 2000, TimeUnit.MILLISECONDS);
    }

    @OnClick(R2.id.btn_print_filament_changed_complete)
    void onClickComplete() {
        playNormalClickSound();
        // TODO: Print resume after filament loaded.
//        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setResume();
        back();
    }
}
