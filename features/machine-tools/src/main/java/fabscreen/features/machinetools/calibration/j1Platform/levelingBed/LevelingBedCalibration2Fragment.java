package fabscreen.features.machinetools.calibration.j1Platform.levelingBed;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class LevelingBedCalibration2Fragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.tv_calibration_instructions_content)
    TextView mContent;
    @BindView(R2.id.tv_calibration_instructions_title)
    TextView mTitle;
    @BindView(R2.id.top_bar_back)
    Button mBtBack;
    @BindView(R2.id.btn_next)
    Button mBtNext;

    private int mIndex = 2;
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    public static Fragment newInstance() {
        return new LevelingBedCalibration2Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mIsMovingSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMove -> {
                    mBtBack.setEnabled(!isMove);
                    mBtNext.setEnabled(!isMove);
                });
        mTitle.setText(R.string.leveling_bed_calibration_left_point_title);
        mContent.setText(R.string.leveling_bed_calibration_left_point_content);

    }

    private void moveIndex() {
        fabMoving.show();
        mIsMovingSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .calibratePointByIndex(mIndex, false)
                .as(bindToLifecycle())
                .subscribe(success -> {
                            if (success.isSuccess()) {
                                fabMoving.dismiss();
                                switch (mIndex) {
                                    case 3:
                                        mTitle.setText(R.string.leveling_bed_calibration_right_point_title);
                                        mContent.setText(R.string.leveling_bed_calibration_right_point_content);
                                        break;
                                    default:
                                        break;
                                }
                                mIsMovingSubject.onNext(false);
                            } else if (success.isGeneralError()) {
                                mIsMovingSubject.onNext(false);
                                back();
                            }

                        },
                        e -> {
                            // Mobile failure
                            fabMoving.show();
                            mIsMovingSubject.onNext(false);
                        });
    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        switch (mIndex) {
            case 2:
                mIndex++;
                moveIndex();
                break;
            case 3:
                ServiceContainer.getInstance().getService(IMachine.class)
                        .getFDMController()
                        .exitCalibration(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                            if (success.isSuccess()) {
                                if (getActivity() == null) return;
                                ((LevelingBedCalibrationActivity) getActivity()).gotoCalibrationSuccess();
                            }
                        });

                break;
            default:
                break;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_calibration_instructions;
    }


}
