package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.manual;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedViewModel;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.ui.common.CalibrationPanelAdapter;
import fabscreen.platform.core.ui.data.calibration.CalibrationPoint;
import fabscreen.platform.core.ui.view.A400DirectionControlPanelTemp;
import fabscreen.platform.core.ui.view.ChessboardView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LevelingBedCalibrationManualFragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.cp_a400_leveling_bed_calibration_move)
    A400DirectionControlPanelTemp mCpMove;
    @BindView(R2.id.bt_a400_leveling_bed_calibration_manual_next)
    Button mBtnSave;
    @BindView(R2.id.cv_a400_leveling_bed_calibration_grid)
    ChessboardView mCVCalibrationGrid;
    private int calibrationIndex = 1;
    private A400LevelingBedViewModel mViewModel;
    private CalibrationPanelAdapter mAdapter;
    int gridCount;

    public static Fragment newInstance() {
        return new A400LevelingBedCalibrationManualFragment();
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
        mViewModel.startCalibration()
                .as(bindToLifecycle())
                .subscribe(order -> {
                    mViewModel.setCalibrationPoint(calibrationIndex);
                }, LogHelper::log);

        // start calibration
//        mViewModel.startCalibration();
    }

    private void initView() {
        gridCount = mViewModel.getGridCount();
        setTitle(R.string.calibration_heated_bed_leveling_title);
        setContent(getString(R.string.calibration_heated_bed_leveling_sub_title, "(" + 1 + "/" + gridCount * gridCount + ")"));
        mBtnSave.setText(R.string.all_next_point);
        int gridCount = mViewModel.getGridCount();
        ArrayList<ChessboardView.ProcessedPiece> processedPieces = new ArrayList<>();
        for (int i = 0; i < gridCount * gridCount; i++) {
            processedPieces.add(new ChessboardView.ProcessedPiece());
        }
        processedPieces.get(0).processedPieceState = ChessboardView.ProcessedPieceState.PROCESSING;
        mCVCalibrationGrid.setData(gridCount, gridCount, processedPieces);

        mGuideProgressBar.setMax(1);
        mGuideProgressBar.setProgress(1);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);

        mAdapter = new CalibrationPanelAdapter(getContext());
        ArrayList<CalibrationPoint> points = mViewModel.initPoints();
        mAdapter.setPoints(points);
//        mAdapter.setOnPointClickListener(point -> mViewModel.gotoPointManual(point.getViewOrder()));

        mCpMove.setStepWidths(0.02f, 0.1f, 1, 5).setOnDirectionClickListener((direction, stepWidth) -> mViewModel.move(direction, stepWidth));

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    // show Move
                    mCpMove.setEnabled(!isMoving);
                    mBtnSave.setEnabled(!isMoving);
                    mAdapter.setButtonsEnabled(!isMoving && !mViewModel.isAutoMode());
                });

        mViewModel.getIsMovePopUpSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    if (isMoving) {
                        fabLoading.show();
                    } else {
                        fabLoading.dismiss();
                    }
                });

        mViewModel.getCalibrationObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(pointIndex -> {
                    int viewOrder = mViewModel.getViewOrder(pointIndex);
                    if (viewOrder < gridCount * gridCount) {
                        mCVCalibrationGrid.setChangPieceState(mViewModel.getCoordinateOrder(pointIndex), ChessboardView.ProcessedPieceState.PROCESSED, mViewModel.getCoordinateOrderByViewOrder(viewOrder + 1), ChessboardView.ProcessedPieceState.PROCESSING);
                    }
                    if (viewOrder + 1 == gridCount * gridCount) {
                        mBtnSave.setText(R.string.all_save);
                    }
                    StringBuffer sb = new StringBuffer();
                    setContent(getString(R.string.calibration_heated_bed_leveling_sub_title, "(" + (viewOrder + 1) + "/" + gridCount * gridCount + ")"));
                });

    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_bed_calibration_manual;
    }

    @Override
    protected A400LevelingBedViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingBedViewModel.class);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @OnClick(R2.id.bt_a400_leveling_bed_calibration_manual_next)
    public void onCheckNext() {
        playNormalClickSound();
        if (calibrationIndex < mViewModel.getGridCount() * mViewModel.getGridCount()) {
            mViewModel.setCalibrationPoint(++calibrationIndex);
        } else {
            getServiceContainer().getService(IMachine.class).getFDMController().exitCalibration(true).as(bindToLifecycle()).subscribe(response -> {
                if (response.isSuccess()) {
                    ((A400LevelingBedCalibrationManualActivity) requireActivity()).gotoLevelingBedCalibrationComplete();
                }
            });
        }
    }
}
