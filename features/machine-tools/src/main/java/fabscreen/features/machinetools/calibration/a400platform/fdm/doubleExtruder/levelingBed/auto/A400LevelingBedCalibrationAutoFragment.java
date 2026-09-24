package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.auto;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.ChessboardView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LevelingBedCalibrationAutoFragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.tv_a400_leveling_bed_calibration_auto_title)
    TextView mTvAutoTitle;
    @BindView(R2.id.tv_a400_leveling_bed_calibration_auto_content)
    TextView mTvAutoContent;
    @BindView(R2.id.cv_a400_leveling_bed_calibration_grid)
    ChessboardView mCVCalibrationGrid;
    @BindView(R2.id.top_bar_ico)
    ImageView mIvProProblemIcon;

    int gridCount;
    private A400LevelingBedViewModel mViewModel;
    private int mLastPoint = 0;

    public static Fragment newInstance() {
        return new A400LevelingBedCalibrationAutoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvProProblemIcon.setVisibility(View.GONE);
        gridCount = mViewModel.getGridCount();
        initView();
        mViewModel.startCalibration()
                .as(bindToLifecycle())
                .subscribe(order -> {
                }, LogHelper::log);
    }

    private void initView() {
        setTitle(R.string.calibration_heated_bed_leveling_title);
        setContent(R.string.calibration_heated_bed_leveling_content_2);
        mTvAutoContent.setText(getText(R.string.a400_calibration_headted_bed_leveing_wait_time));
        mGuideProgressBar.setMax(2);
        mGuideProgressBar.setProgress(2);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mTvAutoTitle.setText(getString(R.string.a400_calibration_headted_bed_leveing_gide, 0, gridCount * gridCount));
        int gridCount = mViewModel.getGridCount();
        ArrayList<ChessboardView.ProcessedPiece> processedPieces = new ArrayList<>();
        for (int i = 0; i < gridCount * gridCount; i++) {
            processedPieces.add(new ChessboardView.ProcessedPiece());
        }
        processedPieces.get(0).processedPieceState = ChessboardView.ProcessedPieceState.PROCESSING;
        mLastPoint = 0;
        mCVCalibrationGrid.setData(gridCount, gridCount, processedPieces);
        mViewModel.getCalibrationObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(pointIndex -> {
                    int viewOrder = mViewModel.getViewOrder(pointIndex);
                    if (viewOrder < this.gridCount * this.gridCount) {
                        mCVCalibrationGrid.setChangPieceState(mViewModel.getCoordinateOrder(pointIndex), ChessboardView.ProcessedPieceState.PROCESSED, mViewModel.getCoordinateOrderByViewOrder(viewOrder + 1), ChessboardView.ProcessedPieceState.PROCESSING);
                    }
                    mTvAutoTitle.setText(getString(R.string.a400_calibration_headted_bed_leveing_gide, viewOrder, this.gridCount * this.gridCount));
                    mTvAutoContent.setText(getString(R.string.a400_calibration_headted_bed_leveing_time, mViewModel.getWholeCalculateTime(), Math.abs(this.gridCount * this.gridCount - viewOrder) * mViewModel.LEVELING_POINT_TIME));
                    if (pointIndex == this.gridCount * this.gridCount) {
                        saveCalibration();
                    }
                });
    }

    private void saveCalibration() {
        getServiceContainer().getService(IMachine.class).getFDMController().exitCalibration(true).as(bindToLifecycle()).subscribe(response -> {
            if (response.isSuccess()) {
                coolDownBedIfHave();
                finishActivityWithResultOk();
            }
        });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_bed_calibration_auto;
    }

    @Override
    protected A400LevelingBedViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingBedViewModel.class);
    }

    @Override
    protected void back() {
        fabBackConfirm = DecisionDialog.create(getContext())
                .setTitle(R.string.calibration_heated_bed_leveling_title)
                .setContent(getString(R.string.assistant_back_notice, getString(R.string.calibration_heated_bed_leveling_title)))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> dialog.dismiss()))
                .setSecondTv(getContext().getResources().getString(R.string.all_yes), R.color.select_dialog_yellow_txt, ((dialog, which) -> {
                    fabBackConfirm.mCancelBtn.setEnabled(false);
                    fabBackConfirm.mSecondBtn.setEnabled(false);
                    mViewModel.getInterruptAutoLevelingObservable()
                            .flatMap(responseStructure -> ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(false))
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                if (!success.isSuccess()) {
                                    Logger.d("Exit Calibration: " + success);
                                }
                                dialog.dismiss();
                                coolDownBedIfHave();
                                requireActivity().setResult(Activity.RESULT_CANCELED);
                                requireActivity().finish();
                            }, LogHelper::log);
                }));
        fabBackConfirm.show();
    }
}
