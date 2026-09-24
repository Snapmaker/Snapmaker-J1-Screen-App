package fabscreen.platform.core.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import fabscreen.platform.core.R;

public class CustomSteeringView extends SteeringView {
    private SteeringViewSkin mSkin;
    private boolean mFrozen = false;

    public CustomSteeringView(Context context) {
        super(context);
        mSkin = new SteeringViewSkin(context);
    }

    public CustomSteeringView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mSkin = new SteeringViewSkin(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFrozen) {
            setEnabled(false);
            showFrozenBackgrounds();
            return;
        }
        if (!isEnabled()) {
            setBackground(mSkin.getDrawable(STATE_DISABLE));
        } else {
            switch (mDirection) {
                case DRECTION_IDLE:
                    setBackground(mSkin.getDrawable(DRECTION_IDLE));
                    break;
                case DIRECTION_UP:
                    setBackground(mSkin.getDrawable(DIRECTION_UP));
                    break;
                case DIRECTION_DOWN:
                    setBackground(mSkin.getDrawable(DIRECTION_DOWN));
                    break;
                case DIRECTION_LEFT:
                    setBackground(mSkin.getDrawable(DIRECTION_LEFT));
                    break;
                case DIRECTION_RIGHT:
                    setBackground(mSkin.getDrawable(DIRECTION_RIGHT));
                    break;
            }
        }
    }

    private void showFrozenBackgrounds() {
        switch (mDirection) {
            case DIRECTION_UP:
                setBackground(mSkin.getDrawable(STATE_MOVING_UP));
                break;
            case DIRECTION_DOWN:
                setBackground(mSkin.getDrawable(STATE_MOVING_DOWN));
                break;
            case DIRECTION_LEFT:
                setBackground(mSkin.getDrawable(STATE_MOVING_LEFT));
                break;
            case DIRECTION_RIGHT:
                setBackground(mSkin.getDrawable(STATE_MOVING_RIGHT));
                break;
            case DRECTION_IDLE:
                setBackground(mSkin.getDrawable(DRECTION_IDLE));
                break;
        }
    }


    public void setSkin(int skin) {
        mSkin = new SteeringViewSkin(getContext(), skin);
        invalidate();
    }

    public void setFrozenWithDirection(boolean frozen, int direction) {
        mFrozen = frozen;
        mDirection = direction;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public static class SteeringViewSkin {
        public final static int STEERING_VIEW_SKIN_SNAPMAKER_2_0 = 0;
        public final static int STEERING_VIEW_SKIN_A400 = 1;
        public final static int STEERING_VIEW_SKIN_J1 = 2;

        private final Context mContext;
        private final SparseArray<Drawable> mSteeringViewSkins = new SparseArray<>();

        public SteeringViewSkin(Context context) {
            this(context, STEERING_VIEW_SKIN_SNAPMAKER_2_0);
        }

        public SteeringViewSkin(Context context, int skin) {
            mContext = context;

            switch (skin) {
                case STEERING_VIEW_SKIN_A400:
                    initA400Skin();
                    break;
                case STEERING_VIEW_SKIN_J1:
                    initJ1Skin();
                    break;
                case STEERING_VIEW_SKIN_SNAPMAKER_2_0:
                default:
                    initSnapmaker2Skin();
                    break;

            }
        }

        void initSnapmaker2Skin() {
            mSteeringViewSkins.put(DIRECTION_UP, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_up_240x240));
            mSteeringViewSkins.put(DIRECTION_DOWN, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_down_240x240));
            mSteeringViewSkins.put(DIRECTION_LEFT, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_left_240x240));
            mSteeringViewSkins.put(DIRECTION_RIGHT, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_right_240x240));
            mSteeringViewSkins.put(DRECTION_IDLE, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_240x240));
            mSteeringViewSkins.put(STATE_DISABLE, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_disabled_240x240));

            mSteeringViewSkins.put(STATE_MOVING_UP, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_disabled_240x240));
            mSteeringViewSkins.put(STATE_MOVING_DOWN, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_disabled_240x240));
            mSteeringViewSkins.put(STATE_MOVING_LEFT, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_disabled_240x240));
            mSteeringViewSkins.put(STATE_MOVING_RIGHT, AppCompatResources.getDrawable(mContext, R.drawable.btn_steering_view_disabled_240x240));
        }

        void initA400Skin() {
            Bitmap bmMovingRight = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pic_a400_frozen_panel_right);

            mSteeringViewSkins.put(DIRECTION_UP, AppCompatResources.getDrawable(mContext, R.drawable.btn_a400_steering_view_up_400x400));
            mSteeringViewSkins.put(DIRECTION_DOWN, AppCompatResources.getDrawable(mContext, R.drawable.btn_a400_steering_view_down_400x400));
            mSteeringViewSkins.put(DIRECTION_LEFT, AppCompatResources.getDrawable(mContext, R.drawable.btn_a400_steering_view_left_400x400));
            mSteeringViewSkins.put(DIRECTION_RIGHT, AppCompatResources.getDrawable(mContext, R.drawable.btn_a400_steering_view_right_400x400));
            mSteeringViewSkins.put(DRECTION_IDLE, AppCompatResources.getDrawable(mContext, R.drawable.btn_a400_steering_view_400x400));
            mSteeringViewSkins.put(STATE_DISABLE, AppCompatResources.getDrawable(mContext, R.drawable.btn_a400_steering_view_disabled_400x400));

            mSteeringViewSkins.put(STATE_MOVING_UP, new BitmapDrawable(mContext.getResources(), rotateBitmap(bmMovingRight, -90)));
            mSteeringViewSkins.put(STATE_MOVING_DOWN, new BitmapDrawable(mContext.getResources(), rotateBitmap(bmMovingRight, 90)));
            mSteeringViewSkins.put(STATE_MOVING_LEFT, new BitmapDrawable(mContext.getResources(), rotateBitmap(bmMovingRight, -180)));
            mSteeringViewSkins.put(STATE_MOVING_RIGHT, new BitmapDrawable(mContext.getResources(), rotateBitmap(bmMovingRight, 0)));
        }

        void initJ1Skin() {
            mSteeringViewSkins.put(DIRECTION_UP, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_up_400x400));
            mSteeringViewSkins.put(DIRECTION_DOWN, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_down_400x400));
            mSteeringViewSkins.put(DIRECTION_LEFT, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_left_400x400));
            mSteeringViewSkins.put(DIRECTION_RIGHT, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_right_400x400));
            mSteeringViewSkins.put(DRECTION_IDLE, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_400x400));
            mSteeringViewSkins.put(STATE_DISABLE, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_disabled_400x400));

            mSteeringViewSkins.put(STATE_MOVING_UP, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_disabled_400x400));
            mSteeringViewSkins.put(STATE_MOVING_DOWN, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_disabled_400x400));
            mSteeringViewSkins.put(STATE_MOVING_LEFT, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_disabled_400x400));
            mSteeringViewSkins.put(STATE_MOVING_RIGHT, AppCompatResources.getDrawable(mContext, R.drawable.btn_j1_steering_view_disabled_400x400));
        }

        public Drawable getDrawable(int direction) {
            return mSteeringViewSkins.get(direction);
        }

        private Bitmap rotateBitmap(Bitmap src, int angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }
    }
}
