package fabscreen.features.settings.a350.factory;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class FactoryColorFraudFragment extends BaseFragment {
    private static final String TAG = FactoryTouchPanelTestFragment.class.getSimpleName();
    private static final int INTERVAL = 2000;
    @BindView(R2.id.view_color_fraud)
    View mView;
    private Handler mHandler;
    private int mIndex = 0;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            int[] colors = {
                    Color.RED,
                    Color.GREEN,
                    Color.BLUE,
                    Color.WHITE,
                    Color.BLACK

            };

            int color = colors[mIndex % 5];
            mIndex++;

            mView.setBackgroundColor(color);

            mHandler.postDelayed(this, INTERVAL);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("Color Fraud");

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 0);

        mView.setOnClickListener((v) -> back());

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_factory_color_fraud;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }
}
