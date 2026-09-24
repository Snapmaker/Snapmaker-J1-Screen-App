package fabscreen.features.print.a400platform;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButton;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.PrepareJogControlViewModel;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.ui.common.a400jogpanel.JogPanelViewPagerAdapter;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.A400CoordinatesPanel;
import fabscreen.platform.core.ui.view.A400XYZBControlPanel;
import fabscreen.platform.core.ui.view.PullDownMenu;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.PREPARE_PRINT_JOG_CONTROL)
public class A400PrepareJogControlFragment extends BaseFragment {

    @BindView(R2.id.tv_x_value)
    TextView mTvXValue;
    @BindView(R2.id.tv_y_value)
    TextView mTvYValue;
    @BindView(R2.id.tv_z_value)
    TextView mTvZValue;
    @BindView(R2.id.tv_b_value)
    TextView mTvBValue;
    @BindView(R2.id.tv_b_title)
    TextView mTvBTitle;
    @BindView(R2.id.tv_b_degree)
    TextView mTvBDegree;


    @BindView(R2.id.acp_coordinate_type)
    A400CoordinatesPanel mCoordinatesPanel;
    @BindView(R2.id.xyzb_calibration_control)
    A400XYZBControlPanel mXYZBCalibrationControl;

    private PrepareJogControlViewModel mViewModel;

    private List<String> mList;
    private DecisionDialog mDecisionDialog;

    public static Fragment newInstance() {
        return new A400PrepareJogControlFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
        mViewModel.subscribeCoordinate();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

        mXYZBCalibrationControl.setRotaryStuffVisibility(mViewModel.isRotaryAvailable());
        mXYZBCalibrationControl.setOnDirectionClickListener(new A400XYZBControlPanel.OnDirectionClickListener() {
            @Override
            public void onDirectionClicked(MoveController.Direction direction, float stepWidth) {
                playNormalClickSound();
                mViewModel.moveToPosition(direction)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(responseStructure -> {
                            if (responseStructure.isGeneralError()) {
                                mDecisionDialog.setContent(getString(R.string.all_moving_limitation))
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

        mDecisionDialog = DecisionDialog.create(requireContext()).setDialogStatus(DecisionDialog.BTN_ONE,
                false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE);

        updateCoordinateView();

        mList = new ArrayList<>();
        mList.add(getString(R.string.work_coordinates));
        mList.add(getString(R.string.machine_coordinates));

        mCoordinatesPanel.setBAxisVisibility(mViewModel.isRotaryAvailable());
        mCoordinatesPanel.setRunBoundaryVisibility(true);
        mCoordinatesPanel.setCoordinatesList(mList)
                .setOnDirectionClickListener(new A400CoordinatesPanel.OnCoordinatesOnClickListener() {
                    @Override
                    public void onDirectionClicked(int type, int viewId) {
                        playNormalClickSound();
                        switch (type) {
                            case A400CoordinatesPanel.X_TYPE:
                                Vector AVector = new Vector();
                                AVector.setX(0);
                                mViewModel.setOrigin(AVector, viewId);
                                break;

                            case A400CoordinatesPanel.Y_TYPE:
                                Vector BVector = new Vector();
                                BVector.setY(0);
                                mViewModel.setOrigin(BVector, viewId);
                                break;

                            case A400CoordinatesPanel.Z_TYPE:
                                Vector ZVector = new Vector();
                                ZVector.setZ(0);
                                mViewModel.setOrigin(ZVector, viewId);
                                break;
                            case A400CoordinatesPanel.XYZ_TYPE:
                                Vector XYZVector = new Vector();
                                XYZVector.setX(0);
                                XYZVector.setY(0);
                                XYZVector.setZ(0);
                                mViewModel.setOrigin(XYZVector, viewId);
                                break;
                        }
                    }

                    @Override
                    public void onPopupOnClicked(int position) {
                        mViewModel.setCoordinateType(position);
                    }

                    @Override
                    public void onClickRunBoundary() {
                        if (getParentFragment() != null && getParentFragment() instanceof SetOriginFragment) {
                            ((SetOriginFragment) getParentFragment()).runBoundary();
                        }
                    }
                });

        // check work type
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        switch (workType) {
            case FDM:
                mCoordinatesPanel.setXYZVisibility(false);
                break;
            case LASER:
            case CNC:
                mCoordinatesPanel.setXYZVisibility(true);
                break;
            case NONE:
                break;
        }
    }


    private void updateCoordinateView() {
        mViewModel.getCoordinateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(vector -> {
                    mCoordinatesPanel.setCoordinatesValue(
                            String.format(Locale.US, "%.2f", vector.getX()),
                            String.format(Locale.US, "%.2f", vector.getY()),
                            String.format(Locale.US, "%.2f", vector.getZ()),
                            String.format(Locale.US, "%.2f", vector.getB())
                    );
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_prepare_jog;
    }

    @Override
    protected PrepareJogControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(PrepareJogControlViewModel.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.unSubscribeCoordinate();
    }
}
