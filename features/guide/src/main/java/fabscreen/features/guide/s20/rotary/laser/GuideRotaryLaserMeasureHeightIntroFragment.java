package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GuideRotaryLaserMeasureHeightIntroFragment extends BaseFragment {
    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;
    @BindView(R2.id.btn_guide_intro_next)
    Button mBtnNext;
    private LaserCalibrationViewModel mViewModel;
    private CoordinateSystemPresenter mCoordinateSystemPresenter;

    public static GuideRotaryLaserMeasureHeightIntroFragment newInstance() {
        return new GuideRotaryLaserMeasureHeightIntroFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(0);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_intro;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    private void initView() {
        mIvCover.setImageResource(R.drawable.pic_rotary_laser_work_table_360x320);

        mTvTitle.setText(R.string.guide_laser_measure_height_title);
        mTvContent.setText(R.string.guide_laser_rotary_measure_height_intro_content);
    }

    private void startMovements() {
        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float materialLength = mViewModel.getWorkpieceLength();

        float initialY = Math.min(sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - (2 * 10 + 1), sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - materialLength);

        float z = MockConst.LASER_MOCK_ROTARY_WASTE_BOARD_HEIGHT
                + MockConst.LASER_MOCK_ROTARY_HEIGHT
                + (mViewModel.getWorkpieceDiameter() / 2f)
                + MockConst.LASER_HOOD_HEIGHT
                + 15;

        // Make sure on CS#0 and then go to center for measuring height.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f - 6, initialY)))
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    finish();
                }, e -> {
                    mBtnNext.setEnabled(true);
                    LogHelper.log(e);
                    Logger.e("Failed to move.");
                });
    }

    private void finish() {
        mBtnNext.setEnabled(true);
        if (getActivity() == null) return;
        ((GuideRotaryLaserActivity) getActivity()).startMeasureHeightFragment();
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        mBtnNext.setEnabled(false);
        startMovements();
    }
}
