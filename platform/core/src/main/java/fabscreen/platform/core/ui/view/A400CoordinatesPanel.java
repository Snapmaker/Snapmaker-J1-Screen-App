package fabscreen.platform.core.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

@SuppressLint("NonConstantResourceId")
public class A400CoordinatesPanel extends ConstraintLayout {

    @BindView(R2.id.tv_x_value)
    TextView mTvXValue;
    @BindView(R2.id.tv_y_value)
    TextView mTvYValue;
    @BindView(R2.id.tv_z_value)
    TextView mTvZValue;
    @BindView(R2.id.tv_coordinate_types)
    TextView mTvCoordinateTypes;
    @BindView(R2.id.tv_run_boundary)
    TextView mTvRunBoundary;
    @BindView(R2.id.v_b_line)
    View mVBLine;
    @BindView(R2.id.tv_b_value)
    TextView mTvBValue;
    @BindView(R2.id.tv_b_degree)
    TextView mTvBDegree;
    @BindView(R2.id.btn_set_b_origin)
    Button mBtnSetBOrigin;
    @BindView(R2.id.tv_b_title)
    TextView mTvBTitle;

    @BindView(R2.id.btn_set_x_origin)
    Button mBtnSetOriginX;
    @BindView(R2.id.btn_set_y_origin)
    Button mBtnSetOriginY;
    @BindView(R2.id.btn_set_z_origin)
    Button mBtnSetOriginZ;
    @BindView(R2.id.btn_set_xyz_origin)
    Button mBtnSetOriginXYZ;

    public static final int X_TYPE = 101;
    public static final int Y_TYPE = 102;
    public static final int Z_TYPE = 103;
    public static final int XYZ_TYPE = 104;
    public static final int B_TYPE = 105;

    public Context mContext;
    private MenuAdapter mPopupAdapter;
    private OnCoordinatesOnClickListener mDirectionListener;
    private int mSelectMunPosition = 0;


    public A400CoordinatesPanel(@NonNull Context context) {
        super(context);
        init(context);
    }

    public A400CoordinatesPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_coordinates_panel, this);
        mContext = context;
        ButterKnife.bind(this, view);

        //Text underline
        mTvRunBoundary.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTvRunBoundary.getPaint().setAntiAlias(true);
    }

    public A400CoordinatesPanel setCoordinatesList(List<String> list) {
        mPopupAdapter = new MenuAdapter(mContext, list);
        mTvCoordinateTypes.setText(list.get(0));
        mPopupAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mSelectMunPosition = position;
                mTvCoordinateTypes.setText(list.get(position));
                if (mDirectionListener != null) {
                    mDirectionListener.onPopupOnClicked(position);
                }
                PullDownMenu.dismiss();
            }
        });
        return this;
    }

    public void setCoordinatesValue(String x, String y, String z) {
        mTvXValue.setText(x);
        mTvYValue.setText(y);
        mTvZValue.setText(z);
    }

    public void setCoordinatesValue(String x, String y, String z, String b) {
        mTvXValue.setText(x);
        mTvYValue.setText(y);
        mTvZValue.setText(z);
        mTvBValue.setText(b);
    }

    public void setBAxisVisibility(boolean isVisibility) {
        mTvBValue.setVisibility(isVisibility ? VISIBLE : GONE);
        mTvBDegree.setVisibility(isVisibility ? VISIBLE : GONE);
        mVBLine.setVisibility(isVisibility ? VISIBLE : GONE);
        mBtnSetBOrigin.setVisibility(isVisibility ? VISIBLE : GONE);
        mTvBTitle.setVisibility(isVisibility ? VISIBLE : GONE);
    }

    public void setXYZVisibility(boolean isVisibility) {
        mBtnSetOriginX.setVisibility(isVisibility ? VISIBLE : GONE);
        mBtnSetOriginY.setVisibility(isVisibility ? VISIBLE : GONE);
        mBtnSetOriginZ.setVisibility(isVisibility ? VISIBLE : GONE);
        mBtnSetOriginXYZ.setVisibility(isVisibility ? VISIBLE : GONE);
    }

    public void setRunBoundaryVisibility(boolean isVisibility) {
        mTvRunBoundary.setVisibility(isVisibility ? VISIBLE : GONE);
    }

    @OnClick({R2.id.tv_coordinate_types, R2.id.iv_coordinate_types_arrow, R2.id.btn_set_x_origin, R2.id.btn_set_y_origin,
            R2.id.btn_set_z_origin, R2.id.btn_set_b_origin, R2.id.btn_set_xyz_origin, R2.id.tv_run_boundary})
    void onClickItem(View view) {
        if (mDirectionListener == null) {
            return;
        }
        int id = view.getId();
        if (id == R.id.tv_coordinate_types || id == R.id.iv_coordinate_types_arrow) {
            PullDownMenu.create(getContext(), mPopupAdapter)
                    .showDownView(mTvCoordinateTypes);
            if (mPopupAdapter != null) {
                mPopupAdapter.setSelectPosition(mSelectMunPosition);
            }

        } else if (id == R.id.btn_set_x_origin) {
            mDirectionListener.onDirectionClicked(X_TYPE, view.getId());

        } else if (id == R.id.btn_set_y_origin) {
            mDirectionListener.onDirectionClicked(Y_TYPE, view.getId());

        } else if (id == R.id.btn_set_z_origin) {
            mDirectionListener.onDirectionClicked(Z_TYPE, view.getId());

        } else if (id == R.id.btn_set_b_origin) {
            mDirectionListener.onDirectionClicked(B_TYPE, view.getId());
        } else if (id == R.id.btn_set_xyz_origin) {
            mDirectionListener.onDirectionClicked(XYZ_TYPE, view.getId());
        } else if (id == R.id.tv_run_boundary) {
            mDirectionListener.onClickRunBoundary();
        }
    }

    public void setOnDirectionClickListener(OnCoordinatesOnClickListener listener) {
        mDirectionListener = listener;
    }

    public interface OnCoordinatesOnClickListener {
        void onDirectionClicked(int type, int viewId);

        void onPopupOnClicked(int position);

        void onClickRunBoundary();
    }
}
