package fabscreen.features.addons.airpurifier;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import fabscreen.features.addons.R;
import fabscreen.features.addons.R2;
import fabscreen.features.addons.enclosure.EnclosureViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.presenter.AirPurifierControlWidgetPresenter;

public class AirPurifierHomeFragment extends BaseFragment {
    @BindView(R2.id.widget_add_on_air_purifier_panel)
    View mViewAirPurifier;
    private AirPurifierControlWidgetPresenter mAirPurifierControlWidgetPresenter;

    public static AirPurifierHomeFragment newInstance() {
        return new AirPurifierHomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_air_purifier);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_air_purifier_home;
    }

    @Override
    protected EnclosureViewModel getViewModel() {
        return getViewModelProvider().get(EnclosureViewModel.class);
    }

    private void initView() {
        mAirPurifierControlWidgetPresenter = new AirPurifierControlWidgetPresenter(disposables);
        mAirPurifierControlWidgetPresenter.bind(mViewAirPurifier);
        mAirPurifierControlWidgetPresenter.connectStatus();
    }
}
