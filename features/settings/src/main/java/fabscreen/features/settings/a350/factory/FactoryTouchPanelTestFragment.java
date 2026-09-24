package fabscreen.features.settings.a350.factory;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class FactoryTouchPanelTestFragment extends BaseFragment {
    private static final String TAG = FactoryTouchPanelTestFragment.class.getSimpleName();

    @BindView(R2.id.dsv_factory_view)
    DragSurfaceView mDragSurfaceView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDragSurfaceView = new DragSurfaceView(getContext(), null);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_factory_touch_panel_test;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_factory_touch_panel_back)
    void onClickBack() {
        playSwitchSound();
        if (mDragSurfaceView != null) {
            mDragSurfaceView.surfaceDestroyed(mDragSurfaceView.getHolder());
        }
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
