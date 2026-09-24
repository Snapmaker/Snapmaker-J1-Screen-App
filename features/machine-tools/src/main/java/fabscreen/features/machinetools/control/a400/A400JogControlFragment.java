package fabscreen.features.machinetools.control.a400;

import static fabscreen.platform.core.ui.data.MoveController.Direction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30JogControlViewModel;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.common.a400jogpanel.JogPanelViewPagerAdapter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.TOOLS_CONTROL_A400_JOG)
public class A400JogControlFragment extends BaseFragment {

    @BindView(R2.id.tv_x_value)
    TextView mTvXValue;
    @BindView(R2.id.tv_y_value)
    TextView mTvYValue;
    @BindView(R2.id.tv_z_value)
    TextView mTvZValue;
    @BindView(R2.id.tv_b_value)
    TextView mTvBValue;
    @BindView(R2.id.btn_set_x_origin)
    Button mBtnSetOriginX;
    @BindView(R2.id.btn_set_y_origin)
    Button mBtnSetOriginY;
    @BindView(R2.id.btn_set_z_origin)
    Button mBtnSetOriginZ;
    @BindView(R2.id.btn_set_xyz_origin)
    Button mBtnSetOriginXYZ;
    @BindView(R2.id.ll_coordinate_chooser)
    LinearLayout mLlCoordinateChooser;
    @BindView(R2.id.tv_coordinate_type)
    TextView mTvCoordinateType;
    @BindView(R2.id.rg_step)
    RadioGroup mRgStep;
    @BindView(R2.id.vp_direction_buttons)
    ViewPager2 mVpDirectionButtons;

    @BindView(R2.id.v_dot_0)
    View mVDot0;
    @BindView(R2.id.v_dot_1)
    View mVDot1;

    @BindView(R2.id.sv_values)
    ScrollView mSvCoordinateValues;
    @BindView(R2.id.group_set_origin_inner)
    Group mGroupSetOriginInner;
    @BindView(R2.id.group_set_origin_outer)
    Group mGroupSetOriginOuter;
    @BindView(R2.id.btn_home)
    View mBtnHome;
    @BindView(R2.id.v_coordinate_divider)
    View mVCoordinateDivider;

    private S30JogControlViewModel mViewModel;

    private PopupWindow mPopupWindow;
    private DecisionDialog mDecisionDialog;
    private List<Integer> mAxisTypes;
    private JogPanelViewPagerAdapter mJogPanelAdapter;

    public static Fragment newInstance() {
        return new A400JogControlFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getFragmentScopeViewModel(S30JogControlViewModel.class);
        mViewModel.subscribeCoordinate();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mDecisionDialog = DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE);

        updateCoordinateView();

        // rotary show hide
        setRotaryStuffVisibility(mViewModel.isRotaryAvailable());

        initRadioGroup();

        // view pager
        initControlPanelViewPager();

        initCoordinateTypeChooser();

        // check work type
        refreshSetOriginViews();

        watchMovingState();

