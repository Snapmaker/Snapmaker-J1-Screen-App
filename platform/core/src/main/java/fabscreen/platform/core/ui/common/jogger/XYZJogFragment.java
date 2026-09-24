package fabscreen.platform.core.ui.common.jogger;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.A400XYZBControlPanel;
import fabscreen.platform.core.ui.view.CustomSteeringView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class XYZJogFragment extends BaseFragment {

    @BindView(R2.id.xyzb_calibration_control)
    A400XYZBControlPanel mXYZBCalibrationControl;

    private XYZJogViewModel mViewModel;
    private DecisionDialog mDecisionDialog;

    private BehaviorSubject<Boolean> mButtonsEnableSubject = BehaviorSubject.create();

    public static Fragment newInstance() {
        return new XYZJogFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_xyz_jog;
    }

    protected void initView() {
        mXYZBCalibrationControl.setRotaryStuffVisibility(mViewModel.isRotaryAvailable());
        mXYZBCalibrationControl.setOnDirectionClickListener(new A400XYZBControlPanel.OnDirectionClickListener() {
            @Override
            public void onDirectionClicked(MoveController.Direction direction, float stepWidth) {
                playNormalClickSound();
                mViewModel.moveToPosition(direction)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(responseStructure -> {
                            if (!responseStructure.isSuccess()) {
                                mDecisionDialog.setContent(getString(R.string.all_moving_limitation)+ responseStructure.resultProp.getValue())
                                        .setFirstTv(requireContext().getString(R.string.all_confirm),
                                                R.color.select_dialog_blue_txt, (dialog, which) -> {
                                                    dialog.dismiss();
                                                }).show();
                            }
                        });
            }

            @Override
            public void onPositionChange(int position) {
                mViewModel.changeStepWidth(position);
            }
        });
        mViewModel.getMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> mButtonsEnableSubject.onNext(!isMoving), LogHelper::log);
    }

    public Observable<Boolean> getButtonsEnableObservable() {
        return mButtonsEnableSubject.hide();
    }


    @Override
    protected XYZJogViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(XYZJogViewModel.class);
    }
}
