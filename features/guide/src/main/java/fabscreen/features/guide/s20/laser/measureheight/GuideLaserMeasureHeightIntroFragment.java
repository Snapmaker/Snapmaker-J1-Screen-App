package fabscreen.features.guide.s20.laser.measureheight;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GuideLaserMeasureHeightIntroFragment extends BaseFragment {
    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;
    @BindView(R2.id.btn_guide_intro_next)
    Button mBtnNext;

    public static GuideLaserMeasureHeightIntroFragment newInstance() {
        return new GuideLaserMeasureHeightIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mIvCover.setImageResource(R.drawable.pic_guide_laser_measure_height_360x320);

        mTvTitle.setText(R.string.guide_laser_measure_height_title);
        mTvContent.setText(R.string.guide_laser_measure_height_content);
    }

    private void ensureHomed() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    boolean homed = coordinateSystem.isHomed;
                    if (homed) {
                        startMovements();
                    } else {
                        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0)
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(response -> checkHome());
                    }
                }, LogHelper::log);
    }

    private void checkHome() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    boolean homed = coordinateSystem.isHomed;
                    if (homed) {
                        startMovements();
                    } else {
                        AndroidSchedulers.mainThread().scheduleDirect(this::checkHome, 2000, TimeUnit.MILLISECONDS);
                    }
                }, LogHelper::log);
    }

    private void startMovements() {
        // Now offset x, y, z = 0 after homing.

        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        final float thickness = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();

        // Go to center of plat for measuring height.
        float z = MockConst.LASER_PLATE_SAFETY_HEIGHT + thickness + MockConst.LASER_HOOD_HEIGHT + 10;
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f, sizeY / 2f))
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    // done
                    finish();
                }, e -> {
                    LogHelper.log(e);
                    Logger.e("Failed to move.");
                });
    }

    private void finish() {
        mBtnNext.setEnabled(true);
        if (getActivity() == null) return;
        ((GuideLaserActivity) getActivity()).startMeasureHeightFragment();
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        mBtnNext.setEnabled(false);
        ensureHomed();
    }
}