        watchHomingState();
    }

    private void watchHomingState() {
        mViewModel.getHomingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshWhenHoming, LogHelper::log);
    }

    private void refreshWhenHoming(Boolean isHoming) {
        mBtnHome.setEnabled(!isHoming);
        if (mViewModel.getWorkType() == IMachine.WorkType.FDM) {
            mBtnHome.setBackgroundResource(isHoming ? R.drawable.pic_a400_control_home_bg_large_active : R.drawable.selector_a400_bg_contorl_home_large);
        } else {
            mBtnHome.setBackgroundResource(isHoming ? R.drawable.pic_a400_control_home_bg_active : R.drawable.selector_a400_bg_control_home);
        }
        mJogPanelAdapter.onMachineMoving(isHoming ? Direction.ALL : Direction.NONE);
    }

    private void watchMovingState() {
        mViewModel.getMoveStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshByMovingState, LogHelper::log);
    }

    private void refreshByMovingState(Direction direction) {
        if (mJogPanelAdapter == null) return;
        mJogPanelAdapter.onMachineMoving(direction);
        mBtnHome.setEnabled(direction == Direction.NONE);
    }

    private void refreshSetOriginViews() {
        switch (mViewModel.getWorkType()) {
            case FDM:
                // invisible the set origin views
                mGroupSetOriginInner.setVisibility(View.INVISIBLE);
                mGroupSetOriginOuter.setVisibility(View.INVISIBLE);
                // enlarge the home button
                mBtnHome.getLayoutParams().width = (int) DimensUtils.dp2px(451f);
                mBtnHome.setBackgroundResource(R.drawable.selector_a400_bg_contorl_home_large);
                mVCoordinateDivider.getLayoutParams().width = (int) DimensUtils.dp2px(260f);
                mBtnHome.requestLayout();
                break;
            case LASER:
            case CNC:
                mGroupSetOriginInner.setVisibility(View.VISIBLE);
                mGroupSetOriginOuter.setVisibility(View.VISIBLE);
                break;
            case NONE:
                break;
        }
    }

    private void initCoordinateTypeChooser() {
        LinearLayout coordinateTypeChooser = (LinearLayout) LayoutInflater.from(requireContext()).inflate(R.layout.popup_coordinate_types, (ViewGroup) requireView(), false);
        TextView tvWork = coordinateTypeChooser.findViewById(R.id.tv_work);
        TextView tvMachine = coordinateTypeChooser.findViewById(R.id.tv_machine);
        //default one
        tvWork.setActivated(true);

        tvWork.setOnClickListener(v -> {
            setChildItemActivated(coordinateTypeChooser, 0);
            mTvCoordinateType.setText(R.string.work_coordinates);
            mViewModel.setCoordinateType(S30JogControlViewModel.WORK);
            mPopupWindow.dismiss();
        });
        tvMachine.setOnClickListener(v -> {
            setChildItemActivated(coordinateTypeChooser, 1);
            mTvCoordinateType.setText(R.string.machine_coordinates);
            mViewModel.setCoordinateType(S30JogControlViewModel.MACHINE);
            mPopupWindow.dismiss();
        });

        mPopupWindow = new PopupWindow(coordinateTypeChooser, (int) DimensUtils.dp2px(360f), (int) DimensUtils.dp2px(218f));
        mPopupWindow.setElevation(8);
        mPopupWindow.setContentView(coordinateTypeChooser);
    }

    private void initControlPanelViewPager() {
        mJogPanelAdapter = new JogPanelViewPagerAdapter(mAxisTypes);
        mVpDirectionButtons.setAdapter(mJogPanelAdapter);
        mVpDirectionButtons.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                changeSign(position);
                scrollCoordinate(position);
                refreshRadioButtons(position);
            }
        });

        mJogPanelAdapter.setOnItemClickListener((direction) -> {
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


        });
    }

    private void initRadioGroup() {
        setRadioGroupSound();
        mRgStep.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_0_1) {
                mViewModel.changeStepWidth(0);
            } else if (checkedId == R.id.rb_1_0) {
                mViewModel.changeStepWidth(1);
            } else if (checkedId == R.id.rb_10) {
                mViewModel.changeStepWidth(2);
            } else if (checkedId == R.id.rb_100) {
                mViewModel.changeStepWidth(3);
            }
        });
        mRgStep.check(R.id.rb_1_0);
    }

    private void setRadioGroupSound() {
        for (int i = 0; i < mRgStep.getChildCount(); i++) {
            View child = mRgStep.getChildAt(i);
            child.setOnClickListener(v -> playNormalClickSound());
        }
    }

    private void setChildItemActivated(LinearLayout chooser, int pos) {
        for (int i = 0; i < chooser.getChildCount(); i++) {
            View child = chooser.getChildAt(i);
            child.setActivated(i == pos);
        }
    }

    private void refreshRadioButtons(int position) {
        if (mRgStep.getChildCount() < 4) return;
        String[] strs;
        if (position == 0) {
            strs = new String[]{"100mm", "10mm", "1mm", "0.1mm"};
        } else {
            strs = new String[]{"90°", "10°", "1°", "0.2°"};
        }
        ((RadioButton) mRgStep.getChildAt(0)).setText(strs[0]);
        ((RadioButton) mRgStep.getChildAt(1)).setText(strs[1]);
        ((RadioButton) mRgStep.getChildAt(2)).setText(strs[2]);
        ((RadioButton) mRgStep.getChildAt(3)).setText(strs[3]);
    }

    private void scrollCoordinate(int position) {
        int scrollDestY = 0;
        if (position == 1) {
            scrollDestY = mSvCoordinateValues.getHeight();
        }
        mSvCoordinateValues.smoothScrollTo(0, scrollDestY);
    }

    private void changeSign(int position) {
        mVDot0.setBackgroundResource(position == 0 ? R.drawable.ic_view_pager_normal : R.drawable.ic_view_pager_select);
        mVDot1.setBackgroundResource(position == 1 ? R.drawable.ic_view_pager_normal : R.drawable.ic_view_pager_select);
    }

    private void updateCoordinateView() {
        mViewModel.getCoordinateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(vector -> {
                    mTvXValue.setText(String.format(Locale.US, "%.2f", vector.getX()));
                    mTvYValue.setText(String.format(Locale.US, "%.2f", vector.getY()));
                    mTvZValue.setText(String.format(Locale.US, "%.2f", vector.getZ()));
                    mTvBValue.setText(String.format(Locale.US, "%.2f", vector.getB()));
                }, LogHelper::log);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setRotaryStuffVisibility(boolean visible) {
        // coordinate, set B visibility by make scroll view scrollable/un-scrollable
        mSvCoordinateValues.setVerticalScrollBarEnabled(visible);
        mSvCoordinateValues.setOnTouchListener((v, event) -> !visible);
        // control panel
        mAxisTypes = new ArrayList<>();
        mAxisTypes.add(JogPanelViewPagerAdapter.XYZ_TYPE);
        if (visible) {
            mAxisTypes.add(JogPanelViewPagerAdapter.B_TYPE);
            mVDot0.setVisibility(View.VISIBLE);
            mVDot1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_control_move;
    }

    @OnClick(R2.id.btn_set_x_origin)
    void onClickSetOriginX(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setX(0);
        mViewModel.setOrigin(vector);
    }

    @OnClick(R2.id.btn_set_y_origin)
    void onClickSetOriginY(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setY(0);
        mViewModel.setOrigin(vector);
    }

    @OnClick(R2.id.btn_set_z_origin)
    void onClickSetOriginZ(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setZ(0);
        mViewModel.setOrigin(vector);
    }

    @OnClick(R2.id.btn_set_b_origin)
    void onClickSetOriginB(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setB(0);
        mViewModel.setOrigin(vector);
    }

    @OnClick(R2.id.btn_set_xyz_origin)
    void onClickSetOrigin(View view) {
        playNormalClickSound();
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        if (mViewModel.isRotaryAvailable()) {
            vector.setB(0);
        }
        mViewModel.setOrigin(vector);
    }

    @OnClick(R2.id.btn_home)
    void onHomeClicked() {
        playNormalClickSound();
        mViewModel.goHome(true)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (!success) {
                        mDecisionDialog.setContent("直线轴异常，需联系售后。")
                                .setFirstTv(requireContext().getString(R.string.all_confirm), R.color.select_dialog_blue_txt, (dialog, which) -> {
                                    dialog.dismiss();
                                }).show();
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.ll_coordinate_chooser)
    void onCoordinateChooserClicked() {
        playNormalClickSound();
        showCoordinateChooser();
    }

    private void showCoordinateChooser() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            mPopupWindow.showAsDropDown(mLlCoordinateChooser);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setTouchable(true);
            mPopupWindow.setOutsideTouchable(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.unSubscribeCoordinate();
    }
}
